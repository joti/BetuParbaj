/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Joti
 */
public class WordDaoPq implements WordDao, Serializable {

  private static final Logger logger = Logger.getLogger(WordDaoPq.class.getName());

  private static Connection getConnection() throws SQLException {
    // Heroku környezeti változón keresztül biztosítja a connection adatait
    String dbUrl = System.getenv("JDBC_DATABASE_URL");
    return DriverManager.getConnection(dbUrl);
  }

  @Override
  public List<Word> findAllWords() {

    List<Word> words = new LinkedList<>();

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = getConnection();

      pstmt = conn.prepareStatement("select * from word");
      rs = pstmt.executeQuery();

      while (rs.next()) {
        String phrase = rs.getString("phrase");
        int category = rs.getInt("category");

        Word word = new Word(phrase.trim(), category);
        words.add(word);
      }
    } catch (SQLException ex) {
      logger.error(ex);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ex) {
          logger.error(ex);
        }
      }
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException ex) {
          logger.error(ex);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          logger.error(ex);
        }
      }
    }

    logger.info("No of words in db glossary: " + words.size());
    return words;
  }
  
}
