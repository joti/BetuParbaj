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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Joti
 */
public class Game implements Serializable {

  private static final long SerialVersionUID = 1L;

  public static final Integer[] NUM_OF_PLAYERS = {2, 3, 4};
  public static final Integer[] TIMELIMITS = {15, 20, 30, 45, 60, 90, 120};
  public static final int TURN0_TIMELIMIT = 15;
  public static final int TURN_INTERMISSION = 3;
  public static final int WELCOMEMSG_TIME = 5;
  public static final int PLAYER_DRAW = 1;
  public static final int RANDOM_DRAW = 2;

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
  public static final int DEF_TIMELIMIT = 30;

  public static final boolean DEF_INCLUDELONGVOWELS = false;
  public static final boolean DEF_INCLUDEDIGRAPHS = false;
  public static final boolean DEF_INCLUDEX = true;
  public static final boolean DEF_INCLUDEY = true;
  public static final RndLetterMode DEF_RNDLETTERMODE = RndLetterMode.NORNDLETTER;
  public static final ScoringMode DEF_SCORING_MODE = ScoringMode.SQUARE;
  public static final int DEF_MINPLAYERS = 2;
  public static final int DEF_MAXPLAYERS = 4;
  public static final boolean DEF_RANDOMPLACE = true;

  private static final Logger LOGGER = LogManager.getLogger(Game.class.getName());
  private static final int ALPHABETSETTINGSSTRING_MODE = 3;
  private static final DateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

  private int id;
  private List<Board> boards;

  private String name;
  private String password;

  private boolean includeLongVowels;
  private boolean includeDigraphs;
  private boolean includeX;
  private boolean includeY;
  private RndLetterMode rndLetterMode;
  private ScoringMode scoringMode;
  private int minPlayers;
  private int maxPlayers;
  private int numberOfPlayers;
  private int adminPlayerPos;
  private String openingPlayerName;
  private int numberOfActivePlayers;
  private boolean randomOrder;
  private boolean randomPlace;
  private int timeLimit;
  private Date openDate; // A játék elérhetővé válik a lobbyban, játékosok csatlakozására vár
  private Date startDate; // A játék elindul
  private Date endDate; // A játék véget ér
  private transient boolean uploaded; // Fájlból betöltött játék

  private Map<String, Integer> availableLetters;
  private String[] selectedLetters;
  private boolean[] randomLetters;
  private int[] selectedVowelTypes;

  private int turn; // Forduló sorszáma (egy forduló a kiválasztott betű lehelyezésére és a soron következő játékos 
  // betűjének kiválasztására rendelkezésre álló, legfeljebb timelimit mp hosszú időtartam)
  // A "0. forduló" csonka: itt a kezdő játékosnak legfeljebb TURN0_TIMELIMIT mp-e van az első betű kijelölésére
  // A forduló véget ér, ha letelik a timelimit mp, vagy ha minden (még aktív) játékos lerakta a betűt, 
  // és a következő betű is kiválasztásra került.
  // Ha valamelyik játékos kifut az időből, akkor a következő kör csak TURN_INTERMISSION mp-nyi szünetet követően indul el.
  // (Ezalatt ezen játékos üzenetet kap arról, hogy hová került lehelyezésre a betűje, ill. melyik betűt sorsolta a gép helyette.)
  // A "-1. forduló" a játék kezdetét megelőző üzenet megjelenítésére szolgáló idő, ez WELCOMEMSG_TIME ideig tart.
  private Date turnStart; // Az aktuális forduló pontos kezdő időpontja                   
  private Date intermissionStart; // Fordulók közötti szünet kezdő időpontja (csak ha valamelyik játékos kifutott az időből)
  private int currentPlayer;
  private String preselectedLetter; // Kiválasztásra megjelölt betű

  public Game() {
    init();
  }

  public Game(int id, String name, boolean includeLongVowels, boolean includeDigraphs, boolean includeX, boolean includeY,
          boolean randomPlace, RndLetterMode rndLetterMode, int minPlayers, int maxPlayers, int timeLimit, ScoringMode scoringMode) {
    this.id = id;
    this.name = name;
    this.includeLongVowels = includeLongVowels;
    this.includeDigraphs = includeDigraphs;
    this.includeX = includeX;
    this.includeY = includeY;
    this.rndLetterMode = rndLetterMode;
    this.minPlayers = minPlayers;
    this.maxPlayers = maxPlayers;
    this.timeLimit = timeLimit;
    this.scoringMode = scoringMode;
    this.randomPlace = randomPlace;
    init();
  }

  public Game(String name, boolean includeLongVowels, boolean includeDigraphs, boolean includeX, boolean includeY,
          boolean randomPlace, RndLetterMode rndLetterMode, int minPlayers, int maxPlayers, int timeLimit, ScoringMode scoringMode) {
    this.name = name;
    this.includeLongVowels = includeLongVowels;
    this.includeDigraphs = includeDigraphs;
    this.includeX = includeX;
    this.includeY = includeY;
    this.rndLetterMode = rndLetterMode;
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
    Board board;
    if (pos < 0) {
      board = new Board(player);
      boards.add(board);
      numberOfPlayers++;
      if (boards.size() == 1)
        setAdminPlayer();
    } else {
      board = boards.get(pos);
      board.setPlayer(player);
      board.setQuitDate(null);
      board.setQuitByPlayer(false);

      numberOfActivePlayers++;
    }
  }

  public void removePlayer(Player player) {
    removePlayer(player.getName(), false);
  }

  public void removePlayer(String name, boolean quitByPlayer) {
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
          board.setQuitByPlayer(quitByPlayer);
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
    if (startDate == null) {
      if (playerPos >= maxPlayers)
        return "";
      else if (playerPos >= minPlayers) {
        if (inLobby)
          return ". . . . . . . . . . .";
        else
          return ". . . . . . . . . . . . . . . . . . . . . . . . . . .";
      } else {
        if (inLobby)
          return ". . . . . . . . . . .";
        else
          return ". . . . . . . . . . . . . . . . . . . . . . . . . . .";
      }
    } else
      return "";
  }

  public int getPlayerPosState(Long pos) {
    /* Lehetséges visszatérési értékek:
       - 0: a maximális játékosszámnál nagyobb pozíció
       - 1-4: van játékos a pozícióban, a helye fix
       - 5: van játékos a pozícióban, a végső helye sorsolással dől el
       - 6: a pozíció még betöltetlen, szükséges a játék indításához
       - 7: a pozíció még betöltetlen, a minimum játékosszámon túli
     */
    if (openDate != null) {
      int playerPos = (int) (long) pos;
      if (numberOfPlayers > playerPos) {
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
    if (minPlayers > 1) {
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

  public void fillAvailableLetters(int sets){
    // betűkészlet összeállítása
    for (int i = 0; i < ALPHABET.length; i++) {
      if (!((ALPHABET[i].length() == 2 && !includeDigraphs) || (ALPHABET[i].equals("Y") && !includeY) || (ALPHABET[i].equals("X") && !includeX) || (VOWELTYPES[i] == 2 && !includeLongVowels))) {
        availableLetters.put(ALPHABET[i], LETTERSET[i] * sets);
      }
    }
  }
  
  public void start() {
    fillAvailableLetters(1);

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

    preselectedLetter = "";
    startDate = new Date();
    numberOfActivePlayers = numberOfPlayers;
    currentPlayer = 0;

    if (WELCOMEMSG_TIME == 0) {
      turn = 0;

      if (canPlayerSelectLetter()) {
        turnStart = new Date();
      } else {
        endTurn();
        nextTurn();
      }
    } else {
      turn = -1;
      turnStart = new Date();
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
    if (!includeLongVowels) {
      settings += "Ü";
    } else {
      settings += "Ű";
    }
    return settings;
  }

  public String getAlphabetSettingsString() {
    String settings = "";

    switch (ALPHABETSETTINGSSTRING_MODE) {
      case 1:
        settings = "A,Á,B,C,D," + "\u2026" + " ";
        if (includeLongVowels) {
          settings += "+Í,Ó," + "\u2026" + " ";
        }
        if (includeDigraphs) {
          settings += "+CS,GY," + "\u2026" + " ";
        }
        if (includeX) {
          settings += "+X ";
        }
        if (includeY) {
          settings += "+Y ";
        }
        break;
      case 2:
        settings = "A,Á,B,C,";
        if (includeDigraphs) {
          settings += "CS,";
        } else {
          settings += "D,";
        }
        settings += "...,";
        if (!includeLongVowels) {
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
        if (includeDigraphs) {
          settings += "Z,ZS";
        } else {
          settings += "Z";
        }
        break;
      case 3:
        // 2009: short space, FF0B: fullwidth plus, 2026: small 3 dots
        settings = "A\u2009Á\u2009B\u2009" + "\u2026";
        if (includeDigraphs) {
          settings += "\u2009\uFF0BCS\u2009GY\u2009" + "\u2026";
        }
        if (includeLongVowels) {
          settings += "\u2009\uFF0BÓ\u2009Ő\u2009" + "\u2026";
        }
        if (includeX) {
          settings += "\u2009\uFF0BX";
        }
        if (includeY) {
          settings += "\u2009\uFF0BY";
        }
        break;
    }

    return settings;
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
            + ":" + rndLetterMode + ":" + timeLimit + "." + (randomOrder ? "+" : "-");
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
    if (turn >= 0){
      gameHist += ":";
      if (turn < 10)
        gameHist += "0" + turn;
      else
        gameHist += turn;
      gameHist += ":";
    }
      
    for (int i = 0; i < turn; i++) {
      if (i > 0)
        gameHist += ",";
      if (selectedLetters[i] != null) {
        gameHist += selectedLetters[i];
      }
    }
    if (turn >= 0 && turn < 36 && selectedLetters[turn] != null && !selectedLetters[turn].isEmpty()) {
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
    switch (turn) {
      case -1:
        return WELCOMEMSG_TIME;
      case 0:
        return TURN0_TIMELIMIT;
      default:
        return timeLimit;
    }
  }

  public void endTurn() {
    if (endDate != null || intermissionStart != null) {
      return;
    }

    boolean needIntermission = false;
    if (turn > 0) {
      // Ha valamelyik játékos még nem helyezte le a betűt a táblájára, akkor most lerakjuk valahová
      for (Board board : boards) {
        int letterCount = board.getTotalLetterCount();
        if (letterCount < turn) {
          if (board.getMarkedRow() >= 0){
            board.setMarkedLetter(selectedLetters[turn - 1], turn);  
          } else {
            if (randomPlace)
              board.setLetterRandom(selectedLetters[turn - 1]);
            else
              board.getUnplacedLetters().put(turn, selectedLetters[turn - 1]);

            if (board.getQuitDate() == null)
              needIntermission = true;
          }  
        }
      }
    }

    if (turn >= 0 && turn < 36) {
      // Ha még nem lett kiválasztva a következő betű, akkor most kisorsoljuk
      if (selectedLetters[turn].equals("")) {
        if (!preselectedLetter.isEmpty()){
          selectLetter(preselectedLetter);
        } else {
          selectedLetters[turn] = drawLetter();
          randomLetters[turn] = true;

          if (canPlayerSelectLetter()) {
            Board board = boards.get(currentPlayer);
            if (board.getQuitDate() == null)
              needIntermission = true;
          }
        }  
      }
      int count = availableLetters.get(selectedLetters[turn]);
      availableLetters.put(selectedLetters[turn], count - 1);
    }

    if (needIntermission)
      intermissionStart = new Date();
  }

  public void nextTurn() {
    if (endDate != null) {
      return;
    }

    if (turn == 36) {
      LOGGER.info("Game #" + id + " ends");
      endDate = new Date();
    } else {
      if (turn >= 0) {
        currentPlayer = (currentPlayer + 1) % numberOfPlayers;
      }
      turn++;
      turnStart = new Date();
      intermissionStart = null;
      preselectedLetter = "";

      if (turn == 0 && getRndLetterNumLimit() > 0) {
        endTurn();
        turn++;
      }
    }
  }

  public void selectLetter(String letter) {
    selectedVowelTypes[turn] = getLetterVowelType(letter);
    selectedLetters[turn] = letter;
    preselectedLetter = "";
  }

  public void selectLetter(int letterIndex) {
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    String letter = ALPHABET[letterIndex];
    selectLetter(letter);
  }

  public void preselectLetter(int letterIndex) {
    if (letterIndex > ALPHABET.length)
      letterIndex = 0;
    preselectedLetter = ALPHABET[letterIndex];
  }

  public void confirmLetter(){
    if (!preselectedLetter.isEmpty())
      selectLetter(preselectedLetter);
  }

  public void revokeLetter(){
    if (!preselectedLetter.isEmpty())
      preselectedLetter = "";
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

  public boolean canPlayerSelectLetter() {
    if (startDate == null)
      return false;

    return (turn >= getRndLetterNumLimit());
  }

  public int getRndLetterNumLimit() {
    int roundLimit = rndLetterMode.getRoundLimit();
    int letterNumLimit = rndLetterMode.getLetterNumLimit();
    if (letterNumLimit == 0 && roundLimit > 0) {
      letterNumLimit = roundLimit * boards.size();
    }

    return letterNumLimit;
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
    }

    Random rnd = new Random();
    int draw;
    int vowelType;
    boolean nok = false;

    do {
      draw = rnd.nextInt(letterCount) + 1;

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

    LOGGER.debug("Random letter: " + selectedLetter + " -> " + (vowelType == 0 ? "consonant" : "vowel"));
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

  public void placeLetter(int playerPos, int row, int column, boolean finePointer) {
    if (startDate != null && endDate == null) {
      String letter = getSelectedLetter();
      boolean doSet = finePointer;
      
      if (!finePointer){
        int markedRow = boards.get(playerPos).getMarkedRow();
        int markedCol = boards.get(playerPos).getMarkedCol();
        if (markedRow == row && markedCol == column){
          doSet = true;
        }
      }  
      
      if (doSet){
        boards.get(playerPos).setLetter(letter, row, column, turn);
      } else {
        boards.get(playerPos).markCell(row, column);
      }
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

  public String getDateInfo() {
    Date now = new Date();
    Date compareDate;
    String dateInfo;
    String pre = "";

    if (endDate != null) {
      compareDate = endDate;
    } else if (startDate != null) {
      compareDate = startDate;
      pre = "\u2022 ";
    } else if (openDate != null) {
      compareDate = openDate;
      pre = "\u2022 ";
    } else
      return "";

    long timeDiff = now.getTime() - compareDate.getTime();
    int mins = (int) (timeDiff / 60000);
    if (mins == 0)
      dateInfo = pre + "most";
    else if (mins < 60)
      dateInfo = pre + mins + " perce";
    else {
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

  public List<Board> getBoards() {
    return boards;
  }

  public void setBoards(List<Board> boards) {
    this.boards = boards;
    this.numberOfPlayers = boards.size();
  }

  public boolean isIncludeLongVowels() {
    return includeLongVowels;
  }

  public void setIncludeLongVowels(boolean includeLongVowels) {
    this.includeLongVowels = includeLongVowels;
  }

  public boolean isIncludeDigraphs() {
    return includeDigraphs;
  }

  public void setIncludeDigraphs(boolean includeDigraphs) {
    this.includeDigraphs = includeDigraphs;
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

  public ScoringMode getScoringMode() {
    return scoringMode;
  }

  public void setScoringMode(ScoringMode scoringMode) {
    this.scoringMode = scoringMode;
  }

  public boolean isRandomPlace() {
    return randomPlace;
  }

  public void setRandomPlace(boolean randomPlace) {
    this.randomPlace = randomPlace;
  }

  public RndLetterMode getRndLetterMode() {
    return rndLetterMode;
  }

  public void setRndLetterMode(RndLetterMode rndLetterMode) {
    this.rndLetterMode = rndLetterMode;
  }

  public String getPreselectedLetter() {
    return preselectedLetter;
  }

  public void setPreselectedLetter(String preselectedLetter) {
    this.preselectedLetter = preselectedLetter;
  }

  public boolean isUploaded() {
    return uploaded;
  }

  public void setUploaded(boolean uploaded) {
    this.uploaded = uploaded;
  }

  public String getOpeningPlayerName() {
    return openingPlayerName;
  }

  public void setOpeningPlayerName(String openingPlayerName) {
    this.openingPlayerName = openingPlayerName;
  }

//  @Override
//  public int compareTo(Game o) {
//    if (endDate == null && o.endDate != null)
//      return 1;
//    else if (endDate != null && o.endDate == null)
//      return -1;
//    else if (endDate != null && o.endDate != null)
//      return (int)(o.endDate.getTime() - endDate.getTime());
//    else {
//      return o.id - id;
//    }
//      
//  }
  
}
