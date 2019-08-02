/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import hu.joti.betuparbaj.model.Game;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Joti
 */
@ManagedBean
@ApplicationScoped
public class Lobby implements Serializable {

  private List<Game> gamesInLobby;
  private List<Game> gamesInProgress;
  private List<Game> gamesFinished;

  private int lastId;

  public Lobby() {
    gamesInLobby = new ArrayList<>();
    gamesInProgress = new ArrayList<>();
    gamesFinished = new ArrayList<>();

    Date curTime = new Date();
    Date startDate = new Date(119, 3, 21);
    lastId = (int) ((curTime.getTime() - startDate.getTime()) / 1000);
    System.out.println("lastId: " + lastId);

    // teszthez dummy asztalok létrehozása
    addTestGamesToLobby(3,1);
  }

  private void addTestGamesToLobby(int count, int full) {
    int gameId;
    Game game;
    Random rnd = new Random();
    String playerName;
    int maxPlayers;
    int numOfPlayers;
    int timeLimit;
    int fullcount = 0;
    Set<String> players = new HashSet<>();

    for (int i = 0; i < count; i++) {
      gameId = getGameId();
      maxPlayers = Game.NUM_OF_PLAYERS[rnd.nextInt(Game.NUM_OF_PLAYERS.length)];
      if (fullcount < full){
        numOfPlayers = maxPlayers;
        fullcount++;
      } else {
        numOfPlayers = rnd.nextInt(maxPlayers) + 1;
      }  
      timeLimit = Game.TIMELIMITS[rnd.nextInt(Game.TIMELIMITS.length)];

      game = new Game(gameId, rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean(), 1, 2, maxPlayers, timeLimit);

      players.clear();
      for (int j = 0; j < numOfPlayers; j++) {
        do {
          playerName = Game.TESTPLAYERS[rnd.nextInt(Game.TESTPLAYERS.length)];
        } while (players.contains(playerName));
        game.addPlayer(playerName);
        players.add(playerName);
      }
      
      gamesInLobby.add(game);
      game.setOpenDate(new Date());
    }
  }

  public String getGamesInLobbyString(){
    String gamesString = ":";
    for (Game game : gamesInLobby) {
      gamesString += game.getGameSetupString() + ";";
    }
    return gamesString;
  }
  
  public int getGameId() {
    lastId++;
    return lastId;
  }

  public List<Game> getGamesInLobby() {
    return gamesInLobby;
  }

  public void setGamesInLobby(List<Game> gamesInLobby) {
    this.gamesInLobby = gamesInLobby;
  }

  public List<Game> getGamesInProgress() {
    return gamesInProgress;
  }

  public void setGamesInProgress(List<Game> gamesInProgress) {
    this.gamesInProgress = gamesInProgress;
  }

  public List<Game> getGamesFinished() {
    return gamesFinished;
  }

  public void setGamesFinished(List<Game> gamesFinished) {
    this.gamesFinished = gamesFinished;
  }

  public int getLastId() {
    return lastId;
  }

  public void setLastId(int lastId) {
    this.lastId = lastId;
  }

}
