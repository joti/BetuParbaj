package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Game;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@RequestScoped
@FacesValidator("pwValidator")
public class PwValidator implements Serializable, Validator {

  private static final Logger LOGGER = LogManager.getLogger(PwValidator.class.getName());
  
  private UIComponent pwInput;
  private boolean btnClicked;

  /**
   * Creates a new instance of PwValidator
   */
  public PwValidator() {
    //System.out.println("pwValidator constructor");
  }

  @Override
  public void validate(FacesContext fc, UIComponent uic, Object t) throws ValidatorException {
    Map<String, Object> attrs = uic.getAttributes();
    
    String gamePassword = (String) attrs.get("gamePassword");
    if (gamePassword.isEmpty())
      return;

    int gId = (Integer) attrs.get("gameId");
    String btnId = "";
    if (attrs.get("btnId") != null){
      btnId = (String)attrs.get("btnId");
    }  
    if (btnId.isEmpty())
      return;
    
    String inputValue = (String)t;
    
    if (!gamePassword.equalsIgnoreCase(inputValue)) {
      LOGGER.debug("Wrong password: " + inputValue);
      
      FacesMessage msg = new FacesMessage("Nem megfelelő jelszó.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      msg.setDetail(gId + "");
      
      throw new ValidatorException(msg);
    }
    LOGGER.debug("Right password: " + inputValue);

  }

  public String getErrorStyle(Game g) {
    FacesContext context = FacesContext
        .getCurrentInstance();
    String clientId = pwInput.getClientId(context);
    Iterator<FacesMessage> messages = context
        .getMessages(clientId);
    while (messages.hasNext()) {
      FacesMessage msg = messages.next();
      if (msg.getSeverity().compareTo(
          FacesMessage.SEVERITY_ERROR) >= 0) {
        if (msg.getDetail().equals(g.getId() + "")){
          return "wrongvalueinput";
        } 
      }
    }
    return null;
  }
  
  public void clickBtn(ActionEvent event){
    btnClicked = true;
  }
  
  public UIComponent getPwInput() {
    return pwInput;
  }

  public void setPwInput(UIComponent pwInput) {
    this.pwInput = pwInput;
  }  

  public boolean isBtnClicked() {
    return btnClicked;
  }

  public void setBtnClicked(boolean btnClicked) {
    this.btnClicked = btnClicked;
  }

  
}
