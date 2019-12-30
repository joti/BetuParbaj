package hu.joti.betuparbaj.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Joti
 */
public interface WordDao {

  List<Word> findAllWords() throws FileNotFoundException, IOException;

  void close();
  
}
