package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Joti
 */
public class Player implements Comparable<Player>, Serializable {

  public static final int INACTIVITY_SEC = 20;
  
  private int id;
  private String name;

  // belépés időpontja
  private Date loginTime;
  // utolsó aktivitás időpontja
  private Date lastAccess;
  // kilépés időpontja
  private Date quitTime;

  public Player() {
  }

  public Player(String name) {
    this.name = name;
  }

  public Player(String name, Date loginTime) {
    this.name = name;
    this.loginTime = loginTime;
  }

  public boolean isActive() {
    if (quitTime != null) {
      return false;
    }

    if (lastAccess != null) {
      Date now = new Date();
      int elapsedSec = (int) ((now.getTime() - lastAccess.getTime()) / 1000);
      return (elapsedSec <= INACTIVITY_SEC);
    }

    return true;
  }

  @Override
  public int compareTo(Player o) {
    return name.compareTo(o.getName());
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getLastAccess() {
    return lastAccess;
  }

  public void setLastAccess(Date lastAccess) {
    this.lastAccess = lastAccess;
  }

  public Date getQuitTime() {
    return quitTime;
  }

  public void setQuitTime(Date quitTime) {
    this.quitTime = quitTime;
  }

  public Date getLoginTime() {
    return loginTime;
  }

  public void setLoginTime(Date loginTime) {
    this.loginTime = loginTime;
  }

}
