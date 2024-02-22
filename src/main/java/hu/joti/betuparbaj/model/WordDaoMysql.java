/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Joti
 */
public class WordDaoMysql implements WordDao, Serializable{

  private static final Logger LOGGER = LogManager.getLogger(WordDaoMysql.class.getName());
  private static final int WORD_LIMIT = 20;

  private static Connection getConnection() {

    Connection conn = null;
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      DataSource ds = (DataSource) envCtx.lookup("jdbc/betuparbaj");
      conn = ds.getConnection();
    } catch (NamingException ex) {
      LOGGER.error(ex);
    } catch (SQLException ex) {
      LOGGER.error(ex);
    }
    
    return conn;
  }
  
  @Override
  public List<Word> findAllWords() {

    List<Word> words = null;

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();
      if (conn != null){
        words = new LinkedList<>();

        pstmt = conn.prepareStatement("select * from glossary");
        rs = pstmt.executeQuery();

        while (rs.next()) {
          String[] phrases = rs.getString("gl_words").split(",");
          int category = rs.getInt("gl_category");

          for (String phrase : phrases) {
            Word word = new Word(phrase.trim(), category);
            words.add(word);
          }
        }  
      }
    } catch (SQLException ex) {
      LOGGER.error(ex);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
    }

    if (words != null) {
      LOGGER.info("No. of words in Mysql db glossary: " + words.size());
    }  
    return words;
  }

  @Override
  public void saveAllWords(List<Word> words) {

    Connection conn = null;
    PreparedStatement pstmt = null;
    Set<String> glsWords = new TreeSet<>();
    int wordNum = 0;

    class Glossary {

      int category;
      String init = "";
      String words = "";

      public Glossary() {
      }

      public Glossary(int category, String init, String words) {
        this.category = category;
        this.init = init;
        this.words = words;
      }
    }

    List<Glossary> glossaries = new LinkedList<>();
    Glossary glossary = null;
    String init;

    /* rendezett listára készülünk */
    for (Word word : words) {
      if (glossary == null || !glossary.init.equals(word.getPhrase().substring(0, 2)) || wordNum >= WORD_LIMIT) {
        LOGGER.info("szó: " + word.getPhrase());
        if (glossary != null)
          glossaries.add(glossary);
        glossary = new Glossary();
        wordNum = 0;
      }
      wordNum++;
      glossary.category = word.getCategory();
      glossary.init = word.getPhrase().substring(0, 2);
      if (!glossary.words.isEmpty())
        glossary.words = glossary.words + ",";
      glossary.words = glossary.words + word.getPhrase();
    }
    if (glossary != null)
      glossaries.add(glossary);

//    for (Glossary glossary1 : glossaries) {
//      LOGGER.info(glossary1.init + ": " + glossary1.words);
//    }
    LOGGER.info("glossaries.size: " + glossaries.size());

    try {
      conn = getConnection();

      pstmt = conn.prepareStatement("truncate glossary");
      pstmt.execute();

      pstmt = conn.prepareStatement("insert into glossary (gl_category, gl_init, gl_words) values (?, ?, ?);");

      for (Glossary g : glossaries) {
        pstmt.setInt(1, g.category);
        pstmt.setString(2, g.init);
        pstmt.setString(3, g.words);
        pstmt.executeUpdate();
      }

    } catch (SQLException ex) {
      LOGGER.error(ex);
    } finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
    }

  }

  public int saveWord(String word) {

    String wordToSave = word.trim().toUpperCase();
    if (wordToSave.length() < 2)
      return 1;

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();

      pstmt = conn.prepareStatement("select * from glossary where gl_init = ? and gl_category = ?;");
      pstmt.setString(1, wordToSave.substring(0, 2));
      pstmt.setInt(2, 1);

      rs = pstmt.executeQuery();

      int glid = 0;
      String glwords = null;

      while (rs.next()) {
        String[] phrases = rs.getString("gl_words").split(",");
        for (String phrase : phrases) {
          if (phrase.equals(wordToSave))
            return 2;
        }
        if (phrases.length < WORD_LIMIT) {
          glid = rs.getInt("gl_id");
          glwords = rs.getString("gl_words") + "," + wordToSave;
        }
      }
      
      LOGGER.info("gl_id = " + glid);
      LOGGER.info("gl_words = " + glwords);

      if (glid > 0) {
        pstmt = conn.prepareStatement("update glossary set gl_words = ? where gl_id = ?;");
        pstmt.setString(1, glwords);
        pstmt.setInt(2, glid);
        pstmt.executeUpdate();
      } else {
        pstmt = conn.prepareStatement("insert into glossary (gl_category, gl_init, gl_words) values (?, ?, ?);");
        pstmt.setInt(1, 1);
        pstmt.setString(2, wordToSave.substring(0, 2));
        pstmt.setString(3, glwords);
        pstmt.executeUpdate();
      }

    } catch (SQLException ex) {
      LOGGER.error(ex);
      return 3;
    } finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
    }
    return 0;

  }

  public int deleteWord(String word) {

    String wordToDel = word.trim().toUpperCase();
    if (wordToDel.length() < 2)
      return 1;

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();

      pstmt = conn.prepareStatement("select * from glossary where gl_init = ? and gl_category = ?;");
      pstmt.setString(1, wordToDel.substring(0, 2));
      pstmt.setInt(2, 1);

      rs = pstmt.executeQuery();

      int glid = 0;
      String glwords = null;

      while (rs.next()) {
        glwords = "";
        String[] phrases = rs.getString("gl_words").split(",");
        for (String phrase : phrases) {
          if (phrase.equals(wordToDel)) {
            glid = rs.getInt("gl_id");
          } else {
            if (!glwords.isEmpty())
              glwords += ",";
            glwords += phrase;
          }
        }
        if (glid > 0)
          break;
      }
      
      LOGGER.info(glid);
      LOGGER.info(glwords);
      
      if (glid == 0)
        return 2;

      if (glwords.isEmpty()) {
        pstmt = conn.prepareStatement("delete from glossary where gl_id = ?;");
        pstmt.setInt(1, glid);
        pstmt.executeUpdate();
      } else {
        pstmt = conn.prepareStatement("update glossary set gl_words = ? where gl_id = ?;");
        pstmt.setString(1, glwords);
        pstmt.setInt(2, glid);
        pstmt.executeUpdate();
      }

    } catch (SQLException ex) {
      LOGGER.error(ex);
      return 3;
    } finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          LOGGER.error(ex);
        }
      }
    }
    return 0;

  }
  
}
