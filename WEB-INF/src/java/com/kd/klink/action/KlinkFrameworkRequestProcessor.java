package com.kd.klink.action;

import com.kd.klink.control.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.apache.struts.util.*;

import com.kd.klink.model.response.Message;
import com.kd.klink.model.response.Response;

/**
 * This class is the second point of control in the Klink processing chain, it
 * is responsible for catching any exceptions thrown by a Klink Controller and
 * returning the message wrapped by the standard Klink Response xml.  All
 * requests of the klink framework are first passed to the RequestProcessor
 * servlet, which decides if the request is for static content, Klink 
 * management, or an actual framework request.  Framework requests are passed 
 * from the RequestProcessor through the KlinkFrameworkRequestProcessor and into
 * the struts ActionServlet which makes the appropriate controller calls.
 */
public class KlinkFrameworkRequestProcessor extends org.apache.struts.action.ActionServlet {
    // Define the logger.
    private static final Logger logger = Logger.getLogger(KlinkFrameworkRequestProcessor.class);
    
    /**
     * Initializes the servlet.
     *
     * @throws ServletException  If there is a problem initializing the servlet.
     * @see org.apache.struts.action.ActionServlet
     */
    public KlinkFrameworkRequestProcessor(ServletConfig config) throws ServletException {  super.init(config); }
    
    /**
     * Attempts to pass the request to the Struts ActionServlet process method.
     * If any ServletExceptions are thrown by the ActionServlet they are caught
     * and returned to the user encapsulated as a response xml reply.
     */
    protected void process(HttpServletRequest request, 
                           HttpServletResponse response,
                           String methodName) throws java.io.IOException {
        
        // Create the default response
        Response klinkResponse = new Response(Boolean.FALSE);

        // Attempt to pass the request to the Struts ActionServlet
        try {
            logger.debug("Processing " + methodName + " request...");
            super.process(request, response);
        }
        // If the Struts ActionServlet had difficulty with processing the request
        catch (javax.servlet.ServletException e) {
            logger.error("There was a problem processing the request.");
            StringBuffer requestUrl = request.getRequestURL();
            
            // Set the response
            klinkResponse.setRequestMethod(methodName);

            // Log the Root Cause if it exists
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) { logger.debug("RootCause: " + rootCause.toString()); }

            // Get the exception message
            String msgString = e.getMessage();
            
            // If the message in indicative of a malformed request, change it to be a more intelligent message
            if (msgString != null && msgString.equalsIgnoreCase("No action config found for the specified url.")) {
                // Log the error
                logger.warn(msgString);
                
                // Change the message to be intelligable to the end user
                msgString = "Unable to process request, there is no framework call matching the request.";
            } else {
                // If the message string is null change it to the Exception description
                if (msgString == null) { msgString = e.toString(); } 
                
                // If we are debugging print a full stack trace
                if (logger.isDebugEnabled()) {
                    logger.debug("StackTrace: ");
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for (int i=0;i<stackTrace.length;i++) {
                        logger.debug("   " + stackTrace[i].toString());
                    }
                }
                // If we are not in debug mode just print the error
                else {
                    logger.error(msgString);
                }
            }

            // Create a new InternalException message and add it to the response
            Message message = new Message("InternalException", msgString);
            klinkResponse.addMessage(message);

            // Send the error response back to the user.
            logger.info("Preparing error response...");
            Controller.sendResponse(klinkResponse, response);
        }
    }
}
