package hu.joti.betuparbaj.mgbeans;

import java.io.Serializable;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Message;
import hu.joti.betuparbaj.model.Player;
import hu.joti.betuparbaj.model.RndLetterMode;
import hu.joti.betuparbaj.model.ScoringMode;
import java.util.HashSet;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@ApplicationScoped
public class ChatRoom implements Serializable {

  @ManagedProperty("#{lobby}")
  Lobby lobby;

  private static final Logger LOGGER = LogManager.getLogger(ChatRoom.class.getName());
  
  public static final boolean ONE_NAME_PER_ROW = true;
  public static final String[] TESTPLAYERS = {"Aromó", "Nagy Zoárd", "Mikkamakka", "Bruckner Szigfrid", "Vacskamati", "Maminti", "Dömdödöm"};
  public static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

  private Set<Player> players;
  private List<Message> messages;

  private transient ScheduledExecutorService scheduler;
  private int schedCount;
 
  public ChatRoom() {
    players = new TreeSet<>();
    messages = new ArrayList<>();
    SDF.setTimeZone(TimeZone.getTimeZone("Europe/Budapest"));
  }

  @PostConstruct
  public void init() {
    // teszteléshez felvehetünk pár kamu usert
    // addTestPlayers();
    
    // és velük néhány dummy asztalt is
    // addTestGamesToLobby(2,1);
    
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(new Runnable(){
      @Override
      public void run() {
        schedCount++;
        
        String msg = "Inactivity check #" + schedCount + " - " + players.size() + " players";
        if (schedCount % 180 == 0) // félóránként 
          LOGGER.info(msg);
        else
          LOGGER.debug(msg);
        
        /* Az inaktív usereket kiléptetjük */
        removeInactivePlayers();
      }
    }, 10, 10, TimeUnit.SECONDS);
  }
  
  @PreDestroy
  public void destroy() {
    scheduler.shutdownNow();
  }   
  
  private void addTestPlayers() {
    for (String name : TESTPLAYERS) {
      Player pl = new Player(name);
      players.add(pl);
    }
  }

  private void addTestGamesToLobby(int count, int full) {
    int gameId;
    String gameName;
    Game game;
    Random rnd = new Random();
    String playerName;
    int maxPlayers;
    int numOfPlayers;
    int timeLimit;
    int scoringModeIndex;
    ScoringMode scoringMode = ScoringMode.SQUARE;
    int fullcount = 0;
    Set<String> gamePlayers = new HashSet<>();

    for (int i = 0; i < count; i++) {
      gameId = lobby.getGameId();
      gameName = "#" + gameId;
      maxPlayers = Game.NUM_OF_PLAYERS[rnd.nextInt(Game.NUM_OF_PLAYERS.length)];
      if (fullcount < full){
        numOfPlayers = maxPlayers;
        fullcount++;
      } else {
        numOfPlayers = rnd.nextInt(maxPlayers) + 1;
      }  
      timeLimit = Game.TIMELIMITS[rnd.nextInt(Game.TIMELIMITS.length)];

      scoringModeIndex = rnd.nextInt(ScoringMode.values().length);
      for (ScoringMode value : ScoringMode.values()) {
        if (value.ordinal() == scoringModeIndex){
          scoringMode = value;
          break;
        }
      }

      game = new Game(gameId, gameName, rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean(), rnd.nextBoolean(), RndLetterMode.NORNDLETTER, 2, maxPlayers, timeLimit, scoringMode);

      gamePlayers.clear();
      for (int j = 0; j < numOfPlayers; j++) {
        do {
          playerName = TESTPLAYERS[rnd.nextInt(TESTPLAYERS.length)];
        } while (gamePlayers.contains(playerName));
        game.addPlayer(getPlayer(playerName));
        gamePlayers.add(playerName);
      }
      
      lobby.getGamesInLobby().add(game);
      game.setOpenDate(new Date());
    }
  }
  
  public void addPlayer(String name, Date loginTime) {
    Player player = new Player(name, loginTime);
    players.add(player);

    Message m = new Message(name, "$ENTER");
    m.setTime(loginTime);
    messages.add(m);
    Collections.sort(messages);
  }

  public void removePlayer(String name) {
    Player player = getPlayer(name);
    if (player != null){
      removePlayer(player);
    }
  }

  public void removePlayer(Player player){
    if (players.contains(player)){
      players.remove(player);    

      Message m = new Message(player.getName(), "$EXIT");
      m.setTime(new Date());
      getMessages().add(m);
      Collections.sort(getMessages());

      LOGGER.info(player.getName() + " removed");
    }
  }
  
  public void removeInactivePlayers() {
    Set<Player> playersToRemove = new HashSet<>();
    
    for (Player player : players) {
      if (!player.isActive()) {
        // Minden játékból kiléptetjük
        lobby.removePlayer(player);
        
        // És a chatről is
        playersToRemove.add(player);
      }
    }
    
    for (Player player : playersToRemove) {
      removePlayer(player);
    }
  }

  public void playerAccess(String name) {
    for (Player player : players) {
      if (player.getName().equals(name)) {
        player.setLastAccess(new Date());
        break;
      }
    }
  }

  public Set<String> getPlayerNames() {
    Set<String> names = new TreeSet<>();
    for (Player player : players) {
      names.add(player.getName());
    }
    return names;
  }

  public Player getPlayer(String name){
    for (Player player : players) {
      if (player.getName().equals(name)) {
        return player;
      }
    }
    return null;    
  }
  
  public String getNameList(String myName) {
    String namesString = "";
    List<String> namesList = new ArrayList<>();

    for (Player player : players) {
      namesList.add(player.getName());
    }

    Collections.sort(namesList, Collator.getInstance(new Locale("hu", "HU")));

    for (String name : namesList) {
      if (!namesString.isEmpty()) {
        if (ONE_NAME_PER_ROW) {
          namesString += "\n";
        } else {
          namesString += " \u00B7 ";
        }
      }
      namesString += name;
      if (name.equals(myName))
        namesString += " (én)";
    }
    return namesString;
  }

  public String getMessageList(Date time) {
    String messageList = "";
    for (Message m : messages) {
      if (m.getTime().compareTo(time) >= 0)
        messageList += m.getFormattedMessage(SDF) + "\n";
    }

    return messageList;
  }

  public Locale getLocale() {
    return new Locale("hu", "HU");
  }

  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  public Set<Player> getPlayers() {
    return players;
  }

  public void setPlayers(Set<Player> players) {
    this.players = players;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

}
