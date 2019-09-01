/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

//    try {
      FileReader fr = new FileReader(absolutePath);
      BufferedReader br = new BufferedReader(fr);
      String row;

      while ((row = br.readLine()) != null){
        Word word = new Word(row.trim());
        words.add(word);  
      }
      br.close();
//    } catch (FileNotFoundException ex) {
//      System.out.println("A fájl nem található.");
//    } catch (IOException ex) {
//      System.out.println("A fájl nem olvasható.");
//    }
    System.out.println(words.size());
    return words;    
  }

  @Override
  public void close() {
    /* Nincs teendő */
  }
  
}
