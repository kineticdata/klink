package com.kd.klink.ars.model;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

/**
 * Wrapper class for the com.remedy.arsys.api.ARServerUser object that adds 
 * source port and static rpc functionality.
 */
public class ArsContext extends ArsObject {
    private static final Logger logger = Logger.getLogger(ArsContext.class.getName());
    
    private ARServerUser context = null;
    private String username = null;
    private String password = null;
    private String server = null;
    private int port = 0;
    private int rpc = 0;
    
    public ArsContext(String username, String password, String server, int port, int rpc) throws ARException {
        logger.debug("Constructing new ArsContext(" + username + ", " + password + ", " + server + ", " + port + ", " + rpc + ")...");
        this.username = username;
        this.password = password;
        this.server = server;
        this.port = port;
        this.rpc = rpc;
        
        try {
            logger.debug("Building ARServerUser object...");
            context = new ARServerUser(this.getUsername(), this.getPassword(), "", this.getServer());

            logger.debug("Setting ARServerUser ports...");
            Util.ARSetServerPort(context, new NameID(getServer()), this.port, this.rpc);
            
            // Add the Context Messages
            logger.debug("Adding last status messages...");
            super.addMessages(context.getLastStatus());
        } catch (ARException e) {
            logger.error(e.toString());
            throw e;
        } catch (Exception e) {
            logger.error(e.toString());
            StatusInfo info = new StatusInfo(Constants.AR_RETURN_FATAL, 10000, e.getClass().getName(), e.getMessage());
            StatusInfo[] infos = {info};
            throw new ARException(infos);
        }
        
        logger.debug("ArsContext constructed.");
    }
    
    public void login() throws ARException {
        this.context.login();
        super.addMessages(this.context.getLastStatus());
    }
    
    public void logout() {
        this.context.logout();
    }

    /**
     * Returns the ARServerUser object associated with the calling HelperContext
     * object.  If the ARServerUser object is null, it will first be created 
     * based on the properties of the wrapping HelperContext.
     */
    public ARServerUser getContext() { return context; }
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public String getServer() { return this.server; }
    public int getPort() { return this.port; }
    public int getRpc() { return this.rpc; }

    // Set methods
    public void setUsername(String username) { 
        this.username = username;
        context.setUser(new AccessNameID(username));
    }
    public void setPassword(String password) { 
        this.password = password;
        context.setPassword(new AccessNameID(password));
    }
    public void setServer(String server) {
        this.server = server;
        context.setServer(server);
    }
    public void setPort(int port) throws ARException {
        this.port = port;
        Util.ARSetServerPort(context, new NameID(getServer()), this.port, this.rpc);
    }
    public void setRpc(int rpc) throws ARException {
        this.rpc = rpc;
        Util.ARSetServerPort(context, new NameID(getServer()), this.port, this.rpc);
    }
    
    public Element generateXmlElement() {
        Element arsContext = null;
        
        // Build the response message
        try {
            // Obtain a document builder
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            // Generate the root and child elements
            arsContext = doc.createElement("RemedyContext");
            Element userElement = doc.createElement("Username");
            //Element passElement = doc.createElement("Password");
            Element servElement = doc.createElement("Server");
            Element portElement = doc.createElement("Port");
            Element rpcElement = doc.createElement("Rpc");

            // Set the values of the child elements
            userElement.appendChild(doc.createTextNode(this.getUsername()));
            //passElement.appendChild(doc.createTextNode(this.getPassword()));
            servElement.appendChild(doc.createTextNode(this.getServer()));
            portElement.appendChild(doc.createTextNode(String.valueOf(this.getPort())));
            rpcElement.appendChild(doc.createTextNode(String.valueOf(this.getRpc())));

            // Add the child elements to the root node
            arsContext.appendChild(userElement);
            //arsContext.appendChild(passElement);
            arsContext.appendChild(servElement);
            arsContext.appendChild(portElement);
            arsContext.appendChild(rpcElement);
        }
        // Because we are using the default DocumentBuilder configuration this should never be thrown
        catch (ParserConfigurationException e) {
            logger.fatal("There was a problem generating the XmlElement for a Response.  " + e.toString());
        }     

        return arsContext;
    }
}