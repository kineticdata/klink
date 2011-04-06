package com.kd.klink.ars.control;

import java.io.*;

import javax.servlet.http.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.control.*;
import com.kd.klink.ars.model.*;
import com.kd.klink.ars.util.ArsUtil;
import com.kd.klink.model.response.Message;

/**
 *
 */
public class ArsController extends Controller {
    private static final Logger logger = Logger.getLogger(ArsController.class.getName());

    public static void logMessages(Logger logger, Message[] messages) {
        for (int i=0;i<messages.length;i++) {
            logger.info(messages[i].getMessage());
        }
    }
    
    public static void prepareForLogout(ArsContext context) {
        try {
            if (context != null) {
                context.getContext().login();
                
                EntryListFieldInfo [] fieldlist = new EntryListFieldInfo[1];
                FieldID field = new FieldID(1);
                fieldlist[0] = new EntryListFieldInfo(field);
                EntryCriteria entrycrit = new EntryCriteria(fieldlist);
                entrycrit.setEntryListFieldInfo(fieldlist);

                QualifierInfo qual = Util.ARGetQualifier(context.getContext(), "1=0", null, null, Constants.AR_QUALCONTEXT_DEFAULT);
                EntryListCriteria listCriteria = new EntryListCriteria(new NameID("User"), qual, 0, new Timestamp(0), null, null);

                EntryFactory entfact = EntryFactory.getFactory();
            }
        } catch (ARException e) {
            // Do nothing, we don't really care if this is successful or not, just making the call is what allows us to logout.
        }
    }
    
    /**
     * Given the datasource (provided in the Url) and the value of the Http Authorization header, this
     * method parses through and generates an ArsContext object.  If the datasource contains username and
     * password information it will override any information provided as part of the Authorization header.
     * The datasource will look for the last "@" to deliminate between the user/password and server information.
     * If the datasource contains user/password information the username and password will be split based on the
     * first occurance of the ":" character.  If there is no "@" in the datasource, the Authorization header value
     * is examined.
     * 
     * @param   datasource              This is the section of the Url which specifies the target Ars server
     *                                  (and can optionally specify the username/password and TCP/RPC server ports).
     * @param   basicauth               This is the value of the "Authorization" header from the Http request.
     * @throws  ARException             If an exception was thrown generating the ArsContext object.  
     *                                  This will typically occur when an invalid TCP or RPC port is specified.
     * @throws  ArsControllerException  If non-BASIC Http authentication was specified by the user.
     * @returns ArsContext              The wrapper object that represents the user/server info required
     *                                  to connect to an Ars server.
     */
    public static ArsContext generateContext(String datasource, String basicauth) throws ARException, ControllerException {
        logger.info("Generating the context information...");
        
        // Declare the variables we are trying to obtain
        String user = new String("");
        String pass = new String("");
        String language = new String("");
        String server = new String("");
        int port = 0;
        int rpc = 0;

        // If the User/Pass information is in the Url
        if (datasource.lastIndexOf("@") > -1) {
            // Split the user information from the server information
            String userinfo = datasource.substring(0, datasource.lastIndexOf("@"));
            datasource = datasource.substring(datasource.lastIndexOf("@") + 1);

            // If there is password information included set both the user and pass
            if (userinfo.indexOf(":") > -1) {
                user = userinfo.substring(0, userinfo.indexOf(":"));
                pass = userinfo.substring(userinfo.indexOf(":") + 1);
            }
            // If no password information was included assume its blank
            else {
                user = userinfo;
                pass = "";
            }
        }
        // If the User/Pass information is included in the Http request
        else if (basicauth != null) {
            // Check to make sure the authentication is sent in BASIC format
            if (!basicauth.toUpperCase().startsWith("BASIC ")) {
                throw new ControllerException("Only BASIC Http authentication supported.");
            }

            // Get encoded user and password, comes after "BASIC "
            String userpassEncoded = basicauth.substring(6);

            // Decode it, using any base 64 decoder
            try {
                sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
                String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));
                user = userpassDecoded.substring(0, userpassDecoded.indexOf(":"));
                pass = userpassDecoded.substring(userpassDecoded.indexOf(":") + 1);
            } catch (Exception e) {
                logger.error(e.toString());
                throw new ControllerException(e.toString());
            }
        }

        // Obtain the server information
        String[] parts = datasource.split(":");
        if (parts.length >= 1) { server = parts[0]; }
        if (parts.length >= 2) { port = Integer.parseInt(parts[1]); }
        if (parts.length >= 3) { rpc = Integer.parseInt(parts[2]); }

        // Build and return the ArsContext object
        ArsContext context = new ArsContext(user, pass, server, port, rpc);
        
        return context;
    }
}
