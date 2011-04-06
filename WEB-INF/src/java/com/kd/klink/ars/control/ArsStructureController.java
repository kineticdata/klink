package com.kd.klink.ars.control;

import java.util.Enumeration;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import org.apache.struts.actions.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.ars.model.*;
import com.kd.klink.control.*;
import com.kd.klink.model.*;
import com.kd.klink.model.response.*;

/**
 *
 */
public class ArsStructureController extends StructureController {
    private static final Logger logger = Logger.getLogger(ArsStructureController.class.getName());
    
    /**
     * 
     */
    public ActionForward structure(ActionMapping mapping, ActionForm form, 
                                   HttpServletRequest httpRequest, 
                                   HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "structure";
        logger.info("Framework method " + requestMethod + " called: Retrieving structure information.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !curParameter.equals("items")) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String itemsString = httpRequest.getParameter("items");
        
        // Obtain the parameters required for generating a context
        String datasource = mapping.getProperty("Datasource");
        String basicauth = httpRequest.getHeader("Authorization");
        logger.debug("Datasource: " + datasource);
        if (basicauth != null) { logger.debug("Recieved authorization header."); }
        
        // Declare default ArsContext
        ArsContext context = null;
        
        // If there is no context information in the Url or as Http Basic Authentication information
        if (datasource.lastIndexOf("@") < 0 && basicauth == null) {
            // Send the 401 Response to request authentication
            ArsController.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Check for valid items
                long[] items = new long[0];
                if (itemsString == null) {
                    items = null;
                    logger.debug("Items: Not provided, defaulting to retrieving all data related items.");
                } else if (itemsString.equalsIgnoreCase("all")) {
                    logger.debug("Items: all");
                } else {
                    String[] itemsArray = itemsString.split(",");
                    items = new long[itemsArray.length];
                    logger.debug("Items[" + items.length + "]: " + itemsString);
                    for (int i=0;i<itemsArray.length;i++) {
                        try { items[i] = Long.parseLong(itemsArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the items list to numerical values, \"" + itemsArray[i] + "\" is not a valid number."); }
                    }
                }

                // Build the structure
                ArsStructure structure = new ArsStructure(context, structureID, items);
                
                // Get the messages
                ArsController.logMessages(logger, structure.getMessages());
                response.addMessages(structure.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(structure.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Structure information could not be retrieved, " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve structure information.  " + e.toString());
                response.addMessage(new Message("InternalException", e.getMessage()));
                ArsController.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                ArsController.sendResponse(response, httpResponse);
            }
            // Log out
            finally {
                // Logout
                logger.debug("Logging out...");
                if (context != null) {
                    // Do something because otherwise your not guarenteed to logout (Silly Remedy)
                    ArsController.prepareForLogout(context);
                    context.logout(); 
                    logger.debug("Logged out.");
                } else {
                    logger.debug("Context not generated, no need to log out.");
                }
            }
        }
        
        // Return the forwarder
        return null;
    }
    
    /**
     * 
     */
    public ActionForward structures(ActionMapping mapping, ActionForm form, 
                                    HttpServletRequest httpRequest, 
                                    HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "structures";
        logger.info("Framework method " + requestMethod + " called: Obtaining server structure list.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
            logger.warn(message);
            response.addMessage(new Message("InvalidParameter", message));
        }
                
        // Obtain the parameters required for generating a context
        String datasource = mapping.getProperty("Datasource");
        String basicauth = httpRequest.getHeader("Authorization");
        logger.debug("Datasource: " + datasource);
        if (basicauth != null) { logger.debug("Recieved authorization header."); }
        
        // Declare default ArsContext
        ArsContext context = null;
        
        // If there is no context information in the Url or as Http Basic Authentication information
        if (datasource.lastIndexOf("@") < 0 && basicauth == null) {
            // Send the 401 Response to request authentication
            ArsController.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsStructureList structureList = new ArsStructureList(context);

                // Get the messages
                ArsController.logMessages(logger, structureList.getMessages());
                response.addMessages(structureList.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(structureList.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Server structure list retrieved. " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve a server structure list.  " + e.toString());
                response.addMessage(new Message("InternalExceptionn", e.getMessage()));
                ArsController.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                ArsController.sendResponse(response, httpResponse);
            }
            // Log out
            finally {
                // Logout
                logger.debug("Logging out...");
                if (context != null) {
                    // Do something because otherwise your not guarenteed to logout (Silly Remedy)
                    ArsController.prepareForLogout(context);
                    context.logout(); 
                    logger.debug("Logged out.");
                } else {
                    logger.debug("Context not generated, no need to log out.");
                }
            }
        }
        
        // Return the forwarder
        return null;
    }
}
