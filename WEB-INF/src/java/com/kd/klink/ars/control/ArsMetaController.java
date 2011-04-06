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
public class ArsMetaController extends MetaController {
    private static final Logger logger = Logger.getLogger(ArsMetaController.class.getName());
    
    /**
     * 
     */
    public ActionForward configurations(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest httpRequest, 
                                        HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "configuration";
        logger.info("Framework method " + requestMethod + " called: Gathering server configuration information.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if (!curParameter.equals("items")) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String itemListString = httpRequest.getParameter("items");
        
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
                // Generate an array of configuration items
                String[] itemList = new String[0];
                if (itemListString != null && !itemListString.equalsIgnoreCase("all")) {
                    itemList = itemListString.split(",");
                }
                logger.debug("Itemlist[" + itemList.length + "]: " + itemListString);
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsConfiguration configurationInfo = new ArsConfiguration(context, itemList);

                // Get the messages
                ArsController.logMessages(logger, configurationInfo.getMessages());
                response.addMessages(configurationInfo.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(configurationInfo.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Server configuration information could not be retrieved, " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve server configurations.  " + e.toString());
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
    public ActionForward permissions(ActionMapping mapping, ActionForm form, 
                                     HttpServletRequest httpRequest, 
                                     HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "permissions";
        logger.info("Framework method " + requestMethod + " called: Retrieving permissions.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !curParameter.equals("items") ) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String itemIDs = mapping.getProperty("ItemIDs");
        String includeAllItemsString = httpRequest.getParameter("includeallitems");
        String itemListString = httpRequest.getParameter("items");
        
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
                
                // Set the defaults
                boolean includeStructurePermissions = true;
                
                // Get all the itemIDs listed as part of the framework call
                long[] propertyItemList = new long[0];
                if (itemIDs != null) {
                    // We don't want the structure permission since a list of fields were specified
                    includeStructurePermissions = false;
                    
                    // Build up the propertyItemList (which overrides the itemlist parameter)
                    String[] itemIDArray = itemIDs.split(",");
                    propertyItemList = new long[itemIDArray.length];
                    for (int i=0;i<itemIDArray.length;i++) {
                        try { propertyItemList[i] = Long.parseLong(itemIDArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the item \"" + itemIDArray[i] + "\" to numerical format."); }
                    }
                }
                logger.debug("ItemIDs[" + propertyItemList.length + "]: " + itemIDs);
                
                // Generate the field list
                long[] itemList = new long[0];
                if (propertyItemList.length > 0) {
                    itemList = propertyItemList;
                } else if (itemListString == null) {
                    itemList = null;
                } else if (!itemListString.equals("all")) {
                    String[] itemListArray = itemListString.split(",");
                    itemList = new long[itemListArray.length];
                    for (int i=0;i<itemList.length;i++) {
                        try { itemList[i] = Long.parseLong(itemListArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the items list to numberical values, \"" + itemListArray[i] + "\" is not a valid number."); }
                    }
                    logger.debug("ItemList[" + itemList.length + "]: " + itemListString);
                }
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the ArsPermissins object
                ArsPermissions permissions = new ArsPermissions(context, structureID, includeStructurePermissions, itemList);

                // Get the messages
                ArsController.logMessages(logger, permissions.getMessages());
                response.addMessages(permissions.getMessages());
                
                // Check for weird cases
                if (itemIDs != null && itemListString != null) {
                    String msg = "Structure items were provided via both request url and items parameter, ignoring items parameter.";
                    logger.warn(msg);
                    response.addMessage(new Message("InternalWarning", msg));
                }
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(permissions.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("User validation unsuccessful, " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve permissions.  " + e.getMessage());
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
    public ActionForward statistics(ActionMapping mapping, ActionForm form, 
                                    HttpServletRequest httpRequest, 
                                    HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "statistics";
        logger.info("Framework method " + requestMethod + " called: Gathering server statistic information.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if (!curParameter.equals("items")) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String itemListString = httpRequest.getParameter("items");
        
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
                // Generate an array of statistics items
                String[] itemList = new String[0];
                if (itemListString != null && !itemListString.equalsIgnoreCase("all")) {
                    itemList = itemListString.split(",");
                }
                logger.debug("Itemlist[" + itemList.length + "]: " + itemListString);
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsStatistics statisticsInfo = new ArsStatistics(context, itemList);

                // Get the messages
                ArsController.logMessages(logger, statisticsInfo.getMessages());
                response.addMessages(statisticsInfo.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(statisticsInfo.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("User validation unsuccessful, " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve statistics.  " + e.toString());
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
    public ActionForward usercheck(ActionMapping mapping, ActionForm form, 
                                   HttpServletRequest httpRequest, 
                                   HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "usercheck";
        logger.info("Framework method " + requestMethod + " called: Validating the context.");
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
        
        ArsContext context = null;

        // If we are in debug mode print all of the headers
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP Headers: ");
            Enumeration headerNames = httpRequest.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement().toString();
                Enumeration headerValues = httpRequest.getHeaders(headerName);
                while(headerValues.hasMoreElements()) {
                    logger.debug("   " + headerName + ": " + headerValues.nextElement().toString());
                }
            }
        }
        
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
                
                // Login, validating the context
                logger.debug("Logging in with context...");
                context.login();

                // Get the messages
                logger.debug("Logging messages...");
                ArsController.logMessages(logger, context.getMessages());
                logger.debug("Adding messages to response...");
                response.addMessages(context.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                logger.debug("Adding result to response...");
                response.addResult(context.generateXmlElement());
                ArsController.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("User validation unsuccessful, " + infos.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                ArsController.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("User validation unsuccessful.  " + e.toString());
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
}