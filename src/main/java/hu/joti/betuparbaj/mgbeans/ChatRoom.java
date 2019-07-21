/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import java.io.Serializable;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Message;

/**
 *
 * @author Joti
 */
@ManagedBean
@ApplicationScoped
public class ChatRoom implements Serializable {

  public final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");   
  public final boolean ONE_NAME_PER_ROW = true;
  
  private Set<String> names;
  private List<Message> messages;
  
  public ChatRoom() {
    names = new TreeSet<>();

    // jöjjön pár kamu user
    names.addAll(Arrays.asList(Game.TESTPLAYERS));
    
    messages = new ArrayList<>();
  }

  public String getNameList(String myName){
    String namesString = "";
    List<String> namesList = new ArrayList<>();
    namesList.addAll(names);
    
    Collections.sort(namesList, Collator.getInstance(new Locale("hu", "HU")));

    for (String name : namesList) {
      if (!namesString.isEmpty()) {
        if (ONE_NAME_PER_ROW) {
          namesString += "\n";
        } else {
          namesString += " \u00B7 ";
        }
      }
      namesString += name;
      if (name.equals(myName))
        namesString += " (én)";
    }  
    return namesString;
  }

  public String getMessageList(Date time){
    String messageList = "";
    for (Message m : messages) {
      if (m.getTime().compareTo(time) >= 0)
        messageList += m.getFormattedMessage(SDF) + "\n";
    }
    
    return messageList;
  }

  public Locale getLocale(){
    return new Locale("hu", "HU");
  }  
  
  public Set<String> getNames() {
    return names;
  }

  public void setNames(Set<String> names) {
    this.names = names;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

//  @Override
//  public void sessionCreated(HttpSessionEvent se) {
//    System.out.println("ChatRoom sessionCreated at " + (new Date()));
//    names.add("Pisti");
//  }
//
//  @Override
//  public void sessionDestroyed(HttpSessionEvent se) {
//    System.out.println("ChatRoom sessionDestroyed at " + (new Date()));  
//  }
  
}
