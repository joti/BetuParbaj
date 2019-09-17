/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    hits = new ArrayList<>();
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

  public void setLetter(String letter, int row, int column) {
    letters[row][column] = letter;
  }

  public void setLetterRandom(String letter) {
    Random rnd = new Random();
    int letterPos;
    int count = 0;

    letterPos = rnd.nextInt(36 - getLetterCount()) + 1;
    for (int row = 0; row < BOARD_SIZE; row++) {
      for (int col = 0; col < BOARD_SIZE; col++) {
        if (letters[row][col].isEmpty()) {
          count++;
          if (count == letterPos) {
            letters[row][col] = letter;
            System.out.println(player.getName() + " random -> " + row + "/" + col);
            break;
          }
        }
      }
    }
  }

  public void evaluate(){
    score = 0;
    for (Hit hit : hits) {
      score += hit.getScore();
      System.out.println(hit.getWord() + " szó értéke: " + hit.getScore() + " pt");
    }
    System.out.println("Összpontsám: " + score);
  }
  
  public String getHitsString(){
    String hitsString = "";
    
    for (Hit hit : hits) {
      hitsString += (hit.isHorizontal()?"H":"V") + hit.getLine() + hit.getStart() + (hit.getEnd() - hit.getStart() + 1) + "";
    }
    return hitsString;
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

}
