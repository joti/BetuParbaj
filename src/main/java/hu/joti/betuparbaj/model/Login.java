/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.joti.betuparbaj.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Joti
 */
public class Login implements Serializable {

  private Date loginTime; 
  private String playerAlias;

  public Login(Date loginTime, String playerAlias) {
    this.loginTime = loginTime;
    this.playerAlias = playerAlias;
  }

  public Login() {
  }

  public Date getLoginTime() {
    return loginTime;
  }

  public void setLoginTime(Date loginTime) {
    this.loginTime = loginTime;
  }

  public String getPlayerAlias() {
    return playerAlias;
  }

  public void setPlayerAlias(String playerAlias) {
    this.playerAlias = playerAlias;
  }

}
