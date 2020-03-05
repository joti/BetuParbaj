package hu.joti.betuparbaj.exception;

import java.util.Iterator;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Joti
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

  private static final Logger LOGGER = LogManager.getLogger(CustomExceptionHandler.class.getName());

  private ExceptionHandler wrapped;

  CustomExceptionHandler(ExceptionHandler exceptionHandler) {
    this.wrapped = exceptionHandler;
  }

  @Override
  public ExceptionHandler getWrapped() {
    return wrapped;
  }

  @Override
  public void handle() throws FacesException {

    final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
    while (i.hasNext()) {
      ExceptionQueuedEvent event = i.next();
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

      // get the exception from context
      Throwable t = context.getException();

      final FacesContext fc = FacesContext.getCurrentInstance();
      final Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
      final NavigationHandler nav = fc.getApplication().getNavigationHandler();

      try {
        String errMsg = t.getMessage();
        String errorPage = null;
        
        if (t instanceof Exception)
          LOGGER.info("Exception: " + (Exception)t + " - " + errMsg);
        
        if (t instanceof ViewExpiredException || errMsg.contains("restoring state") || errMsg.contains("ViewExpiredException")) {
          // redirect to error page
          requestMap.put("error-message", t.getMessage());
          errorPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
          errorPage += "?faces-redirect=true";
        } else {
          requestMap.put("error-message", t.getMessage());
          requestMap.put("error-stack", t.getStackTrace());          
          errorPage = "/error";
        }
        // redirect to error page
        if (!errorPage.isEmpty()){
          nav.handleNavigation(fc, null, errorPage);
          fc.renderResponse();
        }  

      } finally {
        //remove it from queue
        i.remove();
      }
    }
    //parent handle
    getWrapped().handle();
  }
}
