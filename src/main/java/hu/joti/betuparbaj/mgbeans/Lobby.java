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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Player;
import java.util.Iterator;

/**
 *
 * @author Joti
 */
@ManagedBean
@ApplicationScoped
public class Lobby implements Serializable {

  public static final int FINISHEDGAMES_MAXNUM = 10;

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
  }

  public String getGamesInLobbyString() {
    String gamesString = ":";
    for (Game game : gamesInLobby) {
      gamesString += game.getGameSetupString() + ";";
    }
    return gamesString;
  }

  public void removePlayer(Player player) {
    Iterator iter = gamesInLobby.iterator();
    while (iter.hasNext()) {
      Game game = (Game) iter.next();
      game.removePlayer(player);
      if (game.getNumberOfPlayers() == 0) {
        iter.remove();
      }
    }

    iter = gamesInProgress.iterator();
    while (iter.hasNext()) {
      Game game = (Game) iter.next();
      game.removePlayer(player);
      if (game.getNumberOfActivePlayers() == 0) {
        iter.remove();
      }
    }
  }

  public void addToFinished(Game game) {
    gamesInProgress.remove(game);
    gamesFinished.add(game);
    if (gamesFinished.size() > FINISHEDGAMES_MAXNUM) {
      gamesFinished.remove(0);
    }
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
