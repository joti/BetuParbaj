package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.comparators.FinishedGameComparator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Player;
import java.util.Iterator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Joti
 */
@ManagedBean
@ApplicationScoped
public class Lobby implements Serializable {

  private static final Logger LOGGER = LogManager.getLogger(Lobby.class.getName());

  public static final int FINISHEDGAMES_MAXNUM = 10;

  private List<Game> gamesInPrep;
  private List<Game> gamesInLobby;
  private List<Game> gamesInProgress;
  private List<Game> gamesFinished;

  private int lastId;
  private LocalDate lastGameDate;
  private int dailyCounter;

  public Lobby() {
    gamesInPrep = new ArrayList<>();
    gamesInLobby = new ArrayList<>();
    gamesInProgress = new ArrayList<>();
    gamesFinished = new ArrayList<>();

    Date curTime = new Date();
    Date startDate = new Date(119, 3, 21);
    lastId = (int) ((curTime.getTime() - startDate.getTime()) / 1000);
    lastGameDate = LocalDate.now();
    dailyCounter = 0;
    LOGGER.debug("lastId: " + lastId);
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

    Collections.sort(gamesFinished, new FinishedGameComparator());
    for (Game game1 : gamesFinished) {
      System.out.println(game1.getId() + " - " + game1.getName());
    }
    
    while (gamesFinished.size() > FINISHEDGAMES_MAXNUM) {
      gamesFinished.remove(FINISHEDGAMES_MAXNUM);
    }
  }

  public int getGameId() {
    lastId++;
    return lastId;
  }

  public synchronized String getDefGameName() {
    LocalDate today = LocalDate.now();
    if (!today.isEqual(lastGameDate)){
      lastGameDate = today;
      dailyCounter = 0;
    }
    dailyCounter++;
    
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYMMdd");
    return dtf.format(lastGameDate) + "/" + dailyCounter;
  }

  public List<Game> getGamesInPrep() {
    return gamesInPrep;
  }

  public void setGamesInPrep(List<Game> gamesInPrep) {
    this.gamesInPrep = gamesInPrep;
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

  public LocalDate getLastGameDate() {
    return lastGameDate;
  }

  public void setLastGameDate(LocalDate lastGameDate) {
    this.lastGameDate = lastGameDate;
  }

  public int getDailyCounter() {
    return dailyCounter;
  }

  public void setDailyCounter(int dailyCounter) {
    this.dailyCounter = dailyCounter;
  }
  
}
