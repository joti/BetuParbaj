package hu.joti.betuparbaj.model;

/**
 *
 * @author Joti
 */
public enum RndLetterMode {

  NORNDLETTER("Nincsenek", 0, 0),
  FIRSTROUND("Az első körben", 1, 0),
  FIRST2ROUNDS("Az első két körben", 2, 0),
  FIRST3ROUNDS("Az első három körben", 3, 0),
  FIRSTTHIRD("Az első 12 betű", 0, 12),
  FIRSTHALF("Az első 18 betű", 0, 18),
  FIRST2THIRDS("Az első 24 betű", 0, 24),
  WHOLEGAME("Az összes", 0, 36);

  private final String label;
  private final int roundLimit;
  private final int letterNumLimit;

  private RndLetterMode(String label, int roundLimit, int letterNumLimit) {
    this.label = label;
    this.roundLimit = roundLimit;
    this.letterNumLimit = letterNumLimit;
  }

  public String getValueLabel(){
    String valueLabel = "";
    if (roundLimit > 0)
      valueLabel = roundLimit + "\u00D7\u21BB";
    else if (letterNumLimit > 0)
      valueLabel = letterNumLimit + "";
    return valueLabel;
  }
  
  public int getOrdinal(){
    return ordinal();
  }
  
  public RndLetterMode getValue(){
    return this;
  }
  
  public String getLabel() {
    return label;
  }

  public int getRoundLimit() {
    return roundLimit;
  }

  public int getLetterNumLimit() {
    return letterNumLimit;
  }
  
}
