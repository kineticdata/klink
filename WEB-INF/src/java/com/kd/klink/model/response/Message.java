package com.kd.klink.model.response;

import com.kd.klink.model.*;
import java.util.LinkedHashMap;
import java.util.Iterator;

import org.w3c.dom.*;

/**
 *
 */
public class Message extends ModelObject {
    // Variables
    private String type = new String("");
    private String message = new String("");
    
    private LinkedHashMap attributes = new LinkedHashMap();
    
    // Constructors
    public Message() {}
    public Message(String type, String message) {
        this.type = type;
        this.message = message;
    }
    
    // Type beanish methods
    public String getType() { return this.type; }
    public void setType(String type) { 
        if (type == null || type == new String()) {
            this.type = new String("");
        } else {
            this.type = type; 
        }
    }
    
    // Message beanish methods
    public String getMessage() { return this.message; }
    public void setMessage(String message) { 
        if (message == null || message == new String()) {
            this.message = new String("");
        } else {
            this.message = message; 
        }
    }
    
    // Attributes Beanish Methods
    public String[] getAttributeNames() {
        String[] names = new String[attributes.size()];
        Iterator iterator = attributes.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            names[i] = (String)iterator.next();
        }
        return names;
    }
    public String getAttribute(String name) { return (String)attributes.get(name); }
    public void addAttribute(String name, String value) { attributes.put(name, value); }
    public void clearAttributes() { attributes.clear(); }
    public void removeAttribute(String name) { attributes.remove(name); }
    
    // Element Generation Method
    public Element generateXmlElement(){
        // Create the root element
        Element responseMessage = document.createElement("Message");

        // Add any specified attributes
        if (type != null && type != "") { responseMessage.setAttribute("Type", type); }
        Iterator attributeIterator = attributes.keySet().iterator();
        for(int i=0;attributeIterator.hasNext();i++) {
            String name = (String)attributeIterator.next();
            responseMessage.setAttribute(name, (String)attributes.get(name));
        }

        // Add the message
        responseMessage.appendChild(document.createTextNode(message));

        // Return the element
        return responseMessage;
    }
    
    public Message(Element element) throws ModelException {
        // Throw an exception if we are trying to generate an object from a null element
        if (element == null) { 
            throw new ModelException("Unable to generate Message object from a null Element."); 
        }
        // Throw an exception if we are trying to generate an object from a non-configuration element
        else if (!element.getNodeName().equals("Message")) {
            throw new ModelException("Unable to generate Message object from a \"" + element.getNodeName() + "\" element.");
        }
        // Throw an exception if there is not the proper number of child elements
        else if (element.getChildNodes().getLength() != 1) {
            throw new ModelException("Unable to generate Message object from a malformed element (must have exactly 1 child TextElement).");
        }
        // Throw an exception if the RequestMethod attribute is missing
        else if (element.getAttribute("Type") == null) {
            throw new ModelException("Unable to generate Message object from a malformed element (attribute Type is required).");
        }
        // If the skeleton looks good
        else {
            // Check that the child is a text node
            if (element.getFirstChild() != null) {
                if (element.getFirstChild().getNodeType() != Node.TEXT_NODE) {
                    throw new ModelException("Unable to generate Message object, a Message element must have zero or one child text nodes.");
                } else {
                    // Set the value
                    this.setMessage(element.getFirstChild().getNodeValue());
                }
            }
            
            // Set the other attributes
            this.type = element.getAttribute("Type");

            // Get the other attributes
            NamedNodeMap attributes = element.getAttributes();
            for(int i=0;i<attributes.getLength();i++) {
                String name = attributes.item(i).getNodeName();
                if (!name.equals("Type")) {
                    this.addAttribute(name, attributes.item(i).getNodeValue());
                }
            }
        }
    }
}
