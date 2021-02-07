/*v
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Board;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Hit;
import hu.joti.betuparbaj.model.RndLetterMode;
import hu.joti.betuparbaj.model.ScoringMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@SessionScoped
public class GameManager implements Serializable {

  @ManagedProperty("#{lobby}")
  Lobby lobby;

  @ManagedProperty("#{glossaryManager}")
  GlossaryManager glossaryManager;

  @ManagedProperty("#{loginData}")
  LoginData loginData;

  private Game game;
  private Game prevGame;
  private Set<Integer> fadingGames;
  private Set<Integer> fadedGames;
  private String password;
  private int myPosition;

  // menü: 0 - főmenü, 1 - megnyitott játékok, 2 - folyamatban lévő játékok, 3 - befejeződött játékok, 4 - játékszabályok, 5 - üzenetküldés, 6 - szótár, 7 - admin oldal
  private int menu;
  // befejeződött játék lekérdezésének módja: 1 - eredmény, 2 - setup
  private int gameViewMode;
  // azon játszmák, amelyek szabályai láthatók a lobbiban
  private Set<Integer> visibleGameRules;

  private List<Integer> letterIndices;
  private List<Integer> cellIndices;
  private String testWord;
  private int testResult;
  private Set<String> missingWords;

  private static final Logger LOGGER = LogManager.getLogger(GameManager.class.getName());

  public GameManager() {
    LOGGER.info("GameManager constructor");
    fadingGames = new HashSet<>();
    fadedGames = new HashSet<>();
    visibleGameRules = new HashSet<>();
    myPosition = -1;
    gameViewMode = 0;
    password = "";
    menu = 0;

    letterIndices = new ArrayList<>();
    for (int i = 0; i < Game.ALPHABET.length; i++) {
      letterIndices.add(i);
    }

    cellIndices = new ArrayList<>();
    for (int i = 0; i < Board.BOARD_SIZE * Board.BOARD_SIZE; i++) {
      cellIndices.add(i);
    }
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("GameManager session starts");
  }

  @PreDestroy
  public void destroy() {
    LOGGER.debug("GameManager session ends for " + loginData.getName());
    if (game != null) {
      quitGame(false);
    }
  }

  public void refresh() {
    boolean wasEntered = loginData.isEntered();
    loginData.refresh();

    if (!loginData.isEntered()) {
      quitGame(false);

      if (wasEntered && !loginData.getName().isEmpty()) {
        login();
        rejoinGame();
      }

      return;
    }

    if (lobby != null) {
      Set<Integer> gamesInLobby = new HashSet<>();
      int sec = loginData.getSeconds();

      for (Game g : lobby.getGamesInLobby()) {
        gamesInLobby.add(g.getId());

        if (g.getMaxPlayers() <= g.getNumberOfPlayers() && g.getPlayerPos(loginData.getName()) < 0) {
          if (!fadedGames.contains(g.getId())) {
            if (!fadingGames.contains(g.getId())) {
              fadingGames.add(g.getId());
              LOGGER.debug("Game " + g.getId() + " is fading at " + sec + " s");
            } else {
              fadedGames.add(g.getId());
              LOGGER.debug("Game " + g.getId() + " is already faded at " + sec + " s");
            }
          }
        } else {
          if (fadingGames.remove(g.getId())) {
            LOGGER.debug("Game " + g.getId() + " is not fading any more at " + sec + " s");
          }
          fadedGames.remove(g.getId());
        }
      }

      for (Integer gId : fadingGames) {
        if (!gamesInLobby.contains(gId)) {
          if (fadingGames.remove(gId)) {
            LOGGER.debug("Game " + gId + " is not in lobby any more at " + sec + " s");
          }
          fadedGames.remove(gId);
        }
      }
    }

    if (game != null && game.getStartDate() != null && game.getEndDate() == null) {
      if (myPosition < 0) {
        fillMyPosition();
      }

      // Ha mi vagyunk épp soron, vagy mi vagyunk a legközelebbi még aktív játékos (a soron lévő játékos már kilépett vagy nem tud kapcsolódni),
      // akkor szükség esetén elindítjuk a következő kört
      if (game.getNextActivePlayerPos() == myPosition) {

        int turn = game.getTurn();
        boolean endTurn = (turn >= 0);

        // Rakott-e már mindenki betűt?
        if (endTurn && turn > 0) {
          for (Board board : game.getBoards()) {
            // Kilépett játékosra nem várakozunk
            if ((board.getTotalLetterCount() < turn || board.isRandomPlaced(turn))
                    && board.getQuitDate() == null) {
              endTurn = false;
              break;
            }
          }
        }

        // A soron következő játékos kiválasztotta-e már a következő betűt?
        if (endTurn && turn >= 0 && turn < 36 && game.canPlayerSelectLetter()
                && game.getBoards().get(game.getCurrentPlayer()).getQuitDate() == null) {
          if (game.getSelectedLetters()[turn].isEmpty() || game.getRandomLetters()[turn]) {
            endTurn = false;
          }
        }

        int turnSec = getTurnSec();
        int turnTimeLimit = game.getTurnTimeLimit();

        if (turnSec > turnTimeLimit || endTurn) {
          if (game.getIntermissionStart() == null) {
            game.endTurn(); // Itt megtörténik a gépi lehelyezés és betűsorsolás, szükség esetén intermission indítása
          }
          if (game.getIntermissionStart() == null || turnSec > turnTimeLimit + Game.TURN_INTERMISSION) {
            LOGGER.debug(loginData.getName() + ": turn " + turn + " -> " + (turn + 1));
            game.nextTurn();

            if (game.getEndDate() != null) {
              evaluateGame();
              testResult = 0;
              testWord = "";
              lobby.addToFinished(game);
            }
          }
        }
      }
    }

  }

  public void evaluateGame() {
    LOGGER.debug("Evaluating: #" + game.getId());
    if (game != null && game.getTurn() == 36) {
      for (Board board : game.getBoards()) {
        String[][] letters = board.getLetters();
        int[][] turnPlaces = board.getTurnPlaces();
        String[] streak = new String[6];

        /* Végighaladunk a tábla sorain */
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
          for (int col = 0; col < Board.BOARD_SIZE; col++) {
            if (turnPlaces[row][col] == 0)
              streak[col] = "_";
            else
              streak[col] = letters[row][col];
          }
          Hit hit = glossaryManager.findHit(streak, !game.isIncludeLongVowels(), game.getScoringMode());
          if (hit != null) {
            LOGGER.info(board.getPlayer().getName() + "'s word: " + hit.getWord());
            hit.setHorizontal(true);
            hit.setLine(row);
            board.getHits().add(hit);
          }
        }

        /* ...és az oszlopain */
        for (int col = 0; col < Board.BOARD_SIZE; col++) {
          for (int row = 0; row < Board.BOARD_SIZE; row++) {
            if (turnPlaces[row][col] == 0)
              streak[row] = "_";
            else
              streak[row] = letters[row][col];
          }
          Hit hit = glossaryManager.findHit(streak, !game.isIncludeLongVowels(), game.getScoringMode());
          if (hit != null) {
            LOGGER.info(board.getPlayer().getName() + "'s word: " + hit.getWord());
            hit.setHorizontal(false);
            hit.setLine(col);
            board.getHits().add(hit);
          }
        }

        LOGGER.debug(board.getPlayer().getName() + "'s no of words: " + board.getHits().size());
        board.evaluate();
        LOGGER.info(board.getPlayer().getName() + "'s points: " + board.getScore());
        board.fillGaps();
      }

      // Helyezések kiszámítása
      for (Board board : game.getBoards()) {
        int place = 1;
        for (int pos = 0; pos < game.getBoards().size(); pos++) {
          if (pos != board.getPosition() && game.getBoards().get(pos).getScore() > board.getScore())
            place++;
        }
        board.setPlace(place);
      }
    }
  }

  public boolean isGameFading(Game g) {
    return fadingGames.contains(g.getId()) && !fadedGames.contains(g.getId());
  }

  public List<Game> getGamesFromLobby() {
    switch (getLobbyMode()) {
      case 1:
        return lobby.getGamesInLobby();
      case 2:
        return lobby.getGamesInProgress();
      case 3:
        return lobby.getGamesFinished();
    }
    return null;
  }

  public String getGamesInLobbyString() {
    String gamesString = ":";
    List<Game> games = getGamesFromLobby();
    if (games != null) {
      for (Game g : games) {
        gamesString += g.getGameSetupString() + ";";
      }
      gamesString += (loginData.getSeconds() / 60) + "";
    }
    return gamesString;
  }

  public void fillMyPosition() {
    if (game == null || game.getStartDate() == null)
      myPosition = -1;
    else
      myPosition = game.getPlayerPos(loginData.getName());
  }

  public void changeMyPosition(int position) {
    if (game != null && game.getEndDate() != null)
      myPosition = position;
  }

  public void debugGame(String text) {
    if (loginData != null) {
      LOGGER.debug(text + " (" + loginData.getName() + ")");
    } else {
      LOGGER.debug(text);
    }
  }

  public void login() {
    loginData.doLogin();

    for (Game g : lobby.getGamesInLobby()) {
      if (g.getMaxPlayers() <= g.getNumberOfPlayers()) {
        fadingGames.add(g.getId());
        fadedGames.add(g.getId());
      }
    }
  }

  public void createGame() {
    int gameId = lobby.getGameId();

    String name = lobby.getDefGameName();
    LOGGER.info(loginData.getName() + " creates game #" + gameId + " with name " + name);

    /* A játékos legutóbbi asztalának beállításaival indítunk */
    if (prevGame != null) {
      game = new Game(gameId, name, prevGame.isIncludeLongVowels(), prevGame.isIncludeDigraphs(), prevGame.isIncludeX(), prevGame.isIncludeY(), prevGame.isRandomPlace(), prevGame.getRndLetterMode(),
              prevGame.getMinPlayers(), prevGame.getMaxPlayers(), prevGame.getTimeLimit(), prevGame.getScoringMode());
    } else {
      game = new Game(gameId, name, Game.DEF_INCLUDELONGVOWELS, Game.DEF_INCLUDEDIGRAPHS, Game.DEF_INCLUDEX, Game.DEF_INCLUDEY, Game.DEF_RANDOMPLACE, Game.DEF_RNDLETTERMODE,
              Game.DEF_MINPLAYERS, Game.DEF_MAXPLAYERS, Game.DEF_TIMELIMIT, Game.DEF_SCORING_MODE);
    }

    game.addPlayer(loginData.getPlayer());
    myPosition = -1;
    gameViewMode = 0;
    password = "";
    clearWord();
    lobby.getGamesInPrep().add(game);
  }

  public boolean isGameRulesVisible(Game g){
    if (g != null)
      return visibleGameRules.contains(g.getId());
    else
      return false;
  }

  public void showGameRules(Game g){
    if (g != null)
      visibleGameRules.add(g.getId());
    LOGGER.info(visibleGameRules.size());
  }

  public void hideGameRules(Game g){
    if (g != null)
      visibleGameRules.remove(g.getId());
    LOGGER.info(visibleGameRules.size());
  }
  
  public boolean canJoinGame(Game g) {
    if (game != null || g == null || g.getOpenDate() == null || g.getEndDate() != null)
      return false;

    if (g.getStartDate() == null)
      return (g.getNumberOfPlayers() < g.getMaxPlayers());

    return (g.getPlayerPos(loginData.getName()) >= 0);
  }

  public boolean canJoinGameWithPassword(Game g) {
    if (game != null || g == null || g.getOpenDate() == null || g.getEndDate() != null)
      return false;

    if (g.getPassword().isEmpty())
      return false;
    else
      return canJoinGame(g);
  }

  public boolean canJoinGameWOPassword(Game g) {
    if (game != null || g == null || g.getOpenDate() == null || g.getEndDate() != null)
      return false;

    if (!g.getPassword().isEmpty())
      return false;
    else
      return canJoinGame(g);
  }

  public void joinGame(Game g) {
    if (game == null && g != null && g.getOpenDate() != null && g.getEndDate() == null) {
      LOGGER.info(loginData.getName() + " joins game #" + g.getId());
      game = g;
      game.addPlayer(loginData.getPlayer());
      fillMyPosition();
      clearWord();
      gameViewMode = 0;
      setMenu(0);
      visibleGameRules.clear();
      password = "";
    }
  }

  public void rejoinGame() {
    // Ha van olyan játék, amelyik még zajlik, és a játékos helye úgy betöltetlen, hogy szándékosan nem lépett ki, akkor újra beléptetjük
    Game gameToJoin = null;
    Date maxQuitDate = null;

    games:
    for (Game game : lobby.getGamesInProgress()) {
      if (game.getStartDate() != null && game.getEndDate() == null) {
        for (Board board : game.getBoards()) {
          if (board.getPlayer().getName().equals(loginData.getName()) && board.getQuitDate() != null && !board.isQuitByPlayer()) {
            if (maxQuitDate == null || board.getQuitDate().after(maxQuitDate)) {
              gameToJoin = game;
              maxQuitDate = board.getQuitDate();
              continue games;
            }
          }
        }
      }
    }

    if (gameToJoin != null) {
      joinGame(gameToJoin);
    }
  }

  public boolean canViewGame(Game g) {
    return (game == null && g.getEndDate() != null);
  }

  public void viewGame(Game g) {
    LOGGER.debug(loginData.getName() + " views game #" + g.getId());
    if (game == null && g.getEndDate() != null) {
      game = g;
      myPosition = 0;
      gameViewMode = 1;

      // A győztes játékos tábláját mutatjuk elsőre
      for (Board board : game.getBoards()) {
        if (board.getPlace() == 1) {
          myPosition = board.getPosition();
          break;
        }
      }
    }
  }

  public void switchGameViewMode() {
    if (gameViewMode == 1)
      gameViewMode = 2;
    else if (gameViewMode == 2)
      gameViewMode = 1;
  }

  public void quitGame(boolean byPlayer) {
    if (game != null) {
      LOGGER.info(loginData.getName() + " quits game #" + game.getId());
      if (game.getEndDate() == null) {
        game.removePlayer(loginData.getName(), byPlayer);
        LOGGER.info(loginData.getName() + " removed from game #" + game.getId());
      }
      clearWord();
      prevGame = game;
      game = null;
    }
  }

  public void openGame() {
    if (game != null) {
      LOGGER.info(loginData.getName() + " opens game #" + game.getId());
      game.setOpenDate(new Date());
      game.setOpeningPlayerName(loginData.getName());
      lobby.getGamesInPrep().remove(game);
      lobby.getGamesInLobby().add(game);
      password = password.trim();
    }
  }

  public void dropGame() {
    if (game != null) {
      LOGGER.info(loginData.getName() + " drops game #" + game.getId());
      clearWord();
      lobby.getGamesInPrep().remove(game);
      lobby.getGamesInLobby().remove(game);
      lobby.getGamesInProgress().remove(game);
      prevGame = game;
      game = null;
    }
  }

  public void removeGame() {
    if (game != null && game.getEndDate() != null) {
      LOGGER.info(loginData.getName() + " removes game #" + game.getId());
      lobby.getGamesFinished().remove(game);
      game = null;
    }
  }

  public boolean gameExists() {
    if (game == null) {
      return false;
    }

    // Amíg nincs megnyitva, addig csak abban a sessionben látható, ahol létrehozták
    if (game.getOpenDate() == null) {
      return true;
    }

    // Ha a létrehozó játékos eldobta az asztalt, akkor a lobbiból is kikerült
    if (lobby.getGamesInLobby().contains(game)) {
      return true;
    }

    if (lobby.getGamesInProgress().contains(game)) {
      return true;
    }

    if (lobby.getGamesFinished().contains(game)) {
      return true;
    }

    prevGame = game;
    game = null;
    return false;
  }

  /* a játékos kijelentkezik*/
  public void quit() {
    if (game != null) {
      quitGame(true);
    }
    loginData.doLogout();
  }

  public String backToLogin() {
    if (game != null) {
      quitGame(false);
    }
    loginData.doLogout();
    return "index";
  }

  public void startGame() {
    if (game != null) {
      if (game.getOpenDate() == null) {
        game.setOpenDate(new Date());
        lobby.getGamesInPrep().remove(game);
      } else {
        lobby.getGamesInLobby().remove(game);
      }

      game.start();
      lobby.getGamesInProgress().add(game);
      fillMyPosition();
    }
  }

  public void refresh(int source) {
    if (game == null) {
      return;
    }

    // Amíg nincs megnyitva, addig csak abban a sessionben látható, ahol létrehozták
    if (game.getOpenDate() == null) {
      return;
    }

    // Ha a létrehozó játékos eldobta az asztalt, akkor a lobbiból is kikerült
    if (lobby.getGamesInLobby().contains(game)) {
      return;
    }

    if (lobby.getGamesInProgress().contains(game)) {
      return;
    }

    prevGame = game;
    game = null;
  }

  public int getGameState() {
    /* Állapotkódok:
       0 - lobby
       1 - játék előkészítése
       2 - asztal megnyitva, további játékosok csatlakozására vár
       3 - játék elindítva
       4 - játék véget ért
     */
    if (!gameExists()) {
      return 0;
    }
    if (game.getOpenDate() == null) {
      return 1;
    }
    if (game.getStartDate() == null) {
      return 2;
    }
    if (game.getEndDate() == null) {
      return 3;
    }
    return 4;
  }

  public boolean isInGameState(int... states) {
    if (states.length == 0) {
      return false;
    }

    int gameState = getGameState();

    for (int i = 0; i < states.length; i++) {
      if (states[i] == gameState) {
        return true;
      }
    }

    return false;
  }

  public boolean isInMenu(int... ids) {
    if (ids.length == 0) {
      return false;
    }

    for (int i = 0; i < ids.length; i++) {
      if (ids[i] == menu) {
        return true;
      }
    }

    return false;
  }

  public boolean isPlayerTurnOn(int player) {
    if (game == null)
      return false;

    if (game.getStartDate() == null || game.getEndDate() != null)
      return false;

    if (game.getTurn() == -1)
      return false;

    if (game.getTurn() == 0 && game.getCurrentPlayer() != player)
      return false;

    return !isPlayerTurnFinished(player);
  }

  public boolean isPlayerActive(String player) {
    if (game == null || game.getOpenDate() == null) {
      return false;
    }

    return game.isPlayerActive(player);
  }

  public boolean isPlayerTurnFinished(int player) {
    if (game.getStartDate() != null && game.getEndDate() == null) {
      int turn = game.getTurn();

      if (turn >= 0 && turn < 36 && game.getCurrentPlayer() == player && game.canPlayerSelectLetter()) {
        if (!game.getSelectedLetters()[turn].isEmpty() && !game.getRandomLetters()[turn]) {
          return true;
        }
      } else if (turn > 0) {
        boolean intermission = (game.getIntermissionStart() != null);
        Board board = game.getBoards().get(player);

        if (board.getTotalLetterCount() >= turn
                && !((!game.isRandomPlace() && board.getUnplacedLetters().containsKey(turn))
                || (game.isRandomPlace() && board.isRandomPlaced(turn)))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean canPlayerSelectLetterInTurn(int player) {
    return (game != null && game.canPlayerSelectLetter() && game.getTurn() < 36 && game.getCurrentPlayer() == player);
  }

  public boolean canPlayerSelectLetter() {
    return (getPlayerState() == 2 && game.getPreselectedLetter().isEmpty());
  }

  public boolean canPlayerPlaceLetter() {
    return (getPlayerState() == 1);
  }

  public boolean needPlayerConfirmLetter() {
    return (getPlayerState() == 2 && !game.getPreselectedLetter().isEmpty());
  }

  public String getGamePlayersRowClass(int position) {
    if (position == myPosition) {
      return "gameplayersrow2";
    } else {
      return "gameplayersrow";
    }
  }

  public int getPlayerState() {
    /* Állapotkódok:
       0 - a játék még nem indult el, vagy már véget ért
       1 - a játékos betűt rak le
       2 - a játékos betűt választ
       3 - a játékos a többi játékosra / a következő forduló kezdetére vár
     */
    if (getGameState() != 3) {
      return 0;
    }

    if (myPosition < 0) {
      fillMyPosition();
    }

    int turn = game.getTurn();
    if (turn >= 0) {
      if (game.getBoards().get(myPosition).getTotalLetterCount() < turn) // A játékos még nem rakta le az aktuális betűt
      {
        return 1;
      }

      if (turn < 36 && myPosition == game.getCurrentPlayer() && game.getSelectedLetters()[turn].isEmpty() && game.canPlayerSelectLetter()) {
        return 2;
      }
    }

    return 3;
  }

  public String getGameSetupString() {
    if (game != null) {
      return game.getGameSetupString();
    } else {
      return "";
    }
  }

  public String getGameSetupString2() {
    if (game != null) {
      for (Game g : lobby.getGamesInLobby()) {
        if (g.getId() == game.getId()) {
          return g.getGameSetupString();
        }
      }
    }
    return "";
  }

  public String getGameHistString() {
    String gameHistString = "";
    if (game != null && game.getStartDate() != null) {
      gameHistString = game.getGameHistString() + "->" + getPlayerState();
      if (game.getTurn() == -1) {
        gameHistString += "{" + getRemainingTurnSec() + "}";
      }
    }
    return gameHistString;
  }

  public String getBoardHitsString() {
    if (game != null && game.getEndDate() != null && myPosition >= 0) {
      Board board = game.getBoards().get(myPosition);
      return board.getHitsString();
    }
    return "";
  }

  public String getGameMsg() {
    if (game == null || game.getStartDate() == null) {
      return "";
    }

    String msg = "";
    String name = "";
    if (game.getEndDate() != null) {
      int count = 0;
      for (Board b : game.getBoards()) {
        if (b.getPlace() == 1) {
          count++;
          if (!name.isEmpty()) {
            name += " és \n\n";
          }
          name += b.getPlayer().getName();
        }
      }
      if (count > 1 && count == game.getBoards().size()) {
        msg = "\n\n\n\nA játék véget ért.\n\nAz eredmény döntetlen.";
      } else if (game.getNumberOfPlayers() > 1) {
        msg = String.format("\n\n\nA játék véget ért.\n\nA győztes:\n\n%s", name);
      } else {
        msg = "\n\n\n\n\n\nA játék véget ért.";
      }
    } else {
      int turn = game.getTurn();
      int playerState = getPlayerState();
      int currentPlayerPos = game.getCurrentPlayer();
      int rndLetterNumLimit = game.getRndLetterNumLimit();
      boolean intermission = (game.getIntermissionStart() != null);
      name = game.getBoards().get(currentPlayerPos).getPlayer().getName();

      if (turn == -1) {
        // A -1. kör a játék indulása előtti üzenet megjelenítésére szolgál
        int remSec = getRemainingTurnSec();

        switch (remSec) {
          case 1000:
          case 1001:
            if (game.getNumberOfPlayers() > 1 || rndLetterNumLimit > 0)
              msg = "\n\nJó játékot!";
          case 1002:
            if (game.getNumberOfPlayers() == 1 && rndLetterNumLimit == 0)
              msg = "\n\nJó játékot!";
          case 1003:
            if (rndLetterNumLimit == 36) {
              msg = "\n\nA betűket ezúttal\na számítógép fogja\nkisorsolni." + msg;
            } else if (rndLetterNumLimit > 0) {
              if (rndLetterNumLimit > 1)
                msg = String.format("\n\nAz első %d betűt\na számítógép fogja\nkisorsolni.", rndLetterNumLimit) + msg;
              else
                msg = "\n\nAz első betűt\na számítógép fogja\nkisorsolni." + msg;
            } else if (game.getNumberOfPlayers() > 1) {
              msg = String.format("\n\nAz első betűt\n%s\nválasztja ki.", name) + msg;
            }
          default:
            msg = "\n\nA játék hamarosan\nkezdődik!" + msg;
            if (game.getNumberOfPlayers() == 1 && rndLetterNumLimit == 0)
              msg = "\n\n" + msg;
        }

      } else if (turn == 0 && myPosition == currentPlayerPos && !intermission && rndLetterNumLimit == 0) {
        // A 0. körben a játékos választ betűt
        if (rndLetterNumLimit == 0 && playerState == 2) {
          if (game.getPreselectedLetter().isEmpty())
            msg = "\nKérlek, válaszd ki\naz első betűt!";
          else
            msg = "\n\nA kiválasztott betű:";
        }
      } else if (turn == 0 && myPosition != currentPlayerPos && rndLetterNumLimit == 0) {
        // A 0. körben egy másik játékos választja az első betűt   
        if (rndLetterNumLimit == 0)
          msg = String.format("\n\n\n\n\n%s kiválasztja\naz első betűt.", name);
      } else if (playerState == 1) {
        // A kiválasztott betűt el kell helyezni a táblán
        String letter = game.getSelectedLetter();
        if (turn < 36 && myPosition == currentPlayerPos && game.canPlayerSelectLetter()) {
          msg = String.format("\n\nA %d. betű:\n\n\n\n\n Helyezd el a táblán,\nmajd válaszd ki\na következő betűt!", turn);
        } else {
          msg = String.format("\n\nA %d. betű:\n\n\n\n\n\n Helyezd el a táblán!", turn);
        }
      } else if (playerState == 2) {
        if (game.getPreselectedLetter().isEmpty())
          msg = "\nVálaszd ki\na következő betűt!";
        else
          msg = "\n\n\nA kiválasztott betű:";
      } else {
        // Kifutottunk az időből
        if (intermission) {
          Board board = game.getBoards().get(myPosition);

          // Nem sikerült időben lehelyezni a betűt
          if (turn > 0 && (!game.isRandomPlace() && board.getUnplacedLetters().containsKey(turn))) {
            msg = "\n\n\n\n\n\nLejárt az idő.";
          } else if (turn > 0 && (game.isRandomPlace() && board.isRandomPlaced(turn))) {
            String cellName = board.getTurnPlaceCellName(turn);
            if (cellName.charAt(0) == 'A' || cellName.charAt(0) == 'E' || cellName.charAt(0) == 'F')
              msg = String.format("\n\n\n\nLejárt az idő.\n\nA betű az %s\ncellába került.", cellName);
            else
              msg = String.format("\n\n\n\nLejárt az idő.\n\nA betű a %s\ncellába került.", cellName);
          } // A következő betű kiválasztása nem sikerült időben
          else if (turn < 36 && myPosition == currentPlayerPos && game.getRandomLetters()[turn] && game.canPlayerSelectLetter()) {
            msg = "\n\n\n\n\nLejárt az idő.";
          } else if (game.getNumberOfPlayers() > 1) {
            msg = "\n\n\n\nVárakozás\n\na többi játékosra...";
          }
        } else if (game.getNumberOfPlayers() > 1) {
          msg = "\n\n\n\nVárakozás\n\na többi játékosra...";
        }

      }
    }
    return msg;
  }

  public void swapPlayers(int pos1, int pos2) {
    if (game != null && game.getNumberOfPlayers() > pos1 && game.getNumberOfPlayers() > pos2) {
      game.swapPlayers(pos1, pos2);
    }
  }

  public int getMyGamePos() {
    if (game != null) {
      return game.getPlayerPos(loginData.getName());
    }
    return -1;
  }

  public void decMinPlayers() {
    if (game != null) {
      game.decMinPlayers();
    }
  }

  public void incMinPlayers() {
    if (game != null) {
      game.incMinPlayers();
    }
  }

  public void decMaxPlayers() {
    if (game != null) {
      game.decMaxPlayers();
    }
  }

  public void incMaxPlayers() {
    if (game != null) {
      game.incMaxPlayers();
    }
  }

  public int getTurnSec() {
    int turnSec = 0;
    if (game != null && game.getStartDate() != null) {
      if (game.getEndDate() != null || game.getTurnStart() == null) {
        turnSec = 0;
      } else {
        Date now = new Date();
        turnSec = (int) ((now.getTime() - game.getTurnStart().getTime()) / 1000);
      }
    }
    return turnSec;
  }

  public int getRemainingTurnSec() {
    int rTurnSec = 1000;
    if (game != null && game.getStartDate() != null && game.getEndDate() == null) {
      rTurnSec = game.getTurnTimeLimit() - getTurnSec();
      if (rTurnSec < 0) {
        rTurnSec = 0;
      }
      if (game.getTurn() == -1)
        rTurnSec += 1000;
    }
    return rTurnSec;
  }

  public String getPlayerName() {
    if (game != null && game.getEndDate() != null) {
      return game.getBoards().get(myPosition).getPlayer().getName();
    }
    return "";
  }

  public List<Hit> getHits(boolean horizontal, boolean vertical) {
    List<Hit> hits = new ArrayList<>();
    if (game != null && game.getEndDate() != null) {
      List<Hit> boardHits = game.getBoards().get(myPosition).getHits();

      if (horizontal) {
        /*  Vízszintes szavak*/
        for (int row = 0; row < 6; row++) {
          Hit hit = null;

          for (Hit boardHit : boardHits) {
            if (boardHit.isHorizontal() && boardHit.getLine() == row) {
              hit = boardHit;
              break;
            }
          }
          if (hit == null) {
            hit = new Hit(true, row, -1, -1, 0, "");
          }
          hits.add(hit);
        }
      }

      if (vertical) {
        /* Függőleges szavak*/
        for (int col = 0; col < 6; col++) {
          Hit hit = null;

          for (Hit boardHit : boardHits) {
            if (!boardHit.isHorizontal() && boardHit.getLine() == col) {
              hit = boardHit;
              break;
            }
          }
          if (hit == null) {
            hit = new Hit(false, col, -1, -1, 0, "");
          }
          hits.add(hit);
        }
      }
    }
    return hits;
  }

  public String getRandomLettersString(int length){
    Game tmpgame = new Game(0, "random", true, true, true, true, Game.DEF_RANDOMPLACE, Game.DEF_RNDLETTERMODE,
                         Game.DEF_MINPLAYERS, Game.DEF_MAXPLAYERS, Game.DEF_TIMELIMIT, Game.DEF_SCORING_MODE);
    int sets = (length / 100) + 1;
    tmpgame.fillAvailableLetters(sets);

    StringBuilder letters = new StringBuilder("");
    Random rnd = new Random();
    int draw;

    for (int i = 0; i < length; i++) {
      letters.append(tmpgame.drawLetter()).append(".");
    }
    return letters.toString();
  }
  
  public String getSelectedLetter() {
    if (game != null) {
      return game.getSelectedLetter();
    }
    return "";
  }

  public String getPreselectedLetter() {
    if (game != null) {
      return game.getPreselectedLetter();
    }
    return "";
  }

  public String getBoardLetter(int row, int column) {
    String letter = "";
    if (game != null && game.getStartDate() != null) {
      letter = game.getBoards().get(myPosition).getLetters()[row][column];
    }
    return letter;
  }

  public String getPlacedBoardLetter(int row, int column) {
    if (game != null && game.getStartDate() != null) {
      Board board = game.getBoards().get(myPosition);
      if (board.getTurnPlaces()[row][column] != 0)
        return board.getLetters()[row][column];
    }
    return "";
  }

  public String getBoardLetter(int index) {
    return getBoardLetter(index / 6, index % 6);
  }

  public String getBoardLetterClass(int index, boolean finePointer) {
    String boardLetterClass = "";
    if (game != null) {
      if (game.getEndDate() != null) {
        if (getPlacedBoardLetter(index / 6, index % 6).isEmpty()) {
          boardLetterClass = "unplacedletterbutton";
        }
      }
      if (boardLetterClass.isEmpty()) {
        if (finePointer)
          boardLetterClass = "boardbutton";
        else {
          Board board = game.getBoards().get(myPosition);
          if (board.getMarkedRow() == index / 6 && board.getMarkedCol() == index % 6)
            boardLetterClass = "markedletterbutton";
          else
            boardLetterClass = "boardbutton_noptr";
        }
      }
    }
    return boardLetterClass;
  }

  public boolean hasBoardLetter(int row, int column) {
    return !getBoardLetter(row, column).isEmpty() && !getBoardLetter(row, column).equals(Board.MARKCHAR);
  }

  public boolean canPlaceBoardLetter(int row, int column) {
    if (myPosition < 0) {
      fillMyPosition();
    }
    int pls = getPlayerState();
    if (getPlayerState() == 1) {
      return !hasBoardLetter(row, column);
    } else {
      return false;
    }
  }

  public boolean canPlaceBoardLetter(int index) {
    return canPlaceBoardLetter(index / 6, index % 6);
  }

  public void placeLetter(int row, int column, boolean finePointer) {
    if (game != null && game.getStartDate() != null && getPlayerState() == 1) {
      game.placeLetter(myPosition, row, column, finePointer);
    }
  }

  public void placeLetter(int index, boolean finePointer) {
    placeLetter(index / 6, index % 6, finePointer);
  }

  public void selectLetter(String letter) {
    LOGGER.debug(loginData.getName() + " selects letter " + letter);
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.selectLetter(letter);
    }
  }

  public boolean canSelectLetter(String letter) {
    if (game != null && game.getStartDate() != null) {
      return game.isLetterAvailable(letter);
    }
    return false;
  }

  public void selectLetter(int letter, boolean finePointer) {
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      if (finePointer)
        game.selectLetter(letter);
      else
        game.preselectLetter(letter);
    }
  }

  public void revokeSelectedLetter() {
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.revokeLetter();
    }
  }

  public void confirmSelectedLetter() {
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.confirmLetter();
    }
  }

  public boolean canSelectLetter(int letter) {
    if (game != null && game.getStartDate() != null) {
      return game.isLetterAvailable(letter);
    }
    return false;
  }

  public String getLetterByIndex(int index) {
    return game.ALPHABET[index];
  }

  public void checkWord() {
    if (game != null && game.getStartDate() != null && !testWord.isEmpty()) {
      if (glossaryManager.includes(testWord, !game.isIncludeLongVowels()))
        testResult = 1;
      else {
        testResult = 2;
        missingWords.add(testWord);
      }  
    } else
      testResult = 0;
    LOGGER.debug(loginData.getName() + " wordcheck: " + testWord + " -> " + testResult);
  }

  public void clearWord() {
    testWord = "";
    testResult = 0;
  }

  public List<Integer> getAllNumOfPlayers() {
    return Arrays.asList(Game.NUM_OF_PLAYERS);
  }

  public List<Integer> getAllTimeLimits() {
    return Arrays.asList(Game.TIMELIMITS);
  }

  public List<ScoringMode> getAllScoringModes() {
    List<ScoringMode> modes = new ArrayList<>();
    for (ScoringMode value : ScoringMode.values()) {
      modes.add(value);
    }

    return modes;
  }

  public List<RndLetterMode> getAllRndLetterModes() {
    List<RndLetterMode> modes = new ArrayList<>();
    for (RndLetterMode value : RndLetterMode.values()) {
      modes.add(value);
    }
    return modes;
  }

  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  public void setGlossaryManager(GlossaryManager glossaryManager) {
    this.glossaryManager = glossaryManager;
  }

  public void setLoginData(LoginData loginData) {
    this.loginData = loginData;
  }

  public int getLobbyMode() {
    switch (menu) {
      case 1:
        return 1;
      case 2:
        return 2;
      case 3:
        return 3;
      default:
        return 0;
    }
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public int getMyPosition() {
    return myPosition;
  }

  public void setMyPosition(int myPosition) {
    this.myPosition = myPosition;
  }

  public List<Integer> getLetterIndices() {
    return letterIndices;
  }

  public void setLetterIndices(List<Integer> letterIndices) {
    this.letterIndices = letterIndices;
  }

  public List<Integer> getCellIndices() {
    return cellIndices;
  }

  public void setCellIndices(List<Integer> cellIndices) {
    this.cellIndices = cellIndices;
  }

  public String getTestWord() {
    return testWord;
  }

  public void setTestWord(String testWord) {
    this.testWord = testWord;
  }

  public int getTestResult() {
    return testResult;
  }

  public void setTestResult(int testResult) {
    this.testResult = testResult;
  }

  public Set<Integer> getFadingGames() {
    return fadingGames;
  }

  public void setFadingGames(Set<Integer> fadingGames) {
    this.fadingGames = fadingGames;
  }

  public Set<Integer> getFadedGames() {
    return fadedGames;
  }

  public void setFadedGames(Set<Integer> fadedGames) {
    this.fadedGames = fadedGames;
  }

  public Game getPrevGame() {
    return prevGame;
  }

  public void setPrevGame(Game prevGame) {
    this.prevGame = prevGame;
  }

  public int getGameViewMode() {
    return gameViewMode;
  }

  public void setGameViewMode(int gameViewMode) {
    this.gameViewMode = gameViewMode;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getMenu() {
    return menu;
  }

  public void setMenu(int menu) {
    visibleGameRules.clear();
    this.menu = menu;
  }

  public Set<Integer> getVisibleGameRules() {
    return visibleGameRules;
  }

  public void setVisibleGameRules(Set<Integer> visibleGameRules) {
    this.visibleGameRules = visibleGameRules;
  }

    public Set<String> getMissingWords() {
    return missingWords;
  }

  public void setMissingWords(Set<String> missingWords) {
    this.missingWords = missingWords;
  }

}
