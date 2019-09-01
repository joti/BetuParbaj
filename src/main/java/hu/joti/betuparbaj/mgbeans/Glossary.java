/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Word;
import hu.joti.betuparbaj.model.WordDao;
import hu.joti.betuparbaj.model.WordDaoTxt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
//import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author Joti
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class Glossary implements Serializable{

  private List<Word> words;
  private int testValue;
  
  /**
   * Creates a new instance of Glossary
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */
  public Glossary() throws FileNotFoundException, IOException {
    System.out.println("Glossary konstruktora indul");
    WordDao wordDao = new WordDaoTxt();
    
    words = new ArrayList<>();
    testValue = 12;

//    Word w1 = new Word("csiga");
//    words.add(w1);
//    Word w2 = new Word("zsiráf");
//    words.add(w2);

    words = wordDao.findAllWords();
    System.out.println("Szótár mérete: " + words.size());
  }

  public List<Word> getWords() {
    return words;
  }

  public void setWords(List<Word> words) {
    this.words = words;
  }

  public int getTestValue() {
    return testValue;
  }

  public void setTestValue(int testValue) {
    this.testValue = testValue;
  }
  
}
