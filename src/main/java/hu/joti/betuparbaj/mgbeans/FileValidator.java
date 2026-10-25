package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.GameArchive;
import java.io.DataInputStream;
import java.io.IOException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

/**
 * Feltöltött játszmafájl előzetes ellenőrzése.
 */
@FacesValidator("fileValidator")
public class FileValidator implements Validator {

  @Override
  public void validate(FacesContext context, UIComponent component, Object value) {
    Object sessionLogin = context.getExternalContext().getSessionMap().get("loginData");
    if (!(sessionLogin instanceof LoginData) || !((LoginData) sessionLogin).isEntered()) {
      fail("Játszmát csak bejelentkezett játékos tölthet be.");
    }

    if (!(value instanceof Part)) {
      fail("Nincs kiválasztva fájl.");
    }

    Part file = (Part) value;
    if (file.getSize() < 4 || file.getSize() > GameArchive.MAX_FILE_BYTES) {
      fail("Nem megfelelő fájlméret.");
    }

    // A validátor csak a fejlécet nézi meg; a tartalom feldolgozása egyszer történik.
    try (DataInputStream input = new DataInputStream(file.getInputStream())) {
      int header = input.readInt();
      if (!GameArchive.isNewFormat(header)) {
        if ((header >>> 16) != 0x1f8b) {
          fail("Nem megfelelő fájlformátum.");
        }

        if (!((LoginData) sessionLogin).isAdmin()) {
          fail("A fájl formátuma nem megfelelő.");
        }
      }
    } catch (IOException ex) {
      fail("A fájl nem olvasható.");
    }
  }

  private static void fail(String text) {
    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);

    throw new ValidatorException(message);
  }
}