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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private boolean admin;
  private Date time;
  private int seconds;

  private static final Logger LOGGER = LogManager.getLogger(LoginData.class.getName());

  /**
   * Creates a new instance of Login
   */
  public LoginData() {
    name = "";
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("LoginData session starts");
  }

  @PreDestroy
  public void destroy() {
    if (!name.isEmpty())
      LOGGER.debug("LoginData session ends for " + name);
    else
      LOGGER.debug("LoginData session ends");
    doLogout();
  }

  public void doLogin() {
    LOGGER.info("New Player: " + name);
    error = "";
    admin = false;

    if (name.isEmpty()) {
      error = "Adj meg egy nevet!";
    } else if (chatroom.getPlayerNames().contains(name)) {
      error = "Ez a név már foglalt.";
    } else {
      if (name.contains("::")){
        String[] parts = name.split("::");
        if (isAdminLogin(parts[0])){
          admin = true;
          name = parts[1];
        } else {
          error = "Érvénytelen név!";
        }
      }  
    }
    
    if (error.isEmpty()){
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

  public static boolean isAdminLogin(String name) {
    final String adminHash = "2C3EA4960E07C5E88EEDDA070519662DB2B442F532160173E71F3EEE865A77E4AE7229D6EFB862AFAAFB4C7C40B166E15250F2DB5B49F11CF8C8942BB01A197F";
    byte[] hashedName;
    boolean isAdmin = false;
    
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      hashedName = md.digest(name.getBytes(StandardCharsets.UTF_8));
      isAdmin = (DatatypeConverter.printHexBinary(hashedName).equals(adminHash));
    } catch (NoSuchAlgorithmException ex) {
      LOGGER.error(ex);
    }

    return isAdmin; 
  }
  
  public void sendMessage() {
    LOGGER.debug(name + "'s message: " + message);
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

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }
  
}
