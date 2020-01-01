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

  private static final Logger logger = Logger.getLogger(LoginData.class.getName());

  /**
   * Creates a new instance of Login
   */
  public LoginData() {
    name = "";
  }

  @PostConstruct
  public void init() {
    logger.debug("LoginData session starts");
  }

  @PreDestroy
  public void destroy() {
    if (!name.isEmpty())
      logger.debug("LoginData session ends for " + name);
    else
      logger.debug("LoginData session ends");
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
    logger.debug(name + "'s message: " + message);
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
  }

  public void showChat() {
    rulemode = false;
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
