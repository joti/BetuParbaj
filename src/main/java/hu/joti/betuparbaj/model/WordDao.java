package hu.joti.betuparbaj.model;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Joti
 */
public interface WordDao {

  List<Word> findAllWords();
  void saveAllWords(List<Word> words);
  /*void saveLogin(String name, Date loginTime);*/

}
