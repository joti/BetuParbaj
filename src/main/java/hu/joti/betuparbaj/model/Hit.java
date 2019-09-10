/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

import java.io.Serializable;

/**
 * @author Joti
 */
public class Hit implements Serializable {

  private boolean horizontal;
  private int line;
  private int start;
  private int end;
  private int score;
  private String word;

  public Hit() {
  }

  public Hit(int start, int end, int score, String word) {
    this.start = start;
    this.end = end;
    this.score = score;
    this.word = word;
  }

  public Hit(boolean horizontal, int line, int start, int end, int score, String word) {
    this.horizontal = horizontal;
    this.line = line;
    this.start = start;
    this.end = end;
    this.score = score;
    this.word = word;
  }

  public String getPosString(){
    String posString;
    if (horizontal){
      posString = Board.ROW_CODES[line] /*+ Board.COLUMN_CODES[start]*/;
    } else {
      posString = /*Board.ROW_CODES[start]+*/ Board.COLUMN_CODES[line];
    }
    return posString;  
  }
  
  public String getScoreString(){
    if (score == 0)
      return "";
    else
      return "(" + score + ")";
  }
  
  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public boolean isHorizontal() {
    return horizontal;
  }

  public void setHorizontal(boolean horizontal) {
    this.horizontal = horizontal;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

}
