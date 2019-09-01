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

  private String word;
  
  public Word() {
  }

  public Word(String word) {
    this.word = word;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }
  
}
