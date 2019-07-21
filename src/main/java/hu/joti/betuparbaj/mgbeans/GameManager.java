/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import hu.joti.betuparbaj.model.Game;
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

  private static final Logger logger = Logger.getLogger(GameManager.class.getName());

  public GameManager() {
    fadingGames = new HashSet<>();
    fadedGames = new HashSet<>();
    logger.debug("New session");
  }

  @PreDestroy
  public void destroy() {
    System.out.println("PreDestroy method at " + (new Date()));
    if (game != null) {
      quitGame();
    }
  }

  public void refresh() {
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
        if (!gamesInLobby.contains(gId)){
          if (fadingGames.remove(gId)) {
            logger.debug("Game " + gId + " is not in lobby any more at " + sec + " s");
          }
          fadedGames.remove(gId);
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

  public void debug(String text) {
    logger.debug(text);
  }

  public void login(){
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
    System.out.println(game.getId());
    logger.info("Game #" + game.getId() + " created");
  }

  public void joinGame(Game g) {
    System.out.println("JOINGAME");
    System.out.println(g.getId());
    if (game == null && g.getOpenDate() != null && g.getMaxPlayers() > g.getBoards().size()) {
      game = g;
      game.addPlayer(loginData.getName());
    }
  }

  public void quitGame() {
    System.out.println("QUITGAME");
    if (game != null) {
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
      game.setStartDate(new Date());
      lobby.getGamesInLobby().remove(game);
      lobby.getGamesInProgress().add(game);
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

  public String getTestString() {
    String ts = "";

    if (game != null) {
      for (Game g : lobby.getGamesInLobby()) {
        if (g.getId() == game.getId()) {
          ts = g.getTestString();
        }
      }
      logger.debug(loginData.getName() + " << " + ts);
    }
    return ts;
  }

  public void setTestString(String testString) {
    if (game != null) {
      logger.debug(loginData.getName() + " >> " + testString);
      for (Game g : lobby.getGamesInLobby()) {
        if (g.getId() == game.getId()) {
          g.setTestString(testString);
        }
      }
    }
  }

  public void appendToTestString() {
    if (game != null) {
      logger.debug("appendToTestString()");
      String testString = getTestString();
      logger.debug(testString);
      String append = testString.length() % 10 + "";
      logger.debug(append);
      setTestString(testString + append);
    }
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

  public String getGameSetupString() {
    if (game != null) {
      return game.getGameDataString();
    } else {
      return "";
    }
  }

  public String getGameSetupString2() {
    if (game != null) {
      for (Game g : lobby.getGamesInLobby()) {
        if (g.getId() == game.getId()) {
          return g.getGameDataString();
        }
      }

      return "";
    } else {
      return "";
    }
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

}
