package com.kd.klink.model.response;

import java.io.StringWriter;

import java.util.Vector;
import java.util.Iterator;

import org.w3c.dom.*;

import com.kd.klink.model.*;

/**
 *
 */
public class Response extends ModelObject {
    // Variables
    private Boolean success = Boolean.FALSE;
    private String requestMethod = new String("");
    private Vector messages = new Vector();
    private Vector resultElements = new Vector();
    
    // Constructors
    public Response() {
        this((String)null, (Boolean)null, (Message[])null, (Element)null);
    }
    public Response(String requestMethod) {
        this(requestMethod, (Boolean)null, (Message[])null, (Element)null);
    }
    public Response(Boolean success) { 
        this((String)null, success, (Message[])null, (Element)null);
    }
    public Response(String requestMethod, Boolean success) {
        this(requestMethod, success, (Message[])null, (Element)null);
    }
    public Response(String requestMethod, Boolean success, Element result) {
        this(requestMethod, success, (Message[])null, result);
    }

    public Response(String requestMethod, Boolean success, Message[] messages, Element result) {
        setRequestMethod(requestMethod);
        setSuccess(success);
        setMessages(messages);
        addResult(result);
    }
    
    // RequestMethod beanish Methods
    public String getRequestMethod() { return this.requestMethod; }
    public void setRequestMethod(String requestMethod) { 
        if (requestMethod == null || requestMethod == new String()) {
            this.requestMethod = new String("");
        } else {
            this.requestMethod = requestMethod; 
        }
    }
    
    // Success beanish Methods
    public Boolean getSuccess() { return this.success; }
    public void setSuccess(Boolean success) { 
        if (success == null) {
            this.success = Boolean.FALSE;
        } else {
            this.success = success;
        }
    }
    
    // Messages beanish Methods
    public void addMessage(Message message) { messages.add(message); }
    public void addMessages(Message[] messages) {
        for (int i=0;i<messages.length;i++) {
            this.addMessage(messages[i]);
        }
    }
    public Message[] getMessages() {
        Message[] messages = new Message[this.messages.size()];
        for (int i=0;i<messages.length;i++) {
            messages[i] = (Message)this.messages.get(i);
        }
        return messages;
    }
    public void setMessages(Message[] messages) { 
        this.messages.clear();
        if (messages != null) {
            for (int i=0;i<messages.length;i++) {
                this.messages.add(messages[i]);
            }
        }
    }
    
    // Result beanish Methods
    public void addResult(Element result) {
        if (result != null) {
            this.resultElements.add(result);
        }
    }
    public void setResults(Element[] results) {
        this.resultElements.clear();
        for (int i=0;i<results.length;i++) {
            this.resultElements.add(results[i]);
        }
    }
    public Element[] getResults() { 
        Element[] results = new Element[resultElements.size()];
        for (int i=0;i<results.length;i++) {
            results[i] = (Element)resultElements.get(i);
        }
        return results; 
    }
    
    // Element Generation Method
    public Element generateXmlElement() {
        // Generate the root and child elements
        Element responseElement = document.createElement("Response");
        Element messagesElement = document.createElement("Messages");
        Element resultElement = document.createElement("Result");

        // Add any specified attributes
        if (requestMethod != null) { responseElement.setAttribute("RequestMethod", requestMethod); }
        if (success != null) { responseElement.setAttribute("Success", success.toString()); }

        // Add the messages
        for(int i=0;i<messages.size();i++) {
            Message message = (Message)messages.get(i);
            messagesElement.appendChild(document.importNode(message.generateXmlElement(), true));
        }

        // Add the result
        for (int i=0;i<resultElements.size();i++) {
            Element curResult = (Element)resultElements.get(i);
            resultElement.appendChild(document.importNode(curResult, true));
        }

        // Add the messages and result elements to our response element
        responseElement.appendChild(messagesElement);
        responseElement.appendChild(resultElement);

        // Return the element
        return responseElement;
    }
    
    public Response(Element element) throws ModelException {
        // Throw an exception if we are trying to generate an object from a null element
        if (element == null) { 
            throw new ModelException("Unable to generate Response object from a null Element."); 
        }
        // Throw an exception if we are trying to generate an object from a non-configuration element
        else if (!element.getNodeName().equals("Response")) {
            throw new ModelException("Unable to generate Response object from a \"" + element.getNodeName() + "\" element.");
        }
        // Throw an exception if there is not the proper number of child elements
        else if (element.getChildNodes().getLength() != 2) {
            throw new ModelException("Unable to generate Response object from a malformed element (must have exactly 2 children).");
        }
        // Throw an exception if the RequestMethod attribute is missing
        else if (element.getAttribute("RequestMethod") == null) {
            throw new ModelException("Unable to generate Response object from a malformed element (attribute RequestMethod is required).");
        }
        // Throw an exception if the Success attribute is missing
        else if (element.getAttribute("Success") == null) {
            throw new ModelException("Unable to generate Response object from a malformed element (attribute Success is required).");
        }
        // If the skeleton looks good
        else {
            // Check the messages node
            Node messagesNode = element.getFirstChild();
            if (messagesNode.getNodeType() != Node.ELEMENT_NODE || !messagesNode.getNodeName().equals("Messages")) {
                throw new ModelException("Unable to generate Response object, the first child of the Response element must be a Messages element.");
            } else {
                // Get a list of messages
                NodeList messageNodes = messagesNode.getChildNodes();
                
                // Add each message node to our list of messages
                for (int i=0;i<messageNodes.getLength();i++) {
                    this.addMessage(new Message((Element)messageNodes.item(i)));
                }
            }

            // Check the results node
            Node resultNode = element.getLastChild();
            if (resultNode.getNodeType() != Node.ELEMENT_NODE || !resultNode.getNodeName().equals("Result")) {
                throw new ModelException("Unable to generate Response object, the second child of the Response element must be a Result element.");
            } else {
                // Get a list of result elements
                NodeList resultElements = resultNode.getChildNodes();
                        
                // Add each of the result elements
                for (int i=0;i<resultElements.getLength();i++) {
                    // Check that its actually an element
                    if (resultElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
                        throw new ModelException("Unable to generate Response object, the Result child node must contain zero or more element nodes.");
                    } else {
                        this.addResult((Element)resultElements.item(i));
                    }
                }
            }
            
            // Set the attributes
            this.setRequestMethod(element.getAttribute("RequestMethod"));
            this.setSuccess(Boolean.valueOf(element.getAttribute("Success")));
        }
    }
}
