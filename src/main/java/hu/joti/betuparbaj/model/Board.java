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

/**
 * @author Joti
 */
public class Board implements Serializable {

  // játékos neve
  private String name;
  //elhelyezett betűk
  private String[][] letters;
  // pontot érő szavak a táblán
  private List<Word> words;
  // összpontszám
  private int score;
  // csatlakozás időpontja
  private Date joinDate;

  public Board() {
    letters = new String[6][6];
    words = new ArrayList<>();
    joinDate = new Date();
  }

  public Board(String name) {
    this();
    this.name = name;
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
  
}
