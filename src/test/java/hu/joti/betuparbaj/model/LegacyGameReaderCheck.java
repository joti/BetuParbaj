package hu.joti.betuparbaj.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * A régi BPJ olvasó célzott ellenőrzése, servletkonténer nélkül.
 */
public class LegacyGameReaderCheck {

  private static int checks;

  public static void main(String[] args) throws Exception {
    Game game = new Game(1, "Régi játék", false, false, true, true, true,
            RndLetterMode.NORNDLETTER, 1, 4, 30, ScoringMode.SQUARE);
    game.addPlayer(new Player("Játékos", new Date()));
    game.setStartDate(new Date());
    game.setEndDate(new Date());
    game.setTurn(36);
    game.getBoards().get(0).setPosition(0);
    game.getBoards().get(0).setPlace(1);

    byte[] valid = archive(game);
    Game result = LegacyGameReader.read(new ByteArrayInputStream(valid), true);
    require(result.isUploaded() && result.getBoards().size() == 1, "Admin betöltés");

    reject(valid, false, "Nem admin betöltés");
    reject(archive(new Date()), true, "Hibás gyökértípus");
    reject(archive(new Disallowed()), true, "Tiltott osztály");
    require(!Disallowed.executed, "A tiltott readObject nem futhat le");
    reject(archive(new String[1000]), true, "Túl nagy tömb");
    reject(new byte[70000], true, "Tömörített méretkorlát");

    ByteArrayOutputStream expanded = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(expanded)) {
      gzip.write(new byte[300000]);
    }
    reject(expanded.toByteArray(), true, "Kibontott méretkorlát");
    reject(new byte[]{1, 2, 3}, true, "Hibás GZIP");

    game.getBoards().get(0).setPlace(99);
    reject(archive(game), true, "Érvénytelen helyezés");

    System.out.println("PASS: " + checks + " ellenőrzés, Java " + System.getProperty("java.version"));
  }

  private static byte[] archive(Object value) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (ObjectOutputStream objects = new ObjectOutputStream(new GZIPOutputStream(output))) {
      objects.writeObject(value);
    }

    return output.toByteArray();
  }

  private static void reject(byte[] data, boolean admin, String name) throws Exception {
    try {
      LegacyGameReader.read(new ByteArrayInputStream(data), admin);
    } catch (IOException ex) {
      checks++;
      return;
    }

    throw new AssertionError("Nem lett elutasítva: " + name);
  }

  private static void require(boolean condition, String name) {
    if (!condition) {
      throw new AssertionError(name);
    }

    checks++;
  }

  private static class Disallowed implements Serializable {

    private static boolean executed;

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
      executed = true;
      input.defaultReadObject();
    }
  }
}