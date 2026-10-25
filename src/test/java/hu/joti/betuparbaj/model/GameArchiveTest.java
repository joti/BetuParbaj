package hu.joti.betuparbaj.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Új mentések visszatöltése, jogosultságok és hibás feltöltések ellenőrzése.
 */
public class GameArchiveTest {

  @Test
  public void newArchiveCanBeReadWithoutAdmin() throws Exception {
    Game original = createGame();
    original.setPassword("Ez ne kerüljön a mentésbe");
    original.setOpeningPlayerName("Játékos");
    original.getBoards().get(0).getLetters()[0][0] = "Á";
    original.getBoards().get(0).getLetters()[0][1] = "GY";
    original.getBoards().get(0).getTurnPlaces()[0][0] = 1;
    original.getBoards().get(0).getTurnPlaces()[0][1] = -2;
    original.getBoards().get(0).getHits().add(new Hit(true, 0, 0, 1, 4, "ÁGY"));
    original.getBoards().get(0).getUnplacedLetters().put(3, "ZS");

    Game loaded = GameArchive.read(new ByteArrayInputStream(GameArchive.write(original)), false);

    assertEquals(original.getName(), loaded.getName());
    assertEquals(original.getEndDate(), loaded.getEndDate());
    assertEquals("Játékos", loaded.getOpeningPlayerName());
    assertEquals("GY", loaded.getBoards().get(0).getLetters()[0][1]);
    assertEquals(-2, loaded.getBoards().get(0).getTurnPlaces()[0][1]);
    assertEquals("ZS", loaded.getBoards().get(0).getUnplacedLetters().get(3));
    assertEquals("ÁGY", loaded.getBoards().get(0).getHits().get(0).getWord());
    assertEquals(4, loaded.getBoards().get(0).getScore());
    assertEquals("", loaded.getPassword());
    assertTrue(loaded.isUploaded());
  }

  @Test
  public void fourPlayersAndRulesArePreserved() throws Exception {
    Game original = createGame();
    original.setRandomOrder(true);
    original.setTimeLimit(120);
    original.setScoringMode(ScoringMode.FIBONACCI);
    original.setRndLetterMode(RndLetterMode.WHOLEGAME);

    for (int i = 1; i < 4; i++) {
      original.addPlayer(new Player("Játékos" + i));
      original.getBoards().get(i).setPosition(i);
      original.getBoards().get(i).setPlace(i + 1);
    }

    Game loaded = GameArchive.read(new ByteArrayInputStream(GameArchive.write(original)), false);

    assertEquals(4, loaded.getNumberOfPlayers());
    assertEquals(120, loaded.getTimeLimit());
    assertTrue(loaded.isRandomOrder());
    assertEquals(ScoringMode.FIBONACCI, loaded.getScoringMode());
    assertEquals(RndLetterMode.WHOLEGAME, loaded.getRndLetterMode());
    assertEquals(3, loaded.getBoards().get(3).getPosition());
  }

  @Test
  public void legacyArchiveRequiresAdmin() throws Exception {
    byte[] legacy = legacyArchive(createGame());
    reject(legacy, false);

    Game loaded = GameArchive.read(new ByteArrayInputStream(legacy), true);
    assertEquals("Tesztjátszma", loaded.getName());
    assertTrue(loaded.isUploaded());
  }

  @Test
  public void restrictedLegacyReaderChecks() throws Exception {
    LegacyGameReaderCheck.main(new String[0]);
  }

  @Test
  public void truncatedFilesAreRejected() throws Exception {
    byte[] archive = GameArchive.write(createGame());

    for (int length = 0; length < archive.length; length++) {
      reject(Arrays.copyOf(archive, length), false);
    }
  }

  @Test
  public void invalidHeadersNeverFallBackToLegacyReader() throws Exception {
    byte[] legacy = legacyArchive(createGame());
    legacy[0] = 'B';
    legacy[1] = 'P';
    legacy[2] = 'J';
    legacy[3] = '2';

    reject(legacy, true);
    reject(new byte[]{1, 2, 3, 4}, true);
  }

  @Test
  public void oversizedCompressedFileIsRejected() throws Exception {
    reject(new byte[GameArchive.MAX_FILE_BYTES + 1], true);
  }

  @Test
  public void oversizedExpandedFileIsRejected() throws Exception {
    reject(newArchive(new byte[262145]), false);
  }

  @Test
  public void unsupportedVersionIsRejected() throws Exception {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    new DataOutputStream(data).writeInt(99);

    reject(newArchive(data.toByteArray()), false);
  }

  @Test
  public void unfinishedGameCannotBeSaved() throws Exception {
    Game game = createGame();
    game.setEndDate(null);

    try {
      GameArchive.write(game);
      fail("Befejezetlen játszma nem menthető.");
    } catch (IOException expected) {
      // A mentést még a HTTP-válasz megkezdése előtt el kell utasítani.
    }
  }

  private static Game createGame() {
    Game game = new Game(17, "Tesztjátszma", false, true, true, true, true,
            RndLetterMode.NORNDLETTER, 1, 4, 30, ScoringMode.SQUARE);
    game.addPlayer(new Player("Játékos", new Date(1000)));
    game.setOpenDate(new Date(1000));
    game.setStartDate(new Date(2000));
    game.setEndDate(new Date(3000));
    game.setTurn(36);
    game.getBoards().get(0).setPosition(0);
    game.getBoards().get(0).setPlace(1);

    return game;
  }

  private static byte[] legacyArchive(Game game) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (ObjectOutputStream objects = new ObjectOutputStream(new GZIPOutputStream(output))) {
      objects.writeObject(game);
    }

    return output.toByteArray();
  }

  private static byte[] newArchive(byte[] data) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    new DataOutputStream(output).writeInt(0x42504a32);

    try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
      gzip.write(data);
    }

    return output.toByteArray();
  }

  private static void reject(byte[] archive, boolean admin) throws IOException {
    try {
      GameArchive.read(new ByteArrayInputStream(archive), admin);
    } catch (IOException expected) {
      return;
    }

    fail("A hibás vagy tiltott mentést el kellett volna utasítani.");
  }
}