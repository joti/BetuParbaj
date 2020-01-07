package hu.joti.betuparbaj.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;

/**
 * @author Joti
 */
public class WordDaoTxt implements WordDao, Serializable {

  private static final Logger logger = Logger.getLogger(WordDaoTxt.class.getName());
  
  private static final String RELATIVE_WEBPATH = "/resources/data/words.txt";

  String absolutePath;

  public WordDaoTxt() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
    absolutePath = servletContext.getRealPath(RELATIVE_WEBPATH);

    logger.info("Absolute path of glossary: " + absolutePath);    
  }

  @Override
  public List<Word> findAllWords() {
    List<Word> words = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new InputStreamReader(
         new FileInputStream(absolutePath), "UTF8"))){
      String row;

      while ((row = br.readLine()) != null) {
        Word word = new Word(row.trim(), 1);
        words.add(word);
      }
    } catch (IOException ex) {
      logger.error(ex);
    }
    
    logger.info("No. of words in txt glossary: " + words.size());
    return words;
  }

}
