package hu.joti.betuparbaj.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Régi BPJ mentések korlátozott betöltése bejelentkezett admin számára.
 */
public final class LegacyGameReader {

  public static final int MAX_COMPRESSED_BYTES = 65536;
  private static final int MAX_EXPANDED_BYTES = 262144;
  private static final Set<String> CLASSES = new HashSet<>(Arrays.asList(
      Game.class.getName(), Board.class.getName(), Player.class.getName(),
      Hit.class.getName(), ScoringMode.class.getName(), RndLetterMode.class.getName(),
      "java.util.ArrayList", "java.util.HashMap", "java.util.Date",
      "java.lang.Integer", "java.lang.Number", "java.lang.Enum", "java.lang.String"));

  private LegacyGameReader() {
  }

  public static Game read(InputStream input, boolean authorizedAdmin) throws IOException {
    if (!authorizedAdmin) {
      throw new IOException("A fájl formátuma nem megfelelő.");
    }

    byte[] compressed = readBounded(input, MAX_COMPRESSED_BYTES);
    byte[] expanded;

    try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
      expanded = readBounded(gzip, MAX_EXPANDED_BYTES);
    }

    try (ObjectInputStream objects = new ObjectInputStream(new ByteArrayInputStream(expanded)) {

      @Override
      protected Class<?> resolveClass(ObjectStreamClass description)
          throws IOException, ClassNotFoundException {
        Class<?> type = super.resolveClass(description);
        Class<?> component = type;

        while (component.isArray()) {
          component = component.getComponentType();
        }

        if (!component.isPrimitive() && !CLASSES.contains(component.getName())
            && !(type.isArray() && component == Object.class))
          throw new InvalidClassException("Nem engedélyezett osztály", description.getName());

        return type;
      }

      @Override
      protected Class<?> resolveProxyClass(String[] interfaces) throws IOException {
        throw new InvalidClassException("Proxy osztály nem tölthető be");
      }
    }) {
      installFilter(objects);
      Object value = objects.readObject();

      if (value == null || value.getClass() != Game.class) {
        throw new IOException("A fájl nem játszmát tartalmaz.");
      }

      Game game = (Game) value;
      validate(game);
      game.setUploaded(true);

      return game;
    } catch (ClassNotFoundException | RuntimeException ex) {
      throw new IOException("Érvénytelen régi játszmamentés", ex);
    }
  }

  private static void installFilter(ObjectInputStream input) throws IOException {
    // Java 9-től a publikus API-t, Java 8-on a JEP 290 visszaportolt változatát használjuk.
    String filterName = "java.io.ObjectInputFilter";

    try {
      try {
        Class.forName(filterName);
      } catch (ClassNotFoundException ex) {
        filterName = "sun.misc.ObjectInputFilter";
      }

      Class<?> filterClass = Class.forName(filterName);
      Class<?> config = Class.forName(filterName + "$Config");
      StringBuilder pattern = new StringBuilder("maxdepth=20;maxrefs=4000;maxarray=256;maxbytes=262144;");

      for (String name : CLASSES) {
        pattern.append(name).append(';');
      }

      pattern.append("java.lang.Object;java.util.Map$Entry;!*");
      Object filter = config.getMethod("createFilter", String.class).invoke(null, pattern.toString());

      if (filter == null) {
        throw new IOException("A deszerializációs szűrő nem érhető el.");
      }

      if (filterName.startsWith("java.io.")) {
        Method setter = ObjectInputStream.class.getMethod("setObjectInputFilter", filterClass);
        setter.invoke(input, filter);
      } else {
        config.getMethod("setObjectInputFilter", ObjectInputStream.class, filterClass)
            .invoke(null, input, filter);
      }
    } catch (ReflectiveOperationException | RuntimeException ex) {
      throw new IOException("A régi mentés betöltéséhez működő deszerializációs szűrő szükséges", ex);
    }
  }

  private static byte[] readBounded(InputStream input, int limit) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[2048];
    int count;

    while ((count = input.read(buffer)) != -1) {
      if (count > limit - output.size()) {
        throw new IOException("A játszmafájl túl nagy.");
      }

      output.write(buffer, 0, count);
    }

    return output.toByteArray();
  }

  static void validate(Game game) throws IOException {
    if (game == null || game.getEndDate() == null || game.getStartDate() == null || game.getTurn() != 36
        || game.getName() == null || game.getName().length() > 100
        || game.getScoringMode() == null || game.getRndLetterMode() == null
        || game.getBoards() == null || game.getBoards().isEmpty() || game.getBoards().size() > 4
        || game.getNumberOfPlayers() != game.getBoards().size()
        || game.getSelectedLetters() == null || game.getSelectedLetters().length != 36)
      throw new IOException("Érvénytelen játszmaállapot");

    for (String letter : game.getSelectedLetters()) {
      validateLetter(letter);
    }

    for (int pos = 0; pos < game.getBoards().size(); pos++) {
      Board board = game.getBoards().get(pos);

      if (board == null || board.getPlayer() == null || board.getPlayer().getName() == null
          || board.getPlayer().getName().length() > 20 || board.getPosition() != pos
          || board.getPlace() < 1 || board.getPlace() > game.getBoards().size()
          || board.getLetters() == null || board.getLetters().length != 6
          || board.getTurnPlaces() == null || board.getTurnPlaces().length != 6
          || board.getHits() == null || board.getHits().size() > 12
          || board.getUnplacedLetters() == null || board.getUnplacedLetters().size() > 36)
        throw new IOException("Érvénytelen tábla");

      for (int row = 0; row < 6; row++) {
        if (board.getLetters()[row] == null || board.getLetters()[row].length != 6
            || board.getTurnPlaces()[row] == null || board.getTurnPlaces()[row].length != 6)
          throw new IOException("Érvénytelen táblaméret");

        for (int col = 0; col < 6; col++) {
          validateLetter(board.getLetters()[row][col]);

          if (board.getTurnPlaces()[row][col] < -36 || board.getTurnPlaces()[row][col] > 36)
            throw new IOException("Érvénytelen forduló");
        }
      }

      for (Hit hit : board.getHits()) {
        if (hit == null || hit.getLine() < 0 || hit.getLine() > 5 || hit.getStart() < 0
            || hit.getEnd() > 5 || hit.getEnd() <= hit.getStart()
            || hit.getWord() == null || hit.getWord().length() > 12
            || hit.getScore() != game.getScoringMode().getLenValues()[hit.getEnd() - hit.getStart()])
          throw new IOException("Érvénytelen találat");
      }

      board.evaluate();
    }
  }

  private static void validateLetter(String letter) throws IOException {
    if (letter == null || (!letter.isEmpty() && !Arrays.asList(Game.ALPHABET).contains(letter)))
      throw new IOException("Érvénytelen betű");
  }
}