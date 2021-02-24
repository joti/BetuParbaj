/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Game;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@RequestScoped
@FacesValidator("fileValidator")
public class FileValidator implements Validator {

  private static final Logger LOGGER = LogManager.getLogger(PwValidator.class.getName());

  @Override
  public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
    if (value == null){
      FacesMessage msg = new FacesMessage("Nincs kiválasztva fájl.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);

      throw new ValidatorException(msg);
    }
    
    Part file = (Part) value;
    LOGGER.info("File size: " + file.getSize());
    
    if (file.getSize() > 10000) {
      FacesMessage msg = new FacesMessage("Nem megfelelő fájl.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    }
    
    String fileContent;
    
    try {
      InputStream input = file.getInputStream();
      LOGGER.info("A");

      GZIPInputStream gis = new GZIPInputStream(input);
      ObjectInputStream ois = new ObjectInputStream(gis);

      LOGGER.info("B");
      Game inputGame = (Game) ois.readObject();
      LOGGER.info("C");
      ois.close();      
      LOGGER.info("D");

      if (inputGame == null)  {
        LOGGER.info("Játszma betöltése sikertelen.");
        FacesMessage msg = new FacesMessage("Játszma betöltése sikertelen.");
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        throw new ValidatorException(msg);
      }
    } catch (ZipException ex) {
      LOGGER.info("A fájl tartalma nem megfelelő.");
      FacesMessage msg = new FacesMessage("A fájl tartalma nem megfelelő.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    } catch (ClassNotFoundException ex) {
      LOGGER.info("A fájl tartalma nem megfelelő.");
      FacesMessage msg = new FacesMessage("A fájl tartalma nem megfelelő.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.info("Játszma betöltése sikertelen.");
      FacesMessage msg = new FacesMessage("Játszma betöltése sikertelen.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    }    
    
    
  }

}
