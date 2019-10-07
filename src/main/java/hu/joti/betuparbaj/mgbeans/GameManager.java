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
import hu.joti.betuparbaj.model.ScoringMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@SessionScoped
public class GameManager implements Serializable {

  @ManagedProperty("#{lobby}")
  Lobby lobby;

  @ManagedProperty("#{glossary}")
  Glossary glossary;

  @ManagedProperty("#{loginData}")
  LoginData loginData;

  private Game game;
  private Game prevGame;
  private Set<Integer> fadingGames;
  private Set<Integer> fadedGames;
  private String password;
  private int myPosition;

  // játékok listázásának módjai: 1 - megnyitott játékok, 2 - folyamatban lévő játékok, 3 - befejeződött játékok
  private int lobbyMode;
  // befejeződött játék lekérdezésének módja: 1 - eredmény, 2 - setup
  private int gameViewMode;
  
  private List<Integer> letterIndices;
  private List<Integer> cellIndices;
  private String testWord;
  private int testResult;
  
  private static final Logger logger = Logger.getLogger(GameManager.class.getName());

  public GameManager() {
    fadingGames = new HashSet<>();
    fadedGames = new HashSet<>();
    logger.debug("New GM session");
    myPosition = -1;
    lobbyMode = 1;
    gameViewMode = 0;
    password = "";

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
    System.out.println("GameManager.PostConstruct method at " + (new Date()));
    logger.info("Lobby null: " + (lobby == null));
    logger.info("LoginData null: " + (lobby == null));
    logger.info("Glossary null: " + (glossary == null));
    if (glossary != null) {
      logger.info(glossary.getWords() == null);
      logger.info("Szótárméret: " + glossary.getWords().size() + " db szó");
    }

    if (game == null) {
      logger.info("GameManager session starts, game is null");
    } else {
      logger.info("GameManager session starts, game id = " + game.getId());
    }
  }

  @PreDestroy
  public void destroy() {
    logger.info("GameManager session ends for " + loginData.getName());
    System.out.println("GameManager.PreDestroy method at " + (new Date()));
    if (game != null) {
      quitGame();
    }
  }

  public void refresh() {
    loginData.refresh();

    if (!loginData.isEntered()) {
      quitGame();
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
              logger.debug("Game " + g.getId() + " is fading at " + sec + " s");
            } else {
              fadedGames.add(g.getId());
              logger.debug("Game " + g.getId() + " is already faded at " + sec + " s");
            }
          }
        } else {
          if (fadingGames.remove(g.getId())) {
            logger.debug("Game " + g.getId() + " is not fading any more at " + sec + " s");
          }
          fadedGames.remove(g.getId());
        }
      }

      for (Integer gId : fadingGames) {
        if (!gamesInLobby.contains(gId)) {
          if (fadingGames.remove(gId)) {
            logger.debug("Game " + gId + " is not in lobby any more at " + sec + " s");
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

        // Rakott-e már mindenki betűt?
        boolean needWait = false;
        int turn = game.getTurn();
        if (turn > 0) {
          for (Board board : game.getBoards()) {
            // Kilépett játékosra nem várakozunk
            if (board.getTotalLetterCount() < game.getTurn() && board.getQuitDate() == null) {
              needWait = true;
              break;
            }
          }
        }
        
        // A soron következő játékos kiválasztotta-e már a következő betűt?
        if (!needWait && game.getTurn() < 36 && game.getDrawmode() == Game.PLAYER_DRAW
                && game.getBoards().get(game.getCurrentPlayer()).getQuitDate() == null) {
          if (game.getSelectedLetters()[turn].isEmpty()) {
            needWait = true;
          }
        }

        if (getTurnSec() > game.getTurnTimeLimit() || !needWait) {
          System.out.println(loginData.getName() + ": NEXTTURN");
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

  public void evaluateGame() {
    System.out.println("EvaluateGame: " + game.getTurn());
    if (game != null && game.getTurn() == 36) {
      for (Board board : game.getBoards()) {
        System.out.println("Board: " + board.getPlayer().getName());
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
          Hit hit = glossary.findHit(streak, game.isEasyVowelRule(), game.getScoringMode());
          if (hit != null) {
            logger.info(board.getPlayer().getName() + " szava: " + hit.getWord());
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
          Hit hit = glossary.findHit(streak, game.isEasyVowelRule(), game.getScoringMode());
          if (hit != null) {
            logger.info(board.getPlayer().getName() + " szava: " + hit.getWord());
            hit.setHorizontal(false);
            hit.setLine(col);
            board.getHits().add(hit);
          }
        }

        logger.info(board.getPlayer().getName() + " szavainak száma: " + board.getHits().size());
        board.evaluate();
        logger.info(board.getPlayer().getName() + " pontszáma: " + board.getScore());
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
    switch (lobbyMode) {
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
    for (Game g : games) {
      gamesString += g.getGameSetupString() + ";";
    }
    gamesString += (loginData.getSeconds() / 60) + "";
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
      logger.debug(text + " (" + loginData.getName() + ")");
    } else {
      logger.debug(text);
    }
  }

  public void login() {
    loginData.doLogin();

    for (Game g : lobby.getGamesInLobby()) {
      if (g.getMaxPlayers() <= g.getNumberOfPlayers()) {
        fadingGames.add(g.getId());
        fadedGames.add(g.getId());
        logger.debug("Game " + g.getId() + " is already faded at 0 s");
      }
    }
  }

  public void createGame() {
    int gameId = lobby.getGameId();
    
    String name = lobby.getDefGameName();
    logger.info(loginData.getName() + " creates game #" + gameId + " with name " + name);
    
    /* A játékos legutóbbi asztalának beállításaival indítunk */
    if (prevGame != null){
      game = new Game(gameId, name, prevGame.isEasyVowelRule(), prevGame.isNoDigraph(), prevGame.isIncludeX(), prevGame.isIncludeY(), prevGame.isRandomPlace(), prevGame.getDrawmode(), 
                      prevGame.getMinPlayers(), prevGame.getMaxPlayers(), prevGame.getTimeLimit(), prevGame.getScoringMode());
    } else {
      game = new Game(gameId, name, Game.DEF_EASYVOWELRULE, Game.DEF_NODIGRAPH, Game.DEF_INCLUDEX, Game.DEF_INCLUDEY, Game.DEF_RANDOMPLACE, Game.DEF_DRAWMODE, 
                      Game.DEF_MINPLAYERS, Game.DEF_MAXPLAYERS, Game.DEF_TIMELIMIT, Game.DEF_SCORING_MODE);
    }  
    
    game.addPlayer(loginData.getPlayer());
    myPosition = -1;
    gameViewMode = 0;
    password = "";
    lobby.getGamesInPrep().add(game);
    logger.info("Game #" + game.getId() + " created");
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
    System.out.println("JOINGAME");
    if (game == null && g != null && g.getOpenDate() != null && g.getEndDate() == null) {
      logger.info(loginData.getName() + " joins game #" + g.getId());
      game = g;
      game.addPlayer(loginData.getPlayer());
      fillMyPosition();
      clearWord();
      gameViewMode = 0;
      password = "";
    }
  }

  public boolean canViewGame(Game g) {
    return (game == null && g.getEndDate() != null);
  }

  public void viewGame(Game g) {
    logger.info(loginData.getName() + " views game #" + g.getId());
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

  public void switchGameViewMode(){
    if (gameViewMode == 1)
      gameViewMode = 2;
    else if (gameViewMode == 2)
      gameViewMode = 1;
  }
  
  public void quitGame() {
    logger.info(loginData.getName() + " quits game #" + game.getId());
    if (game != null) {
      if (game.getEndDate() == null){
        game.removePlayer(loginData.getName());
        logger.info(loginData.getName() + " removed from game #" + game.getId());
      }  
      clearWord();
      prevGame = game;
      game = null;
    }
  }

  public void openGame() {
    if (game != null) {
      game.setOpenDate(new Date());
      lobby.getGamesInPrep().remove(game);
      lobby.getGamesInLobby().add(game);
      clearWord();
      password = password.trim();
    }
  }

  public void dropGame() {
    if (game != null) {
      clearWord();
      lobby.getGamesInPrep().remove(game);
      lobby.getGamesInLobby().remove(game);
      lobby.getGamesInProgress().remove(game);
      prevGame = game;
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
    System.out.println("quit");
    if (game != null) {
      quitGame();
    }
    loginData.doLogout();
  }

  public void startGame() {
    if (game != null) {
      game.start();
      lobby.getGamesInLobby().remove(game);
      lobby.getGamesInProgress().add(game);
      fillMyPosition();
    }
  }

  public void refresh(int source) {
    System.out.println(source);
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

  public boolean isPlayerTurnOn(int player) {
    if (game == null)
      return false;

    if (game.getStartDate() == null || game.getEndDate() != null)
      return false;

    if (game.getTurn() == 0 && game.getCurrentPlayer() != player)
      return false;
    else
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
      if (turn < 36 && game.getCurrentPlayer() == player) {
        if (!game.getSelectedLetters()[turn].isEmpty()) {
          return true;
        }
      } else if (turn > 0) {
        if (game.getBoards().get(player).getTotalLetterCount() >= turn) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean canPlayerSelectLetterInTurn(int player) {
    return (game != null && game.getDrawmode() == Game.PLAYER_DRAW && game.getTurn() < 36 && game.getCurrentPlayer() == player);
  }

  public boolean canPlayerSelectLetter() {
    return (getPlayerState() == 2);
  }

  public boolean canPlayerPlaceLetter() {
    return (getPlayerState() == 1);
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
       3 - a játékos a többi játékosra vár
     */
    if (getGameState() != 3) {
      return 0;
    }

    if (myPosition < 0) {
      fillMyPosition();
    }

    int turn = game.getTurn();
    System.out.println("GETPLAYERSTATE - " + loginData.getName() + ": turn = " + turn + ", total = " + game.getBoards().get(myPosition).getTotalLetterCount() + 
                       ", PLACED = " + game.getBoards().get(myPosition).getLetterCount() + ", UNPLACED = " + game.getBoards().get(myPosition).getUnplacedLetters().size() +
                       " (" + loginData.getSeconds() + " s)");
    if (game.getBoards().get(myPosition).getTotalLetterCount() < turn) // A játékos még nem rakta le az aktuális betűt
    {
      return 1; 
    }

    if (turn < 36 && myPosition == game.getCurrentPlayer() && game.getSelectedLetters()[turn].isEmpty() && game.getDrawmode() == Game.PLAYER_DRAW) {
      return 2;
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

      return "";
    } else {
      return "";
    }
  }

  public String getGameHistString() {
    String gameHistString = "";
    if (game != null && game.getStartDate() != null) {
      gameHistString = game.getGameHistString() + "->" + getPlayerState();
    }
    return gameHistString;
  }

  public String getBoardHitsString(){
    if (game != null && game.getEndDate() != null && myPosition >= 0){
      Board board = game.getBoards().get(myPosition);
      return board.getHitsString();
    }
    return "";
  }
  
  public String getGameMsg() {
    if (game == null || game.getStartDate() == null) {
      return "";
    }

    String msg;
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
      } else {
        msg = String.format("\n\n\n\nA játék véget ért.\n\nA győztes:\n\n%s", name);
      }
    } else {
      int turn = game.getTurn();
      int playerState = getPlayerState();
      name = game.getBoards().get(game.getCurrentPlayer()).getPlayer().getName();

      if (turn == 0 && playerState == 2) {
        // A 0. körben a játékos választ betűt
        msg = "\n\nKérlek, válaszd ki\naz első betűt!";
      } else if (turn == 0 && playerState == 3) {
        // A 0. körben egy másik játékos választja az első betűt   
        msg = String.format("\n\n\n\n\n%s kiválasztja\naz első betűt.", name);
      } else if (playerState == 1) {
        // A kiválasztott betűt el kell helyezni a táblán
        String letter = game.getSelectedLetter();
        if (turn < 36 && myPosition == game.getCurrentPlayer() && game.getDrawmode() == Game.PLAYER_DRAW) {
          msg = String.format("\n\n\nA %d. betű:\n\n\n\n\n\n Helyezd el a táblán,\nmajd válaszd ki\na következő betűt!", turn);
        } else {
          msg = String.format("\n\n\nA %d. betű:\n\n\n\n\n\n Helyezd el a táblán!", turn);
        }
      } else if (playerState == 2) {
        msg = "\n\nVálaszd ki\na következő betűt!";
      } else {
        msg = "\n\n\n\n\n\nVárakozás a többi játékosra...";
      }
    }
    return msg;
  }

  public void swapPlayers(int pos1, int pos2) {
    logger.debug("SWAP " + pos1 + "-" + pos2);
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

  public String getSelectedLetter() {
    if (game != null) {
      return game.getSelectedLetter();
    }
    return "";
  }

  public String getBoardLetter(int row, int column) {
    if (game != null && game.getStartDate() != null) {
      return game.getBoards().get(myPosition).getLetters()[row][column];
    }
    return "";
  }

  public String getPlacedBoardLetter(int row, int column) {
    if (game != null && game.getStartDate() != null) {
      Board board = game.getBoards().get(myPosition);
      if (board.getTurnPlaces()[row][column] > 0)
        return board.getLetters()[row][column];
    }
    return "";
  }

  public String getBoardLetter(int index) {
    return getBoardLetter(index / 6, index % 6);
  }
  
  public String getBoardLetterClass(int index) {
    if (game != null && game.getEndDate() != null){
      if (getPlacedBoardLetter(index / 6, index % 6).isEmpty()){
        return "unplacedletterbutton";
      }
    }
    return "boardbutton";
  }

  public boolean hasBoardLetter(int row, int column) {
    return !getBoardLetter(row, column).isEmpty();
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

  public void placeLetter(int row, int column) {
    System.out.println("placeletter:" + row + "/" + column);
    if (game != null && game.getStartDate() != null && getPlayerState() == 1) {
      game.placeLetter(myPosition, row, column);
    }
  }

  public void placeLetter(int index) {
    placeLetter(index / 6, index % 6);
  }

  public void selectLetter(String letter) {
    System.out.println("selectletter: " + letter);
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

  public void selectLetter(int letter) {
    System.out.println("selectletter: " + letter);
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.selectLetter(letter);
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
//    testResult = (testResult + 1) % 3;

    if (game != null && game.getStartDate() != null && !testWord.isEmpty()) {
      if (glossary.includes(testWord, game.isEasyVowelRule()))
        testResult = 1;
      else
        testResult = 2;
    } else
      testResult = 0;
    System.out.println("CheckWord: " + testWord + " -> " + testResult);
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
    for (int i = 0; i < Game.SCORING_MODES.length; i++) {
      modes.add(new ScoringMode(i, Game.SCORING_MODES[i]));
    }
    return modes;
  }

  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  public void setGlossary(Glossary glossary) {
    this.glossary = glossary;
  }

  public void setLoginData(LoginData loginData) {
    this.loginData = loginData;
  }

  public int getLobbyMode() {
    return lobbyMode;
  }

  public void setLobbyMode(int lobbyMode) {
    this.lobbyMode = lobbyMode;
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

}
