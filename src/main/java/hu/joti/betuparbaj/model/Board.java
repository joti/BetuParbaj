package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Joti
 */
public class Board implements Serializable {

  public static final int BOARD_SIZE = 6;
  public static final String[] ROW_CODES = {"A","B","C","D","E","F"};
  public static final String[] COLUMN_CODES = {"1","2","3","4","5","6"};

  // játékos helye az asztalon
  private int position;
  // játékos neve
  private Player player;
  //elhelyezett betűk
  private String[][] letters;
  //cellák betöltésének köre (gép által elhelyezett betűk esetén a forduló sorszámának ellentettje)
  private int[][] turnPlaces;
  //el nem helyezett betűk (forduló -> betű map)
  private Map<Integer,String> unplacedLetters;
  // pontot érő szavak a táblán
  private List<Hit> hits;
  // összpontszám
  private int score;
  // helyezés
  private int place;
  // csatlakozás időpontja
  private Date joinDate;
  // folyamatban lévő játékból való kilépés időpontja
  private Date quitDate;
  
  public Board() {
    letters = new String[BOARD_SIZE][BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        letters[i][j] = "";
      }
    }

    turnPlaces = new int[BOARD_SIZE][BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        turnPlaces[i][j] = 0;
      }
    }

    hits = new ArrayList<>();
    unplacedLetters = new HashMap<>();
    joinDate = new Date();
  }

  public Board(Player player) {
    this();
    this.player = player;
  }

  public int getLetterCount() {
    int letterCount = 0;
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        if (!letters[i][j].isEmpty()) {
          letterCount++;
        }
      }
    }
    return letterCount;
  }

  public int getTotalLetterCount() {
    return getLetterCount() + unplacedLetters.size();
  }

  public void setLetter(String letter, int row, int column, int turn) {
    letters[row][column] = letter;
    turnPlaces[row][column] = turn;
  }

  public void setLetterRandom(String letter) {
    Random rnd = new Random();
    int letterPos;
    int count = 0;
    int turn = getLetterCount() + 1;

    letterPos = rnd.nextInt(36 - getLetterCount()) + 1;
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (letters[row][col].isEmpty()) {
          count++;
          if (count == letterPos) {
            letters[row][col] = letter;
            turnPlaces[row][col] = -1 * turn;
            break;
          }
        }
      }
    }
  }

  public boolean isRandomPlaced(int turn){
    for (int i = 0; i < turnPlaces.length; i++) {
      for (int j = 0; j < turnPlaces[i].length; j++) {
        if (turnPlaces[i][j] == -1 * turn){
          return true;
        }
      }
    }
    return false;
  }
  
  public void evaluate(){
    score = 0;
    for (Hit hit : hits) {
      score += hit.getScore();
    }
  }
  
  public String getHitsString(){
    String hitsString = "";
    
    for (Hit hit : hits) {
      hitsString += (hit.isHorizontal()?"H":"V") + hit.getLine() + hit.getStart() + (hit.getEnd() - hit.getStart() + 1) + "";
    }
    return hitsString;
  }
  
  public void fillGaps(){
    int index = 0;
    List<String> unplacedLetterList = new ArrayList<>();
    for (String value : unplacedLetters.values()) {
      unplacedLetterList.add(value);      
    }
            
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 6; col++) {
        if (turnPlaces[row][col] == 0){
          letters[row][col] = unplacedLetterList.get(index);
          index++;
        }
      }
    }
  }
  
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public String[][] getLetters() {
    return letters;
  }

  public void setLetters(String[][] letters) {
    this.letters = letters;
  }

  public List<Hit> getHits() {
    return hits;
  }

  public void setHits(List<Hit> hits) {
    this.hits = hits;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public Date getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(Date joinDate) {
    this.joinDate = joinDate;
  }

  public Date getQuitDate() {
    return quitDate;
  }

  public void setQuitDate(Date quitDate) {
    this.quitDate = quitDate;
  }

  public int getPlace() {
    return place;
  }

  public void setPlace(int place) {
    this.place = place;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  
  public Map<Integer,String> getUnplacedLetters() {
    return unplacedLetters;
  }

  public void setUnplacedLetters(Map<Integer,String> unplacedLetters) {
    this.unplacedLetters = unplacedLetters;
  }

  public int[][] getTurnPlaces() {
    return turnPlaces;
  }

  public void setTurnPlaces(int[][] turnPlaces) {
    this.turnPlaces = turnPlaces;
  }

}
