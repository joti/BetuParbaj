/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

/**
 * @author Joti
 */
public class ScoringMode {

  private int index;
  private String label;

  public ScoringMode() {
  }
  
  public ScoringMode(int id, String label) {
    this.index = id;
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

}
