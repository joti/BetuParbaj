/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import java.util.Collections;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import hu.joti.betuparbaj.model.Message;
import hu.joti.betuparbaj.model.Player;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@SessionScoped
public class LoginData implements Serializable {

  @ManagedProperty("#{chatRoom}")
  ChatRoom chatroom;

  private String name;
  private String error;
  private String message;
  private boolean entered;
  private boolean rulemode;
  private Date time;
  private int seconds;

  private static final Logger logger = Logger.getLogger(GameManager.class.getName());

  /**
   * Creates a new instance of Login
   */
  public LoginData() {
    logger.debug("New LD session");
    name = "";
  }

  @PostConstruct
  public void init() {
    System.out.println("LoginData.PostConstruct method at " + (new Date()));
    if (!name.isEmpty())
      logger.info("LoginData session starts for " + name);
    else
      logger.info("LoginData session starts");
  }

  @PreDestroy
  public void destroy() {
    if (!name.isEmpty())
      logger.info("LoginData session ends for " + name);
    else
      logger.info("LoginData session ends");
    System.out.println("LoginData.PreDestroy method at " + (new Date()));
    doLogout();
  }

  public void doLogin() {
    logger.info("New Player: " + name);

    if (name.isEmpty()) {
      error = "Adj meg egy nevet!";
    } else if (chatroom.getPlayerNames().contains(name)) {
      error = "Ez a név már foglalt.";
    } else {
      error = "";
      entered = true;
      rulemode = false;
      time = new Date();
      seconds = 0;
      chatroom.addPlayer(name, time);
    }
  }

  public void doLogout() {
    chatroom.removePlayer(name);
    
    entered = false;
    name = "";
  }

  public Player getPlayer() {
    if (!name.isEmpty()) {
      return chatroom.getPlayer(name);
    }
    return null;
  }

  public void sendMessage() {
    System.out.println("Event nélkül: " + message);
    if (!message.isEmpty()) {
      Message m = new Message(name, message);
      m.setTime(new Date());
      chatroom.getMessages().add(m);
      Collections.sort(chatroom.getMessages());
      message = "";
    }
  }

  public String getChatMessageList() {
    return chatroom.getMessageList(time);
  }

  public void showRules() {
    rulemode = true;
    System.out.println(rulemode);
  }

  public void showChat() {
    rulemode = false;
    System.out.println(rulemode);
  }

  public void refresh() {
    if (chatroom.getPlayer(name) != null)
      chatroom.playerAccess(name);
    else {
      entered = false;
      name = "";
    }
    seconds++;
  }

  public String getNameList() {
    return chatroom.getNameList(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setChatroom(ChatRoom chatroom) {
    this.chatroom = chatroom;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean isEntered() {
    return entered;
  }

  public void setEntered(boolean entered) {
    this.entered = entered;
  }

  public int getSeconds() {
    return seconds;
  }

  public void setSeconds(int seconds) {
    this.seconds = seconds;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isRulemode() {
    return rulemode;
  }

  public void setRulemode(boolean rulemode) {
    this.rulemode = rulemode;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }
  
}
