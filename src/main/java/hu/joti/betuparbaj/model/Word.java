package hu.joti.betuparbaj.model;

import java.io.Serializable;

/**
 * @author Joti
 */
public class Word implements Serializable {

  private String phrase;
  private int category;
  
  public Word() {
  }

  public Word(String word) {
    this.phrase = word;
  }

  public Word(String word, int category) {
    this.phrase = word;
    this.category = category;
  }
  
  public String getPhrase() {
    return phrase;
  }

  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  public int getCategory() {
    return category;
  }

  public void setCategory(int category) {
    this.category = category;
  }
  
}
