package hu.joti.betuparbaj.model;

/**
 * @author Joti
 */
public enum ScoringMode {

  LINEAR   ("Lineáris" , new int[]{0, 2, 3, 4, 5, 6}),
  FIBONACCI("Fibonacci", new int[]{0, 2 ,3 ,5, 8, 13}),
  SQUARE   ("Négyzetes", new int[]{0, 4, 9, 16, 25, 36});

  private final String name;
  private final String valueLabel;
  private final String label;
  private final int[] lenValues;

  private ScoringMode(String name, int[] lenValues) {
    this.name = name;
    this.lenValues = lenValues;
    
    String lab = "";
    for (int i = 0; i < lenValues.length; i++) {
      if (lenValues[i] > 0){
        lab += lenValues[i];
        if (i + 1 < lenValues.length){
          lab += "-";
        }    
      }          
    }
    this.valueLabel = lab;
    this.label = name + " (" + valueLabel + ")";
  }

  public int getMaxValue(){
    return lenValues[lenValues.length - 1];
  }
  
  public String getLabel() {
    return label; 
  }

  public String getName() {
    return name;
  }

  public ScoringMode getValue(){
    return this;
  }

  public String getValueLabel() {
    return valueLabel;
  }

  public int[] getLenValues() {
    return lenValues;
  }
  
}
