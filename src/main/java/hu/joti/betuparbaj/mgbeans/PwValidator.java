/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Game;
import java.io.Serializable;
import java.util.Iterator;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Joti
 */
@ManagedBean
@RequestScoped
@FacesValidator("pwValidator")
public class PwValidator implements Serializable, Validator {

  private UIComponent pwInput;

  /**
   * Creates a new instance of PwValidator
   */
  public PwValidator() {
    //System.out.println("pwValidator constructor");
  }

  @Override
  public void validate(FacesContext fc, UIComponent uic, Object t) throws ValidatorException {
    System.out.println("VALIDATE4");

    String gamePassword = (String) uic.getAttributes().get("gamePassword");
    System.out.println(gamePassword);
    if (gamePassword.isEmpty())
      return;

    int gId = (Integer) uic.getAttributes().get("gameId");
    System.out.println("gId: " + gId);
    
    ValueExpression ve = uic.getValueExpression("gamePassword");
    String IdString = (ve != null) ? (String)ve.getValue(fc.getELContext()) : "";    
    System.out.println("IdString = " + IdString);

    ve = uic.getValueExpression("gameId");
    int gId2 = (ve != null) ? (Integer)ve.getValue(fc.getELContext()) : 0;    
    System.out.println("gId2 = " + gId2);
    

    String inputValue = (String)t;
    System.out.println(inputValue);
    System.out.println(inputValue.isEmpty());
    
    if (!gamePassword.equalsIgnoreCase(inputValue)) {
      System.out.println("NOK");
      
      FacesMessage msg = new FacesMessage("Nem megfelelő jelszó.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      if (!inputValue.isEmpty())
        msg.setDetail(gId + "");
      
      throw new ValidatorException(msg);
    }
    System.out.println("PASSWORD IS OK");

  }

  public String getErrorStyle(Game g) {
    FacesContext context = FacesContext
        .getCurrentInstance();
    String clientId = pwInput.getClientId(context);
    System.out.println("clientId = " + clientId);
    System.out.println("Game " + g.getId());
    Iterator<FacesMessage> messages = context
        .getMessages(clientId);
    while (messages.hasNext()) {
      FacesMessage msg = messages.next();
      if (msg.getSeverity().compareTo(
          FacesMessage.SEVERITY_ERROR) >= 0) {
        System.out.println("Severity");
        System.out.println(msg.getDetail());
        if (msg.getDetail().equals(g.getId() + "")){
          return "wrongvalueinput";
        } 
        System.out.println("Return után");
      }
    }
    return null;
  }
  
  public UIComponent getPwInput() {
    return pwInput;
  }

  public void setPwInput(UIComponent pwInput) {
    this.pwInput = pwInput;
  }  

}
