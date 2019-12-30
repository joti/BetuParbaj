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
