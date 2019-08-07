/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Joti
 */
public class Game implements Serializable {

  public static final Integer[] NUM_OF_PLAYERS = {2, 3, 4};
  public static final Integer[] TIMELIMITS = {20, 30, 45, 60, 90, 120};
  public static final int TURN0_TIMELIMIT = 15;

  public static final int PLAYER_DRAW = 1;
  public static final int RANDOM_DRAW = 2;

  public final static String[] TESTPLAYERS = {"Pali", "Sanyiarettenthetetlensanyi", "Fecó", "Bruckner Szigfrid", "Szilveszter", "Juliska", "Mariska", "Ákóisz Igor", "LevenGyula", "Ősember", "Maci Laci", "Róbert Gida", "Mekk Elek", "szigfrid"};
  private final static int ALPHABETSETTINGSSTRING_MODE = 2;

  // Betűkészlet
  public static final String[] ALPHABET = {"A", "Á", "B", "C", "CS", "D", "E", "É", "F", "G", "GY", "H", "I", "Í", "J", "K", "L", "LY", "M", "N", "NY",
    "O", "Ó", "Ö", "Ő", "P", "R", "S", "SZ", "T", "TY", "U", "Ú", "Ü", "Ű", "V", "Y", "Z", "ZS"};
  // A betűk számossága egy scrabble készletben - gép által sorsolt betűk esetén figyelembe vesszük
  // (Kivétel: az Y nem szerepel külön betűként a scrabble-ben, itt önkényesen hozzárendelünk egy számot)
  public static final int[] LETTERSET = {6, 4, 3, 1, 1, 3, 6, 3, 2, 3, 2, 2, 3, 1, 2, 6, 4, 1, 3, 4, 1,
    3, 3, 2, 1, 2, 4, 3, 2, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1};
  // Hosszú és rövid magánhangzók
  public static final int[] VOWELTYPES = {1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0,
    1, 2, 1, 2, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 0, 0, 0, 0};

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
  private int numberOfActivePlayers;
  private boolean randomOrder;
  private int timeLimit;
  private Date openDate; // A játék elérhetővé válik a lobbyban, játékosok csatlakozására vár
  private Date startDate; // A játék elindul
  private Date endDate; // A játék véget ér

  private Map<String, Integer> availableLetters;
  private String[] selectedLetters;
  private int[] selectedVowelTypes;

  private int turn; // Forduló sorszáma (egy forduló a kiválasztott betű lehelyezésére és a soron következő játékos 
  // betűjének kiválasztására rendelkezésre álló, legfeljebb timelimit mp hosszú időtartam)
  // A "0. forduló" csonka: itt a kezdő játékosnak legfeljebb TURN0_TIMELIMIT mp-e van az első betű kijelölésére
  // A forduló véget ér, ha letelik a timelimit mp, vagy ha minden játékos lerakta a betűt, 
  // és a következő betű is kiválasztásra került.
  private Date turnStart; // Az aktuális forduló pontos kezdő időpontja                   
  private int currentPlayer;

  public Game() {
    init();
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
    init();
  }

  public Game(boolean easyVowelRule, boolean noDigraph, boolean includeY, int drawmode, int minPlayers, int maxPlayers, int timeLimit) {
    this.easyVowelRule = easyVowelRule;
    this.noDigraph = noDigraph;
    this.includeY = includeY;
    this.drawmode = drawmode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    init();
  }

  private void init() {
    boards = new ArrayList<>();
    availableLetters = new HashMap<>();
    numberOfPlayers = 0;
    selectedLetters = new String[36];
    for (int i = 0; i < selectedLetters.length; i++) {
      selectedLetters[i] = "";
    }
    selectedVowelTypes = new int[36];
    turn = 0;
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

    while (iter.hasNext()) {
      Board board = (Board) iter.next();
      if (board.getName().equals(name)) {
        if (startDate == null) {
          iter.remove();
          numberOfPlayers--;
        } else {
          board.setQuitDate(new Date());
          numberOfActivePlayers--;
        }
        break;
      }
    }
    if (startDate == null)
      setAdminPlayer();
  }

  public void switchPlayers(int pos1, int pos2) {
    Board board1 = boards.get(pos1);
    boards.set(pos1, boards.get(pos2));
    boards.set(pos2, board1);
    setAdminPlayer();
  }

  public String getPlayerName(Long pos) {
    int playerpos = (int) (long) pos;
    if (boards.size() > pos) {
      return boards.get(playerpos).getName();
    }
    if (playerpos >= minPlayers)
      return "..........?";
    else
      return "..........!";
  }

  public int getPlayerPos(String name) {
    for (int i = 0; i < boards.size(); i++) {
      if (boards.get(i).getName().equals(name))
        return i;
    }
    return -1;
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
    // betűkészlet összeállítása
    for (int i = 0; i < ALPHABET.length; i++) {
      if (!((ALPHABET[i].length() == 2 && noDigraph) || (ALPHABET[i].equals("Y") && !includeY) || (VOWELTYPES[i] == 2 && easyVowelRule))) {
        availableLetters.put(ALPHABET[i], LETTERSET[i]);
      }
    }

    startDate = new Date();
    numberOfActivePlayers = numberOfPlayers;
    currentPlayer = 0;
    turn = 0;

    if (drawmode == PLAYER_DRAW) {
      turnStart = new Date();
    } else {
      nextTurn();
    }
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
    if (ALPHABETSETTINGSSTRING_MODE == 1) {
      settings = "A,Á,B,C,D," + "\u2026" + " ";
      if (!easyVowelRule) {
        settings += "+Í,Ó," + "\u2026" + " ";
      }
      if (!noDigraph) {
        settings += "+CS,GY," + "\u2026" + " ";
      }
      if (includeY) {
        settings += "+Y ";
      }
    } else {
      settings = "A,Á,B,C,";
      if (!noDigraph) {
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
      if (includeY) {
        settings += "Y,";
      }
      if (!noDigraph) {
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

  public String getGameSetupString() {
    return id + ":" + getPlayersString() + ":" + adminPlayerPos + ":" + getAlphabetSettingsString() + ":"
            + ":" + drawmode + ":" + timeLimit + "." + (randomOrder ? "+" : "-");
  }

  public String getGameHistString() {
    String gameHist = "";

    for (Board board : boards) {
      if (!gameHist.isEmpty()) {
        gameHist += "/";
      }
      gameHist += (board.getQuitDate() == null ? "+" : "-");
    }
    gameHist += ":";
    System.out.println("turn=" + turn);
    for (int i = 0; i < turn; i++) {
      if (i > 0)
        gameHist += ",";
      if (selectedLetters[i] != null) {
        gameHist += selectedLetters[i];
      }
    }
    if (endDate != null)
      gameHist += ".";
    return gameHist;
  }

  public void setAdminPlayer() {
    Date minDate = new Date();
    Date joinDate;
    int pos = 0;

    for (int i = 0; i < boards.size(); i++) {
      joinDate = boards.get(i).getJoinDate();
      if (joinDate.before(minDate)) {
        minDate = joinDate;
        pos = i;
      }
    }
    System.out.println("Adminplayerpos: " + pos);
    adminPlayerPos = pos;
  }

  public int getNextActivePlayerPos() {
    int pos = currentPlayer - 1;
    do {
      pos++;
      if (pos >= numberOfPlayers)
        pos = 0;
    } while (boards.get(pos).getQuitDate() != null);
    System.out.println("NEXTACTIVEPLAYERPOS=" + pos);
    return pos;
  }

  public int getTurnTimeLimit() {
    if (turn == 0)
      return TURN0_TIMELIMIT;
    else
      return timeLimit;
  }

  public void nextTurn() {
    if (turn > 36) {
      return;
    }

    if (turn > 0){
      // Ha valamelyik játékos még nem helyezte le a betűt a táblájára, akkor most lerakjuk valahová
      for (Board board : boards) {
        int letterCount = board.getLetterCount(); 
        if (letterCount < turn){
          board.setLetterRandom(selectedLetters[turn - 1]);
        }
      }
    }

    if (turn == 36) {
      System.out.println("Játék vége.");
      endDate = new Date();
    } else {
      // Ha még nem lett kiválasztva a következő betű, akkor most kisorsoljuk
      if (selectedLetters[turn].equals("")) {
        selectedLetters[turn] = drawLetter();
      }
      int count = availableLetters.get(selectedLetters[turn]);
      availableLetters.put(selectedLetters[turn], count - 1);

      System.out.println("currentPlayer = " + currentPlayer);
      currentPlayer = (currentPlayer + 1) % numberOfPlayers;
      System.out.println("currentPlayer (új) = " + currentPlayer);
      System.out.println((turn + 1) + ". betű: " + selectedLetters[turn]);
      turn++;
      turnStart = new Date();
    }
  }  

  public void selectLetter(String letter){
    selectedVowelTypes[turn] = getLetterVowelType(letter);
    selectedLetters[turn] = letter;
  }

  public void selectLetter(int letterIndex){
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    String letter = ALPHABET[letterIndex];
    selectedVowelTypes[turn] = getLetterVowelType(letter);
    selectedLetters[turn] = letter;
  }

  public boolean isLetterAvailable(String letter){
    return availableLetters.containsKey(letter);
  }
  
  public boolean isLetterAvailable(int letterIndex){
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    String letter = ALPHABET[letterIndex];
    return availableLetters.containsKey(letter);
  }
  
  public String drawLetter() {
    int count = 0;
    String selectedLetter = "";
    int vowelCount = 0;
    int consCount = 0;

    // Egymást követő 7 betűből legalább 2 legyen mgh vagy msh
    if (turn > 4) {
      int firstIndex = turn - 6;
      if (firstIndex < 0)
        firstIndex = 0;
      for (int i = firstIndex; i < turn; i++) {
        if (selectedVowelTypes[i] > 0)
          vowelCount++;
        else
          consCount++;
      }
    }

    for (String letter : availableLetters.keySet()) {
      count += availableLetters.get(letter);
      System.out.println("betű: " + letter + ", összdb: " + count + ", mgh. db: " + vowelCount + ", msh. db: " + consCount);
    }

    Random rnd = new Random();
    int draw;
    int vowelType;
    boolean nok = false;

    do {
      draw = rnd.nextInt(count) + 1;
      System.out.println("sorsolt szám: " + draw);
 
      count = 0;
      for (String letter : availableLetters.keySet()) {
        count += availableLetters.get(letter);
        if (count >= draw) {
          selectedLetter = letter;
          break;
        }
      }
 
      vowelType = getLetterVowelType(selectedLetter);
    } while ((vowelCount >= 5 && vowelType > 0) || (consCount >= 5 && vowelType == 0));

    System.out.println("Sorsolt betű: " + draw + ". = " + selectedLetter + ", " + (vowelType == 0?"msh":"mgh"));
    selectedVowelTypes[turn] = vowelType;
    return selectedLetter;
  }

  public int getLetterVowelType(String letter){
    for (int i = 0; i < ALPHABET.length; i++) {
      if (ALPHABET[i].equals(letter)){
        return VOWELTYPES[i];
      }
    }
    return -1;
  }

  public void placeLetter(int playerPos, int row, int column){
    if (startDate != null && endDate == null){
      String letter = getSelectedLetter();
      boards.get(playerPos).setLetter(letter, row, column);
    }
  } 

  public String getSelectedLetter(){
    String letter = "";
    if (startDate != null && endDate == null){
      int index = turn - 1;
      letter = selectedLetters[index];
    } 
    return letter;      
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

  public int getAdminPlayerPos() {
    return adminPlayerPos;
  }

  public void setAdminPlayerPos(int adminPlayerPos) {
    this.adminPlayerPos = adminPlayerPos;
  }

  public int getNumberOfActivePlayers() {
    return numberOfActivePlayers;
  }

  public void setNumberOfActivePlayers(int numberOfActivePlayers) {
    this.numberOfActivePlayers = numberOfActivePlayers;
  }

  public String[] getSelectedLetters() {
    return selectedLetters;
  }

  public void setSelectedLetters(String[] selectedLetters) {
    this.selectedLetters = selectedLetters;
  }

  public int[] getSelectedVowelTypes() {
    return selectedVowelTypes;
  }

  public void setSelectedVowelTypes(int[] selectedVowelTypes) {
    this.selectedVowelTypes = selectedVowelTypes;
  }
  
  public int getTurn() {
    return turn;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

  public Date getTurnStart() {
    return turnStart;
  }

  public void setTurnStart(Date turnStart) {
    this.turnStart = turnStart;
  }

  public int getCurrentPlayer() {
    return currentPlayer;
  }

  public void setCurrentPlayer(int currentPlayer) {
    this.currentPlayer = currentPlayer;
  }

  public Map<String, Integer> getAvailableLetters() {
    return availableLetters;
  }

}
