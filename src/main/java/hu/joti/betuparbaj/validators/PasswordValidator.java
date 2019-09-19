/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * @author Joti
 */
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

  @Override
  public void validate(FacesContext fc, UIComponent uic, Object t) throws ValidatorException {
    System.out.println("VALIDATE");

    String gamePassword = (String) uic.getAttributes().get("gamePassword");
    System.out.println(gamePassword);
    if (gamePassword.isEmpty())
      return;

    String inputValue = (String)t;
    System.out.println(inputValue);
    
    if (!gamePassword.equalsIgnoreCase(inputValue)) {
      System.out.println("NOK");
      FacesMessage msg = new FacesMessage("Nem megfelelő jelszó.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    }
    System.out.println("PASSWORD IS OK");
  }

}
