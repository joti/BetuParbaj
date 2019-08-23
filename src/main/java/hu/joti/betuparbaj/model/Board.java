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
  public static final int INACTIVITY_SEC = 5;

  // játékos helye az asztalon
  private int position;
  // játékos neve
  private String name;
  //elhelyezett betűk
  private String[][] letters;
  // pontot érő szavak a táblán
  private List<Word> words;
  // összpontszám
  private int score;
  // helyezés
  private int place;
  // csatlakozás időpontja
  private Date joinDate;
  // kilépés időpontja
  private Date quitDate;
  // utolsó aktivitás időpontja
  private Date lastAccess;

  public Board() {
    letters = new String[BOARD_SIZE][BOARD_SIZE];
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        letters[i][j] = "";
      }
    }

    words = new ArrayList<>();
    joinDate = new Date();
  }

  public Board(String name) {
    this();
    this.name = name;
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
            System.out.println(name + " random -> " + row + "/" + col);
            break;
          }
        }
      }
    }
  }

  public boolean isActive() {
    if (quitDate != null) {
      return false;
    }

    if (lastAccess != null) {
      Date now = new Date();
      int elapsedSec = (int) ((now.getTime() - lastAccess.getTime()) / 1000);
      return (elapsedSec <= INACTIVITY_SEC);
    }

    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String[][] getLetters() {
    return letters;
  }

  public void setLetters(String[][] letters) {
    this.letters = letters;
  }

  public List<Word> getWords() {
    return words;
  }

  public void setWords(List<Word> words) {
    this.words = words;
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

  public Date getLastAccess() {
    return lastAccess;
  }

  public void setLastAccess(Date lastAccess) {
    this.lastAccess = lastAccess;
  }

}
