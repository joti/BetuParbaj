/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Joti
 */
public class Game implements Serializable {

  public static final Integer[] NUM_OF_PLAYERS = {2, 3, 4};
  public static final Integer[] TIMELIMITS = {20, 30, 45, 60, 90, 120};

  public static final int PLAYER_DRAW = 1;
  public static final int RANDOM_DRAW = 2;

  public final static String[] TESTPLAYERS = {"Pali","Sanyiarettenthetetlensanyi","Fecó","Bruckner Szigfrid","Szilveszter","Juliska","Mariska","Ákóisz Igor","LevenGyula","Ősember","Maci Laci","Róbert Gida","Mekk Elek","szigfrid"};
  private final static int ALPHABETSETTINGSSTRING_MODE = 2;
  
  private int id;
  private List<Board> boards;

  private boolean easyVowelRule;
  private boolean noDigraph;
  private boolean includeY;
  private int drawmode;
  private int minPlayers;
  private int maxPlayers;
  private int numberOfPlayers;
  private int adminPlayerPos;
  private boolean randomOrder;
  private int timeLimit;
  private Date openDate; // A játék elérhetővé válik a lobbyban, játékosok csatlakozására vár
  private Date startDate; // A játék elindul
  private Date endDate; // A játék véget ér

  private String testString;
  
  public Game() {
    boards = new ArrayList<>();
    numberOfPlayers = 0;
    testString = "";
  }

  public Game(int id, boolean easyVowelRule, boolean noDigraph, boolean includeY, int drawmode, int minPlayers, int maxPlayers, int timeLimit) {
    this.id = id;
    this.easyVowelRule = easyVowelRule;
    this.noDigraph = noDigraph;
    this.includeY = includeY;
    this.drawmode = drawmode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    boards = new ArrayList<>();
    numberOfPlayers = 0;
    testString = "";
  }

  public Game(boolean easyVowelRule, boolean noDigraph, boolean includeY, int drawmode, int minPlayers, int maxPlayers, int timeLimit) {
    this.easyVowelRule = easyVowelRule;
    this.noDigraph = noDigraph;
    this.includeY = includeY;
    this.drawmode = drawmode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    boards = new ArrayList<>();
    numberOfPlayers = 0;
    testString = "";
  }

  public void addPlayer(String name) {
    Board board = new Board(name);
    boards.add(board);
    numberOfPlayers++;
    if (boards.size() == 1)
      setAdminPlayer();
  }

  public void removePlayer(String name) {
    Iterator iter = boards.iterator();
    
    while (iter.hasNext()){
      Board board = (Board)iter.next();
      if (board.getName().equals(name)) {
        iter.remove();
        numberOfPlayers--;
        break;
      }
    }
    setAdminPlayer();
  }

  public void switchPlayers(int pos1, int pos2){
    Board board1 = boards.get(pos1);
    boards.set(pos1, boards.get(pos2));
    boards.set(pos2, board1);
    setAdminPlayer();
  }
  
  public String getPlayerName(Long pos){
    int playerpos = (int)(long)pos;
    if (boards.size() > pos){
      return boards.get(playerpos).getName();
    }
    if (playerpos >= minPlayers)
      return "..........?";
    else  
      return "..........!";
  }
  
  public void decMinPlayers() {
    if (minPlayers > 2) {
      minPlayers--;
    }
  }

  public void incMinPlayers() {
    if (minPlayers < 4) {
      minPlayers++;
      if (maxPlayers < minPlayers) {
        maxPlayers = minPlayers;
      }
    }
  }

  public void decMaxPlayers() {
    if (maxPlayers > 2) {
      maxPlayers--;
      if (minPlayers > maxPlayers) {
        minPlayers = maxPlayers;
      }
    }
  }

  public void incMaxPlayers() {
    if (maxPlayers < 4) {
      maxPlayers++;
    }
  }

  public void start() {
    startDate = new Date();
  }

  public String getSettingsString() {
    String settings = "mgh: ";
    if (easyVowelRule) {
      settings += "Ü";
    } else {
      settings += "Ű";
    }
    return settings;
  }

  public String getAlphabetSettingsString() {
    String settings;
    if (ALPHABETSETTINGSSTRING_MODE == 1){
      settings = "A,Á,B,C,D," + "\u2026" + " ";
      if (!easyVowelRule) {
        settings += "+Í,Ó," + "\u2026" + " ";
      }
      if (!noDigraph){
        settings += "+CS,GY,"+ "\u2026" + " ";
      }
      if (includeY){
        settings += "+Y ";
      }
    } else {  
      settings = "A,Á,B,C,";
      if (!noDigraph){
        settings += "CS,";
      } else {
        settings += "D,";
      }
      settings += "...,";
      if (easyVowelRule) {
        settings += "U,Ü,";
      } else {
        settings += "U,Ú,Ü,Ű,";
      }
      settings += "V,";
      if (includeY){
        settings += "Y,";
      }
      if (!noDigraph){
        settings += "Z,ZS";
      } else {
        settings += "Z";
      }
    }  
    return settings;
  }
  
  public String getCreator() {
    if (boards.size() > 0) {
      return boards.get(0).getName();
    } else {
      return "";
    }
  }

  public String getPlayersString() {
    String players = "";
    for (int i = 0; i < maxPlayers; i++) {
      if (!players.isEmpty()) {
        players += " / ";
      }
      if (boards.size() > i) {
        players += boards.get(i).getName();
      } else {
        players += "(?)";
      }
    }
    return players;
  }

  public String getGameDataString(){
    return id + ":" + getPlayersString() + ":" + adminPlayerPos + ":" + getAlphabetSettingsString() + ":" 
           + ":" + drawmode + ":" + timeLimit + "." + (randomOrder?"+":"-") ;
  }

  public void setAdminPlayer() {
    Date minDate = new Date();
    Date joinDate;
    int pos = 0;
    
    for (int i = 0; i < boards.size(); i++) {
      joinDate = boards.get(i).getJoinDate();
      if (joinDate.before(minDate)){
        minDate = joinDate;
        pos = i;
      }
    }
    System.out.println("Adminplayerpos: " + pos);
    adminPlayerPos = pos;
  }
  
  public List<Board> getBoards() {
    return boards;
  }

  public void setBoards(List<Board> boards) {
    this.boards = boards;
    this.numberOfPlayers = boards.size();
  }

  public boolean isEasyVowelRule() {
    return easyVowelRule;
  }

  public void setEasyVowelRule(boolean easyVowelRule) {
    this.easyVowelRule = easyVowelRule;
  }

  public int getDrawmode() {
    return drawmode;
  }

  public void setDrawmode(int drawmode) {
    this.drawmode = drawmode;
  }

  public boolean isNoDigraph() {
    return noDigraph;
  }

  public void setNoDigraph(boolean noDigraph) {
    this.noDigraph = noDigraph;
  }

  public boolean isIncludeY() {
    return includeY;
  }

  public void setIncludeY(boolean includeY) {
    this.includeY = includeY;
  }

  public int getMinPlayers() {
    return minPlayers;
  }

  public void setMinPlayers(int minPlayers) {
    this.minPlayers = minPlayers;
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  public void setNumberOfPlayers(int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
  }
  
  public boolean isRandomOrder() {
    return randomOrder;
  }

  public void setRandomOrder(boolean randomOrder) {
    this.randomOrder = randomOrder;
  }

  public int getTimeLimit() {
    return timeLimit;
  }

  public void setTimeLimit(int timeLimit) {
    this.timeLimit = timeLimit;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getOpenDate() {
    return openDate;
  }

  public void setOpenDate(Date openDate) {
    this.openDate = openDate;
  }

  public String getTestString() {
    return testString;
  }

  public void setTestString(String testString) {
    this.testString = testString;
  }

  public int getAdminPlayerPos() {
    return adminPlayerPos;
  }

  public void setAdminPlayerPos(int adminPlayerPos) {
    this.adminPlayerPos = adminPlayerPos;
  }
  
}
