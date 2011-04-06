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
public class ArsDataController extends DataController {
    private static final Logger logger = Logger.getLogger(ArsDataController.class.getName());
    
    /**
     * 
     */
    public ActionForward attachment(ActionMapping mapping, ActionForm form, 
                                    HttpServletRequest httpRequest, 
                                    HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "attachment";
        logger.info("Framework method " + requestMethod + " called: Retrieving an attachment.");
        
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
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String entryID = mapping.getProperty("EntryID");
        String entryItemID = mapping.getProperty("EntryItemID");
        
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
            super.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                logger.debug("EntryID: " + entryID);
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsAttachment attach = new ArsAttachment(context, structureID, entryID, entryItemID);

                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addMessages(attach.getMessages());
                response.addResult(attach.generateXmlElement());
                ArsController.logMessages(logger, response.getMessages());
                Controller.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Attachment information could not be retrieved, " + messages.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                Controller.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve attachment information.  " + e.toString());
                response.addMessage(new Message("InternalException", e.getMessage()));
                Controller.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the Attachmetn xml
            catch (ModelException e) {
                // Build the Response and send it
                logger.warn("Unable to create ArsAttachment object.  " + e.toString());
                response.addMessage(new Message("ModelException", e.getMessage()));
                Controller.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                Controller.sendResponse(response, httpResponse);
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
        
        return null;
    }
    
    /**
     * 
     */
    public ActionForward create(ActionMapping mapping, ActionForm form, 
                                HttpServletRequest httpRequest, 
                                HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "create";
        logger.info("Framework method " + requestMethod + " called: Creating an entry.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !(curParameter.equals("entry") || curParameter.equals("returnentry") || curParameter.equals("items")) ) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String itemsString = httpRequest.getParameter("items");
        String returnEntryString = httpRequest.getParameter("returnentry");
        
        // Declare default ArsContext
        ArsContext context = null;
        
        try {
            // Get the entry string
            String entryString = "";
            if (httpRequest.getContentType() != null && httpRequest.getContentType().equals("application/xml")) {
                logger.debug("ContentType: application/xml");
                String curString;
                while ((curString = httpRequest.getReader().readLine()) != null) {
                    entryString += curString;
                }
            } else if (httpRequest.getMethod().equals("GET")) {
                logger.debug("RequestMethod: GET");
                entryString = httpRequest.getParameter("entry");
            } else if (httpRequest.getMethod().equals("POST")) {
                logger.debug("RequestMethod: POST");
                entryString = httpRequest.getHeader("entry");
            } else {
                String msg = "Unable to process request, invalid Http Method: " + httpRequest.getMethod();
                logger.warn(msg);
                throw new ControllerException(msg);
            }

            // Obtain the parameters required for generating a context
            String datasource = mapping.getProperty("Datasource");
            String basicauth = httpRequest.getHeader("Authorization");
            logger.debug("Datasource: " + datasource);
            if (basicauth != null) { logger.debug("Recieved authorization header."); }

            // If there is no context information in the Url or as Http Basic Authentication information
            if (datasource.lastIndexOf("@") < 0 && basicauth == null) {
                // Send the 401 Response to request authentication
                super.requestBasicAuth(httpResponse);
            }
            // If there was context information 
            else {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                logger.debug("EntryString: " + entryString);
                
                // Verify we have a valid value for returnentry
                boolean returnEntry = false;
                if (returnEntryString == null) {
                    logger.debug("ReturnEntry: Not present, defaulting to \"false\".");
                } else if (returnEntryString.equalsIgnoreCase("true")) {
                    logger.debug("ReturnEntry: true");
                    returnEntry = true;
                } else if (returnEntryString.equalsIgnoreCase("false")) { 
                    logger.debug("ReturnEntry: false");
                } else {
                    logger.debug("ReturnEntry: Invalid returnentry value \"" + returnEntryString + "\", defaulting to \"false\".");
                    response.addMessage(new Message("InvalidParameter", "Unable to process the returnentry parameter, \"" + returnEntryString + "\" is not a valid parameter.  Defaulting to false."));
                    returnEntry = false;
                }
                
                long[] items = null;
                if (itemsString != null) {
                    if (returnEntry == false) {
                        logger.debug("Items: ReturnEntries was false, but items requested.  Implicetly setting returnentries to true.");
                        returnEntry = true;
                    }

                    String[] itemsArray = itemsString.split(",");
                    items = new long[itemsArray.length];
                    logger.debug("Items[" + items.length + "]: " + itemsString);
                    for (int i=0;i<itemsArray.length;i++) {
                        try { items[i] = Long.parseLong(itemsArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the items list to numerical values, \"" + itemsArray[i] + "\" is not a valid number."); }
                    }
                }
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsEntry entry = ArsEntry.create(context, structureID, entryString);
                
                // Log and add the messages from create
                ArsController.logMessages(logger, entry.getMessages());
                response.addMessages(entry.getMessages());
                
                // Check to see if we need to retrieve the new entry
                if (returnEntry) {
                    logger.debug("Retrieving the updated entry...");
                    entry = new ArsEntry(context, structureID, entry.getGenericEntry().getID(), items);

                    // Log and add the messages from retrieve
                    ArsController.logMessages(logger, entry.getMessages());
                    response.addMessages(entry.getMessages());
                }
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(entry.generateXmlElement());
                Controller.sendResponse(response, httpResponse);
            }
        // If there was a problem generating the context
        } catch (ARException e) {
            // Get the messages
            logger.debug("Retrieving last statuses...");
            StatusInfo[] infos = e.getLastStatus();
            Message[] messages = ArsObject.generateMessages(infos);

            // Respond
            logger.warn("Entry could not be created, " + messages.length + " messages recieved.");
            response.addMessages(messages);
            ArsController.logMessages(logger, messages);
            Controller.sendResponse(response, httpResponse);
        }
        // If there was a problem building up a GenericEntry from the provided Xml
        catch (ModelException e) {
            // Build the Response and send it
            logger.warn("Unable to create ArsEntry.  " + e.toString());
            response.addMessage(new Message("ModelException", e.getMessage()));
            Controller.sendResponse(response, httpResponse);
        }
        // If the authentication wasn't of type basic.
        catch (ControllerException e) {
            // Build the Response and send it
            logger.warn("Unable to create ArsEntry.  " + e.toString());
            response.addMessage(new Message("InternalException", e.getMessage()));
            Controller.sendResponse(response, httpResponse);
        }
        // If there were any unexpected exceptions
        catch (Exception e) {
            // Build the Response and send it
            logger.error("An unexpected error has occured: " + e.toString());
            response.addMessage(new Message("UnexpectedException", e.toString()));
            Controller.sendResponse(response, httpResponse);
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
        
        return null;
    }

    /**
     * 
     */
    public ActionForward delete(ActionMapping mapping, ActionForm form, 
                                HttpServletRequest httpRequest, 
                                HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "delete";
        logger.info("Framework method " + requestMethod + " called: Deleting an entry.");
        
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
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String entryID = mapping.getProperty("EntryID");
        
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
            super.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                logger.debug("EntryID: " + entryID);
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsEntry entry = ArsEntry.delete(context, structureID, entryID);

                // Get the messages
                response.addMessages(entry.getMessages());
                ArsController.logMessages(logger, entry.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(entry.generateXmlElement());
                this.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Entry could not be deleted, " + messages.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                Controller.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve the entry list.  " + e.toString());
                response.addMessage(new Message("InternalException", e.getMessage()));
                Controller.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                Controller.sendResponse(response, httpResponse);
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
        
        return null;
    }
        
    public ActionForward entry(ActionMapping mapping, ActionForm form, 
                               HttpServletRequest httpRequest, 
                               HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "entry";
        logger.info("Framework method " + requestMethod + " called: Retrieving an entry.");
        
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
        String structureID = mapping.getProperty("StructureID");
        String entryIDString = mapping.getProperty("EntryID");
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
            super.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                
                // Build up the EntryID array
                String[] entryIDs = entryIDString.split(",");
                logger.debug("EntryIDs[" + entryIDs.length + "]: " + entryIDString);
                
                // Check for valid items
                long[] items = new long[0];
                if (itemsString == null) {
                    items = null;
                    logger.debug("Items: Not provided, defaulting to retrieving all items with non-null.");
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
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                for (int i=0;i<entryIDs.length;i++) {
                    // Retrieve the entry
                    ArsEntry entry = new ArsEntry(context, structureID, entryIDs[i], items);

                    // Get the messages
                    response.addMessages(entry.getMessages());
                    ArsController.logMessages(logger, entry.getMessages());
                 
                    // Add the entry to the response 
                    response.addResult(entry.generateXmlElement());
                }
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                Controller.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Entry could not be retrieved, " + messages.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                Controller.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve an entry list.  " + e.toString());
                response.addMessage(new Message("InternalException", e.getMessage()));
                Controller.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                Controller.sendResponse(response, httpResponse);
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
        
        return null;
    }
    
    /**
     * 
     */
    public ActionForward entries(ActionMapping mapping, ActionForm form, 
                                 HttpServletRequest httpRequest, 
                                 HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "entries";
        logger.info("Framework method " + requestMethod + " called: Obtaining an entry list.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !(curParameter.equals("includenullitems") ||
                   curParameter.equals("items") || 
                   curParameter.equals("limit") ||
                   curParameter.equals("qualification") ||
                   curParameter.equals("range") ||
                   curParameter.equals("sort") ||
                   curParameter.equals("sortorder") ||
                   curParameter.equals("target")
                  )) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String includeNullItemsString = httpRequest.getParameter("includenullitems");
        String itemsString = httpRequest.getParameter("items");
        String limitString = httpRequest.getParameter("limit");
        String qualificationString = httpRequest.getParameter("qualification");
        String rangeString = httpRequest.getParameter("range");
        String sortString = httpRequest.getParameter("sort");
        String sortOrderString = httpRequest.getParameter("sortorder");
        String targetString = httpRequest.getParameter("target");
        
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
            super.requestBasicAuth(httpResponse);
        }
        // If there was context information 
        else {
            try {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                
                // Generate an array of item IDs to include
                long[] includeItems = new long[0];
                if (itemsString != null) { 
                    String[] itemStringList = itemsString.split(",");
                    includeItems = new long[itemStringList.length];
                    logger.debug("Items[" + includeItems.length + "]: " + itemsString);
                    for(int i=0;i<itemStringList.length;i++) {
                        try { includeItems[i] = Long.parseLong(itemStringList[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the items list to numberical values, \"" + itemStringList[i] + "\" is not a valid number."); }
                    }
                } else {
                    logger.debug("Items[0]: Not provided, defaulting to none.");
                }
                
                // Check for valid includeallElements
                boolean includeNullItems = true;
                if (includeNullItemsString == null) {
                    logger.debug("Includenullitems: Not provided, defaulting to \"true\".");
                } else if (includeNullItemsString.equalsIgnoreCase("true")) {
                    logger.debug("Includenullitems: true");
                } else if (includeNullItemsString.equalsIgnoreCase("false")) {
                        includeNullItems = false;
                    logger.debug("Includenullitems: false");
                } else {
                    logger.warn("Include: Invalid Includenullitems \"" + includeNullItemsString + "\", defaulting to \"false\".");
                    response.addMessage(new Message("InvalidParameter", "Unable to process the Includenullitems prameter, \"" + includeNullItemsString + "\" is not a valid value."));
                }
                    
                // Obtain an integer limit for the number of entries to retrieve
                Integer limit = null;
                if (limitString != null) {
                    logger.debug("Limit: " + limitString);
                    try { limit = Integer.valueOf(limitString); }
                    catch (NumberFormatException e) { throw new ControllerException("Unable to convert the limit \"" + limitString + "\" to a numerical value."); }
                } else {
                    logger.debug("Limit: Not provided, defaulting to server setting.");
                }
                
                // Check for valid qualification
                String qualification = "1=1";
                if (qualificationString == null) {
                    logger.debug("Qualification: Not provided, defaulting to \"" + qualification + "\".");
                } else {
                    qualification = qualificationString;
                    logger.debug("Qualification: " + qualificationString);
                }
                
                // Obtain the range of entries to retrieve
                int[][] ranges = new int[0][0];
                if (rangeString != null && !rangeString.equals("")) {
                    String[] rangeStrings = rangeString.split(",");
                    ranges = new int[rangeStrings.length][];
                    for (int i=0;i<ranges.length;i++) {
                        // If we are looking at an inclusive range value
                        if (rangeStrings[i].indexOf("-") != -1) {
                            String[] endpoints = rangeStrings[i].split("-");
                            if (endpoints.length != 2) { throw new ControllerException("Unable to generate range, the range \"" + rangeStrings[i] + "\" is not a valid range.  Please indicate a single starting and ending number separated by a \"-\"."); }
                            else {
                                int start = 0;
                                int end = 0;
                                
                                try { start = Integer.parseInt(endpoints[0]); }
                                catch (NumberFormatException e) { throw new ControllerException("Unable to generate range, the starting value \"" + endpoints[0] + "\" is not a valid number."); }
                                    
                                try { end = Integer.parseInt(endpoints[1]); }
                                catch (NumberFormatException e) { throw new ControllerException("Unable to generate range, the ending value \"" + endpoints[1] + "\" is not a valid number."); }
                                
                                if (start > end) { throw new ControllerException("Unable to generate range, the starting value \"" + endpoints[0] + "\" can't be greater then the ending value \"" + endpoints[1] + "\"."); }
                                
                                int numberOfEntries = end - start + 1;
                                ranges[i] = new int[numberOfEntries];
                                for (int j=0;j<numberOfEntries;j++) { ranges[i][j] = start+j; }
                            }
                        }
                        // If we are looking at a single value
                        else {
                            ranges[i] = new int[1];
                            try { ranges[i][0] = Integer.parseInt(rangeStrings[i]); }
                            catch (NumberFormatException e) { throw new ControllerException("Unable to convert range description to integers.  \"" + rangeStrings[i] + "\" is not a valid number."); }
                        }
                    }
                    
                    // Print the ranges
                    if (ranges.length > 0) {
                        String rangesAttributeValue = "";
                        for (int i=0;i<ranges.length;i++) {
                            if (ranges[i] != null && ranges[i].length > 0) {
                                if (rangesAttributeValue != "") { rangesAttributeValue += ","; }
                                if (ranges[i].length > 1) {
                                    rangesAttributeValue += ranges[i][0] + "-" + ranges[i][ranges[i].length-1];
                                } else {
                                    rangesAttributeValue += ranges[i][0];
                                }
                            }
                        }
                        logger.debug("Ranges: " + rangesAttributeValue);
                    }
                } else {
                    logger.debug("Range: Not provided, defaulting to none.");
                }
                
                // Check for a valid sortorder
                String sortOrder = "ascending";
                if (sortOrderString == null) {
                    logger.debug("SortOrder: Not provided, defaulting to \"ascending\".");
                } else if (sortOrderString.equalsIgnoreCase("ascending")) {
                    logger.debug("SortOrder: ascending");
                } else if (sortOrderString.equalsIgnoreCase("decending")) {
                    sortOrder = "decending";
                    logger.debug("SortOrder: decending");
                } else {
                    logger.warn("SortOrder: Invalid sortorder \"" + sortOrderString + "\", defaulting to \"increasing\".");
                    response.addMessage(new Message("InvalidParameter", "Unable to process the sortorder parameter, \"" + sortOrderString + "\" is not a valid sorting order."));
                }    
                
                // Obtain the list of items to sort by
                long[] sortItems = new long[1];
                if (sortString != null) {
                    String[] sortItemsArray = sortString.split(",");
                    sortItems = new long[sortItemsArray.length];
                    logger.debug("SortItems[" + sortItems.length + "]: " + sortString);
                    for (int i=0;i<sortItemsArray.length;i++) {
                        // If there is a sort order specified for this item reconfigure the value to be a valid number
                        if (sortItemsArray[i].endsWith("+")) { 
                            sortItemsArray[i] = sortItemsArray[i].substring(0,sortItemsArray[i].lastIndexOf("+")); 
                        } else if (sortItemsArray[i].endsWith(" ")) {
                            sortItemsArray[i] = sortItemsArray[i].substring(0,sortItemsArray[i].lastIndexOf(" ")); 
                        } else if (sortItemsArray[i].endsWith("-")) {
                            sortItemsArray[i] = "-" + sortItemsArray[i].substring(0,sortItemsArray[i].lastIndexOf("-")); 
                        } else if (sortOrder == "decending") {
                            sortItemsArray[i] = "-" + sortItemsArray[i];
                        }
                        
                        try { sortItems[i] = Long.parseLong(sortItemsArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the sort items list to numerical values, \"" + sortItemsArray[i] + "\" is not a valid number."); }
                    }
                } else {
                    if (sortOrder == "ascending") { sortItems[0] = 1; }
                    else { sortItems[0] = -1; }
                    
                    logger.debug("SortItems[1]: Not provided, defaulting to sorting on unique id.");
                }
                
                    // Check for a valid target
                String target = "all";
                if (targetString == null) {
                    logger.debug("Target: Not provided, defaulting to \"all\".");
                } else if (targetString.equalsIgnoreCase("all")) {
                    logger.debug("Target: all");
                } else if (targetString.equalsIgnoreCase("max")) {
                    target = "max";
                    logger.debug("Target: max");
                } else if (targetString.equalsIgnoreCase("min")) {
                    target = "min";
                    logger.debug("Target: min");
                } else if (targetString.equalsIgnoreCase("rand")) {
                    target = "rand";
                    logger.debug("Target: rand");
                } else {
                    logger.warn("Target: Unable to process target \"" + targetString + "\".");
                    response.addMessage(new Message("InvalidParameter", "Unable to process the target parameter, \"" + targetString + "\" is not a valid target."));
                }
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsEntryList entryList = new ArsEntryList(context, structureID, includeItems, limit, qualification, ranges, sortItems, target, includeNullItems);

                // Get the messages
                response.addMessages(entryList.getMessages());
                ArsController.logMessages(logger, entryList.getMessages());
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                response.addResult(entryList.generateXmlElement());
                Controller.sendResponse(response, httpResponse);
            }
            // If there was a problem generating the context
            catch (ARException e) {
                // Get the messages
                logger.debug("Retrieving last statuses...");
                StatusInfo[] infos = e.getLastStatus();
                Message[] messages = ArsObject.generateMessages(infos);
                
                // Respond
                logger.warn("Entries could not be retrieved, " + messages.length + " messages recieved.");
                response.addMessages(messages);
                ArsController.logMessages(logger, messages);
                Controller.sendResponse(response, httpResponse);
            }
            // If the authentication wasn't of type basic.
            catch (ControllerException e) {
                // Build the Response and send it
                logger.warn("Unable to retrieve entries.  " + e.toString());
                response.addMessage(new Message("InternalException", e.getMessage()));
                Controller.sendResponse(response, httpResponse);
            }
            // If there were any unexpected exceptions
            catch (Exception e) {
                // Build the Response and send it
                logger.error("An unexpected error has occured: " + e.toString());
                response.addMessage(new Message("UnexpectedException", e.toString()));
                Controller.sendResponse(response, httpResponse);
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
    public ActionForward update(ActionMapping mapping, ActionForm form, 
                                HttpServletRequest httpRequest, 
                                HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "update";
        logger.info("Framework method " + requestMethod + " called: Updating an entry.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !(curParameter.equals("entry") || curParameter.equals("returnentries") || curParameter.equals("qualification") || curParameter.equals("items")) ) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String structureID = mapping.getProperty("StructureID");
        String entryID = mapping.getProperty("EntryID");
        String itemsString = httpRequest.getParameter("items");
        String returnEntriesString = httpRequest.getParameter("returnentries");
        String qualificationString = httpRequest.getParameter("qualification");
        
        // Declare default ArsContext
        ArsContext context = null;
        
        try {
            // Get the entry string
            String entryString = "";
            if (httpRequest.getContentType() != null && httpRequest.getContentType().equals("application/xml")) {
                logger.debug("ContentType: application/xml");
                String curString;
                while ((curString = httpRequest.getReader().readLine()) != null) {
                    entryString += curString;
                }
            } else if (httpRequest.getMethod().equals("GET")) {
                logger.debug("RequestMethod: GET");
                entryString = httpRequest.getParameter("entry");
            } else if (httpRequest.getMethod().equals("POST")) {
                logger.debug("RequestMethod: POST");
                entryString = httpRequest.getHeader("entry");
            } else {
                String msg = "Unable to process request, invalid Http Method: " + httpRequest.getMethod();
                logger.warn(msg);
                throw new ControllerException(msg);
            }

            // Obtain the parameters required for generating a context
            String datasource = mapping.getProperty("Datasource");
            String basicauth = httpRequest.getHeader("Authorization");
            logger.debug("Datasource: " + datasource);
            if (basicauth != null) { logger.debug("Recieved authorization header."); }

            // If there is no context information in the Url or as Http Basic Authentication information
            if (datasource.lastIndexOf("@") < 0 && basicauth == null) {
                // Send the 401 Response to request authentication
                super.requestBasicAuth(httpResponse);
            }
            // If there was context information 
            else {
                // Log the call parameters
                logger.debug("StructureID: " + structureID);
                logger.debug("ExplicitEntryID: " + entryID);
                logger.debug("EntryString: " + entryString);
                
                // Verify we have a valid value for returnentry
                boolean returnEntries = false;
                if (returnEntriesString == null) {
                    logger.debug("ReturnEntries: Not present, defaulting to \"false\".");
                } else if (returnEntriesString.equalsIgnoreCase("true")) {
                    logger.debug("ReturnEntries: true");
                    returnEntries = true;
                } else if (returnEntriesString.equalsIgnoreCase("false")) { 
                    logger.debug("ReturnEntries: false");
                } else {
                    logger.debug("ReturnEntries: Invalid returnentries value \"" + returnEntriesString + "\", defaulting to \"false\".");
                    response.addMessage(new Message("InvalidParameter", "Unable to process the returnentry parameter, \"" + returnEntriesString + "\" is not a valid parameter.  Defaulting to false."));
                    returnEntries = false;
                }
                
                long[] items = null;
                if (itemsString != null) {
                    if (returnEntries == false) {
                        logger.debug("Items: ReturnEntries was false, but items requested.  Implicetly setting returnentries to true.");
                        returnEntries = true;
                    }

                    String[] itemsArray = itemsString.split(",");
                    items = new long[itemsArray.length];
                    logger.debug("Items[" + items.length + "]: " + itemsString);
                    for (int i=0;i<itemsArray.length;i++) {
                        try { items[i] = Long.parseLong(itemsArray[i]); }
                        catch (NumberFormatException e) { throw new ControllerException("Unable to convert the items list to numerical values, \"" + itemsArray[i] + "\" is not a valid number."); }
                    }
                }
                
                // Verify we have a valid value for qualification
                String qualification = null;
                if (qualificationString == null && entryID != null) {
                    if (entryID != null) { qualification = "'1'=\"" + entryID + "\""; }
                    logger.debug("Qualification [Using Explicit EntryID]: " + qualification);
                } else if (qualificationString != null && entryID == null) {
                    qualification = qualificationString;
                    logger.debug("Qualification [Specified]: " + qualificationString);
                } else if (qualificationString != null && entryID != null) {
                    throw new ControllerException("Unable to update.  The 'qualification' parameter may not be used with an explicit entry id.");
                }
                
                // Generate the context
                context = ArsController.generateContext(datasource, basicauth);
                
                // Build the Configuration object
                ArsEntryList entryList = ArsEntry.update(context, structureID, qualification, entryString);
                
                // Get the update messages
                response.addMessages(entryList.getMessages());
                ArsController.logMessages(logger, entryList.getMessages());
                
                // Check to see if we need to retrieve the new entry
                if (returnEntries) {
                    logger.debug("Retrieving the updated entries...");
                    String[] entryIds = entryList.getGenericEntryList().getEntryIDs();
                    for (int i=0;i<entryIds.length;i++) {
                        // Retrieve the entry
                        ArsEntry entry = new ArsEntry(context, structureID, entryIds[i], items);
                        String success = entryList.getGenericEntryList().getGenericEntry(entryIds[i]).getAttribute("Success");
                        entry.addAttribute("Success", success);
                        
                        response.addResult(entry.generateXmlElement());
                    }
                }
                
                // Respond
                response.setSuccess(Boolean.TRUE);
                Controller.sendResponse(response, httpResponse);
            }
        // If there was a problem generating the context
        } catch (ARException e) {
            // Get the messages
            logger.debug("Retrieving last statuses...");
            StatusInfo[] infos = e.getLastStatus();
            Message[] messages = ArsObject.generateMessages(infos);

            // Respond
            logger.warn("Entry could not be updated, " + messages.length + " messages recieved.");
            response.addMessages(messages);
            ArsController.logMessages(logger, messages);
            Controller.sendResponse(response, httpResponse);
        }
        // If the authentication wasn't of type basic.
        catch (ModelException e) {
            // Build the Response and send it
            logger.warn("Unable to update the entry.  " + e.toString());
            response.addMessage(new Message("ModelException", e.getMessage()));
            Controller.sendResponse(response, httpResponse);
        }
        // If the authentication wasn't of type basic.
        catch (ControllerException e) {
            // Build the Response and send it
            logger.warn("Unable to update the entry.  " + e.toString());
            response.addMessage(new Message("InternalException", e.getMessage()));
            Controller.sendResponse(response, httpResponse);
        }
        // If there were any unexpected exceptions
        catch (Exception e) {
            // Build the Response and send it
            logger.error("An unexpected error has occured: " + e.toString());
            StackTraceElement[] st = e.getStackTrace();
            for (int i=0;i<st.length;i++) {
                logger.debug(st[i].toString());
            }
            response.addMessage(new Message("UnexpectedException", e.toString()));
            Controller.sendResponse(response, httpResponse);
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
        
        return null;
    }
}