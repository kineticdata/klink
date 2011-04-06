package com.kd.klink.control;

import java.io.*;
import javax.servlet.http.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.*;

import org.apache.struts.action.*;
import org.apache.struts.actions.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.remedy.arsys.api.*;

import com.kd.klink.model.*;
import com.kd.klink.model.response.*;

/**
 * This class provides the base functionality of a Klink controller.  Klink
 * controllers extend the Struts MappinDesipatchAction and contain the logic to
 * actually process framework requests.
 */
public class Controller extends MappingDispatchAction {
    // Initialize the logger
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    
    /**
     * Send an http Unauthorized response to request basic authentication.
     */
    public static void requestBasicAuth(HttpServletResponse response) {
        logger.info("Missing authentication, requesting Basic Authorization... ");
        try {
            // Send a 401 Unauthorization header
            response.setHeader("WWW-Authenticate", "BASIC realm=\"everyone\"");
            response.sendError(response.SC_UNAUTHORIZED);
        } catch (IOException e) {
            // If we can't send a response now there is no way to return an error to the user
            logger.fatal("Unable to send Http authentication request.  " + e.toString());
        }
    }
    
    /**
     * Send an http response containing the Klink response as xml content.
     */
    public static void sendResponse(Response response, HttpServletResponse httpResponse) {
        logger.info("Sending back the response...");
        try {
            httpResponse.setContentType("text/xml");
            PrintWriter out = httpResponse.getWriter();
            out.println(Controller.convertElementToString(response.generateXmlElement()));
            out.flush();
        } catch (IOException e) {
            // If we can't send a response now there is no way to return an error to the user
            logger.fatal("Unable to send the response.  " + e.toString());
        }
    }
    
    /**
     * Convert and return the string representation of the provided element 
     * parameter.
     */
    public static String convertElementToString(Element element) {
        logger.debug("Converting the element to an Xml string...");
        String returnString = new String("");

        try {
            // Create and configure an xml transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            // Write the DOM document to a String Buffer
            StringWriter stringWriter = new StringWriter();
            Source source = new DOMSource(element);
            Result result = new StreamResult(stringWriter);
            transformer.transform(source, result);

            // Set the returnString
            returnString = stringWriter.toString();
        }
        // Because we are using the default TransformerFactory configuration this should never be thrown
        catch (TransformerConfigurationException e) {
            logger.fatal("There was a problem converting the XmlElement to a string.  " + e.toString());
        }
        // Because we are using the default TransformerFactory configuration this should never be thrown
        catch (TransformerException e) {
            logger.fatal("There was a problem converting the XmlElement to a string.  " + e.toString());
        }
        
        return returnString;
    }
}
