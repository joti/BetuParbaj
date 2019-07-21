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
public class Word implements Serializable {

  private boolean horizontal;
  private int line;
  private int start;
  private int end;

  public Word() {
  }

  public Word(boolean horizontal, int row, int start, int end) {
    this.horizontal = horizontal;
    this.line = row;
    this.start = start;
    this.end = end;
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

  
}
