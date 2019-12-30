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
