/*
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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

  @ManagedProperty("#{loginData}")
  LoginData loginData;

  private Game game;
  private Set<Integer> fadingGames;
  private Set<Integer> fadedGames;
  private int myPosition;
  private List<Integer> letterIndices;
  private List<Integer> cellIndices;

  private static final Logger logger = Logger.getLogger(GameManager.class.getName());

  public GameManager() {
    fadingGames = new HashSet<>();
    fadedGames = new HashSet<>();
    logger.debug("New session");
    myPosition = -1;
    
    letterIndices = new ArrayList<>();
    for (int i = 0; i < Game.ALPHABET.length; i++) {
      letterIndices.add(i);
    }
    System.out.println("letterIndices.size="+letterIndices.size());

    cellIndices = new ArrayList<>();
    for (int i = 0; i < Board.BOARD_SIZE * Board.BOARD_SIZE; i++) {
      cellIndices.add(i);
    }
    System.out.println("cellIndices.size="+cellIndices.size());
    
  }

  @PreDestroy
  public void destroy() {
    System.out.println("PreDestroy method at " + (new Date()));
    if (game != null) {
      quitGame();
    }
  }

  public void refresh() {
    System.out.println(loginData.getName() + " refresh");
    loginData.refresh();

    if (lobby != null) {
      Set<Integer> gamesInLobby = new HashSet<>();
      int sec = loginData.getSeconds();

      for (Game g : lobby.getGamesInLobby()) {
        gamesInLobby.add(g.getId());

        if (g.getMaxPlayers() <= g.getNumberOfPlayers()) {
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
      System.out.println(game.getNextActivePlayerPos() + " - " + myPosition + ". " + loginData.getName() + " refresh: " + getTurnSec() + "/" + game.getTurnTimeLimit());

      // Ha mi vagyunk épp soron, vagy mi vagyunk a legközelebbi még aktív játékos (a soron lévő játékos már kilépett),
      // akkor szükség esetén elindítjuk a következő kört
      if (game.getNextActivePlayerPos() == myPosition) {
      
        // Rakott-e már mindenki betűt és a soron következő játékos kiválasztotta-e már a következő betűt?
        boolean needWait = false;
        int turn = game.getTurn();
        if (turn > 0){
          for (Board board : game.getBoards()) {
            if (board.getLetterCount() < game.getTurn()){
              needWait = true;
              break;
            }  
          }
        }  
        if (!needWait && game.getTurn() < 36){
          if (game.getSelectedLetters()[turn].isEmpty())
            needWait = true;
        }

        if (getTurnSec() > game.getTurnTimeLimit() || !needWait) {
          System.out.println(loginData.getName() + ": NEXTTURN");
          game.nextTurn();
          if (game.getEndDate() != null){
            lobby.getGamesInProgress().remove(game);
            lobby.getGamesFinished().add(game);
          }
        }
      }
    }

  }

  public boolean isGameFading(Game g) {
    if (fadingGames.contains(g.getId()) && !fadedGames.contains(g.getId())) {
      return true;
    } else {
      return false;
    }
  }

  public void fillMyPosition() {
    myPosition = game.getPlayerPos(loginData.getName());
  }

  public void debug(String text) {
    if (loginData != null)
      logger.debug(text + " (" + loginData.getName() + ")");
    else
      logger.debug(text);
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
    game = new Game(gameId, true, true, true, 1, 2, 4, 30);
    game.addPlayer(loginData.getName());
    myPosition = -1;
    System.out.println(game.getId());
    logger.info("Game #" + game.getId() + " created");
  }

  public void joinGame(Game g) {
    System.out.println("JOINGAME");
    System.out.println(g.getId());
    if (game == null && g.getOpenDate() != null && g.getMaxPlayers() > g.getBoards().size()) {
      game = g;
      game.addPlayer(loginData.getName());
      myPosition = -1;
    }
  }

  public void quitGame() {
    System.out.println("QUITGAME - " + loginData.getName());
    if (game != null) {
      System.out.println("activePlayers: " + game.getNumberOfActivePlayers());
      for (int i = 0; i < game.getBoards().size(); i++) {
        System.out.println(i + 1 + ". játékos kilépése: " + game.getBoards().get(i).getQuitDate());
      }
      game.removePlayer(loginData.getName());
      System.out.println(loginData.getName() + " removed from game " + game.getId());
      game = null;
    }
  }

  public void openGame() {
    if (game != null) {
      game.setOpenDate(new Date());
      lobby.getGamesInLobby().add(game);
    }
  }

  public void dropGame() {
    if (game != null) {
      lobby.getGamesInLobby().remove(game);
      lobby.getGamesInProgress().remove(game);
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
    
    System.out.println("GAME = NULL (GameExists())");
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

  public boolean canPlayerSelectLetterInTurn(int player){
    return (game != null && game.getDrawmode() == Game.PLAYER_DRAW && game.getTurn() < 36 && game.getCurrentPlayer() == player);
  }
  
  public boolean canPlayerSelectLetter(){
    return (getPlayerState() == 2);
  }

  public boolean canPlayerPlaceLetter(){
    return (getPlayerState() == 1);
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

    if (myPosition < 0){
      fillMyPosition();
    }
    
    int turn = game.getTurn();
    if (game.getBoards().get(myPosition).getLetterCount() < turn)
      // A játékos még nem rakta le az aktuális betűt
      return 1;

    if (turn < 36 && myPosition == game.getCurrentPlayer() && game.getSelectedLetters()[turn].isEmpty())
      return 2;

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
    if (game != null) {
      logger.debug(game.getStartDate());
      logger.debug(loginData.getName() + " GAMEHISTSTRING=" + gameHistString);
    }
    return gameHistString;
  }

  public String getGameMsg() {
    if (game == null || game.getStartDate() == null)
      return "";

    String msg;
    if (game.getEndDate() != null) {
      msg = "\n\n\n\nA játék véget ért.";
    } else {
      int turn = game.getTurn();
      int playerState = getPlayerState();
      String name = game.getBoards().get(game.getCurrentPlayer()).getName();

      if (turn == 0 && playerState == 2) {
        // A 0. körben a játékos választ betűt
        msg = "\n\nKérlek, válaszd ki\naz első betűt!";
      } else if (turn == 0 && playerState == 3) {
        // A 0. körben egy másik játékos választja az első betűt   
        msg = String.format("\n\n\n\n\n%s kiválasztja\naz első betűt.", name);
      } else if (playerState == 1) {
        // A kiválasztott betűt el kell helyezni a táblán
        String letter = game.getSelectedLetter();
        if (turn < 36 && myPosition == game.getCurrentPlayer())
          msg = String.format("\n\n\nA %d. betű:\n\n\n\n\n\n Helyezd el a táblán,\nmajd válaszd ki\na következő betűt!", turn);
        else
          msg = String.format("\n\n\nA %d. betű:\n\n\n\n\n\n Helyezd el a táblán!", turn);
      } else if (playerState == 2) {
        msg = "\n\nVálaszd ki\na következő betűt!";
      } else {
        msg = "\n\n\n\n\n\nVárakozás a többi játékosra...";
      }
    }
    return msg;
  }

  public void switchPlayers(int pos1, int pos2) {
    logger.debug("SWITCH " + pos1 + "-" + pos2);
    if (game != null && game.getNumberOfPlayers() > pos1 && game.getNumberOfPlayers() > pos2) {
      logger.debug("POS(" + pos1 + "): " + game.getBoards().get(pos1).getName());
      logger.debug("POS(" + pos2 + "): " + game.getBoards().get(pos2).getName());
      game.switchPlayers(pos1, pos2);
      logger.debug("POS(" + pos1 + "): " + game.getBoards().get(pos1).getName());
      logger.debug("POS(" + pos2 + "): " + game.getBoards().get(pos2).getName());
    }
  }

  public int getMyGamePos() {
    if (game != null) {
      for (int i = 0; i < game.getBoards().size(); i++) {
        if (game.getBoards().get(i).getName().equals(loginData.getName())) {
          return i;
        }
      }
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

  public int getTurn() {
    int turn = 0;
    if (game != null && game.getStartDate() != null) {
      if (game.getEndDate() != null) {
        turn = 37;
      } else {
        Date now = new Date();
        turn = (int) ((now.getTime() - game.getStartDate().getTime()) / 1000) / game.getTimeLimit() + 1;
        logger.debug("TURN=" + turn);
      }
    }
    return turn;
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
      if (rTurnSec < 0)
        rTurnSec = 0;
    }
    return rTurnSec;
  }

  public String getSelectedLetter(){
    if (game != null)
      return game.getSelectedLetter();
    return "";
  }
  
  public String getBoardLetter(int row, int column) {
    if (game != null && game.getStartDate() != null) {
      return game.getBoards().get(myPosition).getLetters()[row][column];
    }
    return "";
  }

  public String getBoardLetter(int index) {
    return getBoardLetter(index / 6, index % 6);
  }

  public boolean hasBoardLetter(int row, int column) {
    return !getBoardLetter(row, column).isEmpty();
  }

  public boolean canPlaceBoardLetter(int row, int column) {
    if (myPosition < 0) {
      fillMyPosition();
    }
    int pls = getPlayerState();
    if (getPlayerState() == 1)
      return !hasBoardLetter(row, column);
    else
      return false;
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
  
  public void selectLetter(String letter){
    System.out.println("selectletter: " + letter);
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.selectLetter(letter);
    }
  }
  
  public boolean canSelectLetter(String letter){
    if (game != null && game.getStartDate() != null)
      return game.isLetterAvailable(letter);
    return false;
  }

  public void selectLetter(int letter){
    System.out.println("selectletter: " + letter);
    if (game != null && game.getStartDate() != null && getPlayerState() == 2) {
      game.selectLetter(letter);
    }
  }
  
  public boolean canSelectLetter(int letter){
    if (game != null && game.getStartDate() != null)
      return game.isLetterAvailable(letter);
    return false;
  }
  
  public String getLetterByIndex(int index){
    return game.ALPHABET[index];
  }
  
  public List<Integer> getAllNumOfPlayers() {
    return Arrays.asList(Game.NUM_OF_PLAYERS);
  }

  public List<Integer> getAllTimeLimits() {
    return Arrays.asList(Game.TIMELIMITS);
  }

  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  public void setLoginData(LoginData loginData) {
    this.loginData = loginData;
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
  
}
