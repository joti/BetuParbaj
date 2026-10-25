package hu.joti.betuparbaj.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Játszmamentések verziózott, kizárólag adatokat tartalmazó formátuma.
 * A régi Java-objektumos mentéseket csak admin számára engedjük át a szűrt olvasónak.
 */
public final class GameArchive {

  public static final int MAX_FILE_BYTES = 65536;
  private static final int MAX_DATA_BYTES = 262144;
  private static final int MAGIC = 0x42504a32; // BPJ2
  private static final int VERSION = 1;

  private GameArchive() {
  }

  public static boolean isNewFormat(int header) {
    return header == MAGIC;
  }

  public static byte[] write(Game game) throws IOException {
    LegacyGameReader.validate(game);
    ByteArrayOutputStream data = new ByteArrayOutputStream();

    try (DataOutputStream output = new DataOutputStream(data)) {
      output.writeInt(VERSION);
      output.writeInt(game.getId());
      output.writeUTF(game.getName());
      output.writeBoolean(game.isIncludeLongVowels());
      output.writeBoolean(game.isIncludeDigraphs());
      output.writeBoolean(game.isIncludeX());
      output.writeBoolean(game.isIncludeY());
      output.writeBoolean(game.isRandomPlace());
      output.writeUTF(game.getRndLetterMode().name());
      output.writeUTF(game.getScoringMode().name());
      output.writeInt(game.getMinPlayers());
      output.writeInt(game.getMaxPlayers());
      output.writeInt(game.getTimeLimit());
      output.writeBoolean(game.isRandomOrder());
      writeDate(output, game.getOpenDate());
      writeDate(output, game.getStartDate());
      writeDate(output, game.getEndDate());
      output.writeUTF(game.getOpeningPlayerName() == null ? "" : game.getOpeningPlayerName());

      for (String letter : game.getSelectedLetters()) {
        output.writeUTF(letter);
      }

      output.writeInt(game.getBoards().size());
      for (Board board : game.getBoards()) {
        writeBoard(output, board);
      }
    }

    if (data.size() > MAX_DATA_BYTES) {
      throw new IOException("A játszmamentés túl nagy.");
    }

    ByteArrayOutputStream archive = new ByteArrayOutputStream();
    DataOutputStream header = new DataOutputStream(archive);
    header.writeInt(MAGIC);

    try (GZIPOutputStream gzip = new GZIPOutputStream(archive)) {
      gzip.write(data.toByteArray());
    }

    if (archive.size() > MAX_FILE_BYTES) {
      throw new IOException("A játszmamentés túl nagy.");
    }

    return archive.toByteArray();
  }

  public static Game read(InputStream input, boolean admin) throws IOException {
    byte[] archive = readBounded(input, MAX_FILE_BYTES);
    DataInputStream header = new DataInputStream(new ByteArrayInputStream(archive));

    if (archive.length >= 2 && (archive[0] & 0xff) == 0x1f && (archive[1] & 0xff) == 0x8b) {
      return LegacyGameReader.read(new ByteArrayInputStream(archive), admin);
    }

    if (header.readInt() != MAGIC) {
      throw new IOException("Ismeretlen játszmaformátum.");
    }

    byte[] data;
    try (GZIPInputStream gzip = new GZIPInputStream(header)) {
      data = readBounded(gzip, MAX_DATA_BYTES);
    }

    try (DataInputStream values = new DataInputStream(new ByteArrayInputStream(data))) {
      if (values.readInt() != VERSION) {
        throw new IOException("Nem támogatott formátumverzió.");
      }

      int id = values.readInt();
      String name = readText(values, 100);
      boolean vowels = values.readBoolean();
      boolean digraphs = values.readBoolean();
      boolean x = values.readBoolean();
      boolean y = values.readBoolean();
      boolean randomPlace = values.readBoolean();
      RndLetterMode randomMode = RndLetterMode.valueOf(readText(values, 32));
      ScoringMode scoring = ScoringMode.valueOf(readText(values, 32));
      int minPlayers = readNumber(values, 1, 4);
      int maxPlayers = readNumber(values, minPlayers, 4);
      int timeLimit = readNumber(values, 15, 120);

      if (!Arrays.asList(Game.TIMELIMITS).contains(timeLimit)) {
        throw new IOException("Érvénytelen időkorlát.");
      }

      Game game = new Game(id, name, vowels, digraphs, x, y, randomPlace, randomMode,
              minPlayers, maxPlayers, timeLimit, scoring);
      game.setRandomOrder(values.readBoolean());
      game.setOpenDate(readDate(values));
      game.setStartDate(readDate(values));
      game.setEndDate(readDate(values));
      game.setOpeningPlayerName(readText(values, 20));

      for (int i = 0; i < 36; i++) {
        game.getSelectedLetters()[i] = readText(values, 2);
      }

      int playerCount = readNumber(values, 1, 4);
      List<Board> boards = new ArrayList<>();
      for (int pos = 0; pos < playerCount; pos++) {
        boards.add(readBoard(values, pos, playerCount));
      }

      if (values.read() != -1) {
        throw new IOException("Felesleges adat a mentés végén.");
      }

      game.setBoards(boards);
      game.setTurn(36);
      game.setUploaded(true);
      LegacyGameReader.validate(game);

      return game;
    } catch (IllegalArgumentException ex) {
      throw new IOException("Érvénytelen játszmaadat.", ex);
    }
  }

  private static void writeBoard(DataOutputStream output, Board board) throws IOException {
    output.writeUTF(board.getPlayer().getName());
    output.writeInt(board.getPlace());
    writeDate(output, board.getJoinDate());
    writeDate(output, board.getQuitDate());
    output.writeBoolean(board.isQuitByPlayer());

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 6; col++) {
        output.writeUTF(board.getLetters()[row][col]);
        output.writeInt(board.getTurnPlaces()[row][col]);
      }
    }

    output.writeInt(board.getUnplacedLetters().size());
    for (Map.Entry<Integer, String> entry : board.getUnplacedLetters().entrySet()) {
      output.writeInt(entry.getKey());
      output.writeUTF(entry.getValue());
    }

    output.writeInt(board.getHits().size());
    for (Hit hit : board.getHits()) {
      output.writeBoolean(hit.isHorizontal());
      output.writeInt(hit.getLine());
      output.writeInt(hit.getStart());
      output.writeInt(hit.getEnd());
      output.writeInt(hit.getScore());
      output.writeUTF(hit.getWord());
    }
  }

  private static Board readBoard(DataInputStream input, int position, int playerCount) throws IOException {
    Board board = new Board(new Player(readText(input, 20)));
    board.setPosition(position);
    board.setPlace(readNumber(input, 1, playerCount));
    board.setJoinDate(readDate(input));
    board.setQuitDate(readDate(input));
    board.setQuitByPlayer(input.readBoolean());

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 6; col++) {
        board.getLetters()[row][col] = readText(input, 2);
        board.getTurnPlaces()[row][col] = readNumber(input, -36, 36);
      }
    }

    int unplacedCount = readNumber(input, 0, 36);
    for (int i = 0; i < unplacedCount; i++) {
      int turn = readNumber(input, 1, 36);
      if (board.getUnplacedLetters().put(turn, readText(input, 2)) != null) {
        throw new IOException("Ismétlődő forduló a mentésben.");
      }
    }

    int hitCount = readNumber(input, 0, 12);
    for (int i = 0; i < hitCount; i++) {
      boolean horizontal = input.readBoolean();
      int line = readNumber(input, 0, 5);
      int start = readNumber(input, 0, 4);
      int end = readNumber(input, start + 1, 5);
      int score = readNumber(input, 0, 36);
      String word = readText(input, 12);
      board.getHits().add(new Hit(horizontal, line, start, end, score, word));
    }

    return board;
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

  private static String readText(DataInputStream input, int maxLength) throws IOException {
    String value = input.readUTF();
    if (value.length() > maxLength || value.chars().anyMatch(Character::isISOControl)) {
      throw new IOException("Érvénytelen szöveges adat.");
    }

    return value;
  }

  private static int readNumber(DataInputStream input, int min, int max) throws IOException {
    int value = input.readInt();
    if (value < min || value > max) {
      throw new IOException("Érvénytelen számérték.");
    }

    return value;
  }

  private static void writeDate(DataOutputStream output, Date date) throws IOException {
    output.writeLong(date == null ? -1 : date.getTime());
  }

  private static Date readDate(DataInputStream input) throws IOException {
    long value = input.readLong();
    if (value < -1) {
      throw new IOException("Érvénytelen időpont.");
    }

    return value == -1 ? null : new Date(value);
  }
}