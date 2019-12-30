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

/**
 * @author Joti
 */
public class WordDaoTxt implements WordDao, Serializable {

  private static final String RELATIVE_WEBPATH = "/resources/data/words.txt";

  String absolutePath;

  public WordDaoTxt() {
    System.out.println("WordDaoTxt konstruktora 1");
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
    absolutePath = servletContext.getRealPath(RELATIVE_WEBPATH);
    System.out.println(absolutePath);
  }

  @Override
  public List<Word> findAllWords() throws FileNotFoundException, IOException {
    List<Word> words = new ArrayList<>();

    BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(absolutePath), "UTF8"));
    String row;

    while ((row = br.readLine()) != null) {
      Word word = new Word(row.trim());
      words.add(word);
    }
    br.close();
    System.out.println(words.size());
    return words;
  }

  @Override
  public void close() {
    /* Nincs teendő */
  }

}
