/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.comparators;

import hu.joti.betuparbaj.model.Game;
import java.util.Comparator;

/**
 * @author Joti
 */
public class FinishedGameComparator implements Comparator<Game> {

  @Override
  public int compare(Game o1, Game o2) {
    if (o1.getEndDate() == null && o2.getEndDate() != null)
      return 1;
    else if (o1.getEndDate() != null && o2.getEndDate() == null)
      return -1;
    else if (o1.getEndDate() != null && o2.getEndDate() != null)
      return (o2.getEndDate().getTime() - o1.getEndDate().getTime() < 0 ? -1 : 1);
    else {
      return o1.getId() - o2.getId();
    }    
  }

}
