package hu.joti.betuparbaj.mgbeans;

import javax.faces.application.ViewExpiredException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Joti
 */
@ManagedBean
@RequestScoped
public class ErrorBean {

  private static final Logger LOGGER = LogManager.getLogger(GameManager.class.getName());
  
    public void throwError1(){
        LOGGER.info("Error1");
        throw new RuntimeException("throwing new error");
    }

    public void throwError2(){
        LOGGER.info("Error2");
        throw new ViewExpiredException("view expired");
    }

    public void throwError3(){
        LOGGER.info("Error3");
        throw new RuntimeException("restoring state");
    }

}