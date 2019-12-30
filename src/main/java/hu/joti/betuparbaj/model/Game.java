package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
  public static final Integer[] TIMELIMITS = {5, 10, 20, 30, 45, 60, 90, 120};
  public static final int TURN0_TIMELIMIT = 15;
  public static final int TURN_INTERMISSION = 3;

  public static final String[] SCORING_MODES = {"Lineáris (2-3-4-5-6)","Fibonacci (2-3-5-8-13)","Négyzetes (4-9-16-25-36)"};
  public static final int[][] VALUE_OF_WORDS = {{0, 2, 3, 4, 5, 6},{0, 2, 3, 5, 8, 13},{0, 4, 9, 16, 25, 36}};

  public static final int PLAYER_DRAW = 1;
  public static final int RANDOM_DRAW = 2;

  private final static int ALPHABETSETTINGSSTRING_MODE = 2;

  private static final DateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

  // Betűkészlet
  public static final String[] ALPHABET = {"A", "Á", "B", "C", "CS", "D", "E", "É", "F", "G", "GY", "H", "I", "Í", "J", "K", "L", "LY", "M", "N", "NY",
    "O", "Ó", "Ö", "Ő", "P", "R", "S", "SZ", "T", "TY", "U", "Ú", "Ü", "Ű", "V", "X", "Y", "Z", "ZS"};
  // A betűk számossága egy scrabble készletben - gép által sorsolt betűk esetén figyelembe vesszük
  // (Kivétel: az Y nem szerepel külön betűként a scrabble-ben, itt önkényesen hozzárendelünk egy számot)
  public static final int[] LETTERSET = {6, 4, 3, 1, 1, 3, 6, 3, 2, 3, 2, 2, 3, 1, 2, 6, 4, 1, 3, 4, 1,
    3, 3, 2, 1, 2, 4, 3, 2, 5, 1, 2, 1, 2, 1, 2, 0, 2, 2, 1};
  // Hosszú és rövid magánhangzók
  public static final int[] VOWELTYPES = {1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0,
    1, 2, 1, 2, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 0, 0, 0, 0, 0};

  // Alapértelmezett beállítások
  public static final int DEF_SCORING_MODE = 2;
  public static final int DEF_TIMELIMIT = 30;
  public static final boolean DEF_EASYVOWELRULE = true;
  public static final boolean DEF_NODIGRAPH = true;
  public static final boolean DEF_INCLUDEX = true;
  public static final boolean DEF_INCLUDEY = true;
  public static final int DEF_DRAWMODE = 1;
  public static final int DEF_MINPLAYERS = 2;
  public static final int DEF_MAXPLAYERS = 4;
  public static final boolean DEF_RANDOMPLACE = true;
  
  private int id;
  private List<Board> boards;

  private String name;
  private String password;
  private boolean easyVowelRule;
  private boolean noDigraph;
  private boolean includeX;
  private boolean includeY;
  private int drawmode;
  private int minPlayers;
  private int maxPlayers;
  private int numberOfPlayers;
  private int adminPlayerPos;
  private int numberOfActivePlayers;
  private boolean randomOrder;
  private boolean randomPlace;
  private int timeLimit;
  private int scoringMode;
  private Date openDate; // A játék elérhetővé válik a lobbyban, játékosok csatlakozására vár
  private Date startDate; // A játék elindul
  private Date endDate; // A játék véget ér

  private Map<String, Integer> availableLetters;
  private String[] selectedLetters;
  private boolean[] randomLetters;
  private int[] selectedVowelTypes;

  private int turn; // Forduló sorszáma (egy forduló a kiválasztott betű lehelyezésére és a soron következő játékos 
  // betűjének kiválasztására rendelkezésre álló, legfeljebb timelimit mp hosszú időtartam)
  // A "0. forduló" csonka: itt a kezdő játékosnak legfeljebb TURN0_TIMELIMIT mp-e van az első betű kijelölésére
  // A forduló véget ér, ha letelik a timelimit mp, vagy ha minden játékos lerakta a betűt, 
  // és a következő betű is kiválasztásra került.
  // Ha valamelyik játékos kifut az időből, akkor a következő kör csak TURN_INTERMISSION mp-nyi szünetet követően indul el.
  // (Ezalatt ezen játékos üzenetet kap arról, hogy hová került lehelyezésre a betűje, ill. melyik betűt sorsolta a gép helyette.)
  private Date turnStart; // Az aktuális forduló pontos kezdő időpontja                   
  private Date intermissionStart; // Fordulók közötti szünet kezdő időpontja (csak ha valamelyik játékos kifutott az időből)
  private boolean intermission;
  private int currentPlayer;

  public Game() {
    init();
  }

  public Game(int id, String name, boolean easyVowelRule, boolean noDigraph, boolean includeX, boolean includeY, 
              boolean randomPlace, int drawmode, int minPlayers, int maxPlayers, int timeLimit, int scoringMode) {
    this.id = id;
    this.name = name;
    this.easyVowelRule = easyVowelRule;
    this.noDigraph = noDigraph;
    this.includeX = includeX
            ;
    this.includeY = includeY;
    this.drawmode = drawmode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    this.scoringMode = scoringMode;
    this.randomPlace = randomPlace;
    init();
  }

  public Game(String name, boolean easyVowelRule, boolean noDigraph, boolean includeX, boolean includeY, 
              boolean randomPlace, int drawmode, int minPlayers, int maxPlayers, int timeLimit, int scoringMode) {
    this.name = name;
    this.easyVowelRule = easyVowelRule;
    this.noDigraph = noDigraph;
    this.includeX = includeX;
    this.includeY = includeY;
    this.drawmode = drawmode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    this.scoringMode = scoringMode;
    this.randomPlace = randomPlace;
    init();
  }

  private void init() {
    this.password = "";
    boards = new ArrayList<>();
    availableLetters = new HashMap<>();
    numberOfPlayers = 0;
    selectedLetters = new String[36];
    for (int i = 0; i < selectedLetters.length; i++) {
      selectedLetters[i] = "";
    }
    randomLetters = new boolean[36];
    selectedVowelTypes = new int[36];
    turn = 0;
  }

  public void addPlayer(Player player) {
    int pos = getPlayerPos(player.getName());
    if (pos < 0){
      Board board = new Board(player);
      boards.add(board);
      numberOfPlayers++;
      if (boards.size() == 1)
        setAdminPlayer();
    } else {
      boards.get(pos).setQuitDate(null);
      numberOfActivePlayers++;
    }
  }

  public void removePlayer(Player player){
    removePlayer(player.getName());
  }
  
  public void removePlayer(String name) {
    Iterator iter = boards.iterator();
    boolean needSetAdminPlayer = false;

    while (iter.hasNext()) {
      Board board = (Board) iter.next();
      if (board.getPlayer().getName().equals(name)) {
        if (startDate == null) {
          iter.remove();
          numberOfPlayers--;
          if (numberOfPlayers > 0)
            needSetAdminPlayer = true;
        } else {
          board.setQuitDate(new Date());
          numberOfActivePlayers--;
        }
        break;
      }
    }
    if (needSetAdminPlayer)
      setAdminPlayer();
  }

  public void swapPlayers(int pos1, int pos2) {
    Board board1 = boards.get(pos1);
    boards.set(pos1, boards.get(pos2));
    boards.set(pos2, board1);
    setAdminPlayer();
  }

  public String getPlayerName(Long pos, boolean inLobby) {
    int playerPos = (int) (long) pos;
    if (boards.size() > pos) {
      return boards.get(playerPos).getPlayer().getName();
    }
    if (startDate == null){
      if (playerPos >= maxPlayers)
        return "";
      else if (playerPos >= minPlayers){
        if (inLobby)
          return "...........";
        else 
          return "...........................";
      } else {
        if (inLobby)
          return "...........";
        else
          return "...........................";
      }
    } else
      return "";
  }

  public int getPlayerPosState(Long pos){
    /* Lehetséges visszatérési értékek:
       - 0: a maximális játékosszámnál nagyobb pozíció
       - 1-4: van játékos a pozícióban, a helye fix
       - 5: van játékos a pozícióban, a végső helye sorsolással dől el
       - 6: a pozíció még betöltetlen, szükséges a játék indításához
       - 7: a pozíció még betöltetlen, a minimum játékosszámon túli
    */
    if (openDate != null) {
      int playerPos = (int) (long) pos;
      if (numberOfPlayers > playerPos){
        if (startDate == null && randomOrder)
          return 5;
        else
          return playerPos + 1;
      } 
      if (playerPos < minPlayers)
        return 6;
      else if (playerPos < maxPlayers)
        return 7;
    }          
    return 0;
  }
  
  public int getPlayerPos(String name) {
    for (int i = 0; i < boards.size(); i++) {
      if (boards.get(i).getPlayer().getName().equals(name))
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
      if (!((ALPHABET[i].length() == 2 && noDigraph) || (ALPHABET[i].equals("Y") && !includeY) || (ALPHABET[i].equals("X") && !includeX) || (VOWELTYPES[i] == 2 && easyVowelRule))) {
        availableLetters.put(ALPHABET[i], LETTERSET[i]);
      }
    }

    // Ha véletlenszerű a játékosok sorrendje, akkor most beállítjuk
    if (randomOrder) {
      Random rnd = new Random();
      for (int i = numberOfPlayers - 1; i > 0; i--) {
        int number = rnd.nextInt(i + 1);
        if (i != number)
          swapPlayers(i, number);
      }
    }

    for (int i = 0; i < boards.size(); i++) {
      boards.get(i).setPosition(i);
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

  public boolean isPlayerActive(String name) {
    int pos = getPlayerPos(name);
    if (pos >= 0) {
      return boards.get(pos).getPlayer().isActive();
    }
    return false;
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
      if (includeX) {
        settings += "+X ";
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
      if (includeX) {
        settings += "X,";
      }
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

  public String getScoringModeString(){
    String sm = "";
    for (int wordlen = 1; wordlen < VALUE_OF_WORDS[scoringMode].length; wordlen++) {
      sm += ((wordlen > 1) ? "-" : "" ) + VALUE_OF_WORDS[scoringMode][wordlen];
    }
    return sm;
  }
  
  public String getCreator() {
    if (boards.size() > 0) {
      return boards.get(0).getPlayer().getName();
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
        players += boards.get(i).getPlayer().getName();
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
      gameHist += (board.getQuitDate() == null ? board.getLetterCount() : "-");
      gameHist += (board.getPlayer().isActive() ? "." : "?");
    }
    gameHist += ":";
    for (int i = 0; i < turn; i++) {
      if (i > 0)
        gameHist += ",";
      if (selectedLetters[i] != null) {
        gameHist += selectedLetters[i];
      }
    }
    if (turn < 36 && selectedLetters[turn] != null && !selectedLetters[turn].isEmpty()) {
      gameHist += "*";
    }
    if (endDate != null)
      gameHist = "[" + gameHist + "]";
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
    adminPlayerPos = pos;
  }

  public int getNextActivePlayerPos() {
    int pos = currentPlayer - 1;
    int count = 0;
    do {
      pos++;
      count++;
      if (pos >= numberOfPlayers)
        pos = 0;
    } while ((boards.get(pos).getQuitDate() != null || !boards.get(pos).getPlayer().isActive()) && count <= numberOfPlayers);
    return pos;
  }

  public int getTurnTimeLimit() {
    if (turn == 0)
      return TURN0_TIMELIMIT;
    else
      return timeLimit;
  }

  public void endTurn(){
    System.out.println("endTurn");
    if (endDate != null || intermissionStart != null) {
      return;
    }

    boolean needIntermission = false;  
    if (turn > 0) {
      // Ha valamelyik játékos még nem helyezte le a betűt a táblájára, akkor most lerakjuk valahová
      for (Board board : boards) {
        int letterCount = board.getTotalLetterCount();
        if (letterCount < turn){
          if (randomPlace)
            board.setLetterRandom(selectedLetters[turn - 1]);
          else
            board.getUnplacedLetters().put(turn, selectedLetters[turn - 1]);
          needIntermission = true;
        }
      }
    }  

    if (turn < 36) {
      // Ha még nem lett kiválasztva a következő betű, akkor most kisorsoljuk
      if (selectedLetters[turn].equals("")) {
        selectedLetters[turn] = drawLetter();
        randomLetters[turn] = true;
        needIntermission = true;
      }
      int count = availableLetters.get(selectedLetters[turn]);
      availableLetters.put(selectedLetters[turn], count - 1);
    }  
    
    System.out.println("NEEDINTERMISSION: " + needIntermission);
    if (needIntermission)
      intermissionStart = new Date();

  }
  
  public void nextTurn() {
    if (endDate != null) {
      return;
    }

    if (turn == 36) {
      System.out.println("Játék vége.");
      endDate = new Date();
    } else {
      currentPlayer = (currentPlayer + 1) % numberOfPlayers;
      turn++;
      turnStart = new Date();
      intermissionStart = null;
    }
  }

  public void selectLetter(String letter) {
    selectedVowelTypes[turn] = getLetterVowelType(letter);
    selectedLetters[turn] = letter;
  }

  public void selectLetter(int letterIndex) {
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    String letter = ALPHABET[letterIndex];
    selectedVowelTypes[turn] = getLetterVowelType(letter);
    selectedLetters[turn] = letter;
  }

  public boolean isLetterAvailable(String letter) {
    return availableLetters.containsKey(letter);
  }

  public boolean isLetterAvailable(int letterIndex) {
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    String letter = ALPHABET[letterIndex];
    return availableLetters.containsKey(letter);
  }

  public String drawLetter() {
    int letterCount = 0;
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
      letterCount += availableLetters.get(letter);
      System.out.println("betű: " + letter + ", összdb: " + letterCount + ", mgh. db: " + vowelCount + ", msh. db: " + consCount);
    }

    Random rnd = new Random();
    int draw;
    int vowelType;
    boolean nok = false;

    do {
      draw = rnd.nextInt(letterCount) + 1;
      System.out.println("sorsolt szám: " + draw);

      int count = 0;
      for (String letter : availableLetters.keySet()) {
        count += availableLetters.get(letter);
        if (count >= draw) {
          selectedLetter = letter;
          break;
        }
      }

      vowelType = getLetterVowelType(selectedLetter);
    } while ((vowelCount >= 5 && vowelType > 0) || (consCount >= 5 && vowelType == 0));

    System.out.println("Sorsolt betű: " + draw + ". = " + selectedLetter + ", " + (vowelType == 0 ? "msh" : "mgh"));
    selectedVowelTypes[turn] = vowelType;
    return selectedLetter;
  }

  public int getLetterVowelType(String letter) {
    for (int i = 0; i < ALPHABET.length; i++) {
      if (ALPHABET[i].equals(letter)) {
        return VOWELTYPES[i];
      }
    }
    return -1;
  }

  public void placeLetter(int playerPos, int row, int column) {
    if (startDate != null && endDate == null) {
      String letter = getSelectedLetter();
      boards.get(playerPos).setLetter(letter, row, column, turn);
    }
  }

  public String getSelectedLetter() {
    String letter = "";
    if (startDate != null && endDate == null && turn > 0) {
      int index = turn - 1;
      letter = selectedLetters[index];
    }
    return letter;
  }
  
  public String getDateInfo(){
    Date now = new Date();
    Date compareDate;
    String dateInfo;
    String pre = "";
    
    if (endDate != null){
      compareDate = endDate;
//      pre = "Véget ért: ";
    } else if (startDate != null){
      compareDate = startDate;
//      pre = "Elindítva: ";
    } else if (openDate != null) {
      compareDate = openDate;
//      pre = "Készítve: ";
    } else
      return "";
    
    long timeDiff = now.getTime() - compareDate.getTime();
    int mins = (int)(timeDiff / 60000);
    if (mins == 0)
      dateInfo = pre + "most";
    else if (mins < 60)
      dateInfo = pre + mins + " perce";
    else{
      int hours = mins / 60;
      if (hours < 24)
        dateInfo = pre + hours + " órája";
      else {
        int days = hours / 24;
        dateInfo = pre + days + " napja";
      }
    }
    return dateInfo;
  }
  
  public String getScoringModeLabel(){
    return SCORING_MODES[scoringMode];
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

  public boolean isIncludeX() {
    return includeX;
  }

  public void setIncludeX(boolean includeX) {
    this.includeX = includeX;
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

  public boolean[] getRandomLetters() {
    return randomLetters;
  }

  public void setRandomLetters(boolean[] randomLetters) {
    this.randomLetters = randomLetters;
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

  public Date getIntermissionStart() {
    return intermissionStart;
  }

  public void setIntermissionStart(Date intermissionStart) {
    this.intermissionStart = intermissionStart;
  }

  public boolean isIntermission() {
    return intermission;
  }

  public void setIntermission(boolean intermission) {
    this.intermission = intermission;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getScoringMode() {
    return scoringMode;
  }

  public void setScoringMode(int scoringMode) {
    this.scoringMode = scoringMode;
  }

  public boolean isRandomPlace() {
    return randomPlace;
  }

  public void setRandomPlace(boolean randomPlace) {
    this.randomPlace = randomPlace;
  }

}
