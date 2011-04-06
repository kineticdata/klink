package com.kd.klink.control;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.*;

import org.apache.struts.action.*;
import org.apache.struts.actions.*;

import com.kd.klink.logging.*;
import com.kd.klink.model.*;
import com.kd.klink.model.response.*;

/**
 * This controller processes any requests for logging modification of the live
 * Klink web application.
 */
public class LogController extends Controller {
    private static final Logger logger = Logger.getLogger(LogController.class.getName());

    /**
     * Process a request for live modification of the logging of Kinetic Link.
     * This method will check the http request for two parameters, "configfile"
     * and "configproperties".  If the "configfile" parameter is present it will
     * load the configuration from the specified file.  If the "configproperties"
     * parameter is present it will modify the logging configuration parameters
     * based on the name/value pairs provided (see below for formatting 
     * instructions).  In all cases a Response is returned (which has a single
     * LogConfig child element to the Reply element).
     *
     * The configproperties parameter should contain names and values separated
     * with colons, and having each pair separated by a semi-colon.  For 
     * example: 
     * 
     * log4j.appender.fileAppender:org.apache.log4j.FileAppender;log4j.appender.fileAppender.File:C:\Program Files\Klink\logs\klink.log
     */
    public ActionForward logconfig(ActionMapping mapping, ActionForm form, 
                                   HttpServletRequest httpRequest, 
                                   HttpServletResponse httpResponse) {
        // Set the required parameters
        String requestMethod = "logconfig";
        logger.info("Framework method " + requestMethod + " called: Modifying logging environment.");
        
        // Initialize the Response
        Response response = new Response(requestMethod);
        
        // Check for invalid Http parameters
        Enumeration httpParameters = httpRequest.getParameterNames();
        while(httpParameters.hasMoreElements()) {
            String curParameter = (String)httpParameters.nextElement();
            if ( !(curParameter.equals("configfile") || curParameter.equals("configproperties")) ) {
                String message = "Unrecognized HTTP parameter \"" + curParameter + "\", parameter ignored.";
                logger.warn(message);
                response.addMessage(new Message("InvalidParameter", message));
            }
        }
        
        // Get the required call parameters
        String configFileString = httpRequest.getParameter("configfile");
        String configPropertiesString = httpRequest.getParameter("configproperties");
        
        try {
            // Log the parameters
            logger.debug("ConfigFile: " + configFileString);
            
            // Process the configPropertiesString
            Properties properties = new Properties();
            if (configPropertiesString != null) {
                String[] configPropertiesArray = configPropertiesString.split(";");
                logger.debug("Configproperties[" + configPropertiesArray.length + "]: " + configPropertiesString);
                for(int i=0;i<configPropertiesArray.length;i++) {
                    int delimiter_location = configPropertiesArray[i].indexOf(":");
                    if (delimiter_location < 0) {
                        throw new ControllerException("Unable to parse configproperties, \"" + configPropertiesArray[i] + "\" is not a valid property.  Please use the format \"<property_name>:<value>\".");
                    }
                    String key = configPropertiesArray[i].substring(0, delimiter_location);
                    String val = configPropertiesArray[i].substring(delimiter_location+1);
                    properties.put(key, val);
                }
            } else {
                logger.debug("ConfigProperties: " + configPropertiesString);
            }

            // Retrieve the LogConfig object
            LogConfig config = null;
            if (configFileString == null && configPropertiesString == null) {
                logger.debug("Retrieving log config...");
                config = KlinkLogManager.getLogConfig();
            } else if (configFileString != null && configPropertiesString != null) {
                logger.debug("Loading config from file...");
                if (configFileString.equalsIgnoreCase("DEFAULT")) {
                    KlinkLogManager.loadDefaultConfigFile();
                } else {
                    KlinkLogManager.loadConfigFile(configFileString);
                }
                logger.debug("Loading config from custom properties...");
                config = KlinkLogManager.loadProperties(properties);
            } else if (configFileString != null) {
                logger.debug("Loading config from file...");
                if (configFileString.equalsIgnoreCase("DEFAULT")) {
                    config = KlinkLogManager.loadDefaultConfigFile();
                } else {
                    config = KlinkLogManager.loadConfigFile(configFileString);
                }
            } else if (configPropertiesString != null) {
                logger.debug("Loading config from custom properties...");
                config = KlinkLogManager.loadProperties(properties);
            }

            // Respond
            response.setSuccess(Boolean.TRUE);
            response.addResult(config.generateXmlElement());
            super.sendResponse(response, httpResponse);
        }
        // If the authentication wasn't of type basic.
        catch (ControllerException e) {
            // Build the Response and send it
            logger.warn("Unable to retrieve log configuration information.  " + e.toString());
            response.addMessage(new Message("InternalException", e.getMessage()));
            super.sendResponse(response, httpResponse);
        }
        // If there was a problem loading the log config file
        catch (IOException e) {
            // Build the Response and send it
            logger.error("An unexpected error has occured loading the log configuration file: " + e.toString());
            response.addMessage(new Message("InternalException", "An unexpected error has occured loading the log configuration file: " + e.getMessage()));
            super.sendResponse(response, httpResponse);
        }
        // If there were any unexpected exceptions
        catch (Exception e) {
            // Build the Response and send it
            logger.error("An unexpected error has occured: " + e.toString());
            response.addMessage(new Message("UnexpectedException", e.toString()));
            super.sendResponse(response, httpResponse);
        }
        
        return null;
    }
}
