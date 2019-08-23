/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Joti
 */
public class Message implements Comparable<Message>, Serializable {

  private Date time;
  private String name;
  private String text;
  private int gameId;

  public Message() {
  }

  public Message(String name, String text) {
    this.name = name;
    this.text = text;
    this.time = new Date();
  }

  public String getFormattedMessage(SimpleDateFormat SDF){
    String formatted = "[" + SDF.format(time) + "] ";
    switch (text) {
      case "$ENTER":
        formatted += "--" + name + " belépett--";
        break;
      case "$EXIT":
        formatted += "--" + name + " kilépett--";
        break;
      default:
        formatted += name + ": " + text;
    }
    return formatted;    
  };
  
  @Override
  public int compareTo(Message o) {
    return time.compareTo(o.time);
  }
  
  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

}
