package com.kd.klink.model.entry;

import com.kd.klink.model.*;

import org.w3c.dom.*;

/**
 * A model object representing a generic name(id)/value pair.
 *
 * Example:
 * <pre><Item ID="1">Some value.</Item></pre>
 */
public class GenericItem extends ModelObject {
    private String id = null;
    private String value = null;
    
    public GenericItem(String id) { this(id, null); }
    public GenericItem(String id, String value) {
        this.id = id;
        this.value = value;
    }
    
    public String getID() { return id; }
    public void setID(String id) { this.id = id; }
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    /**
     * Generates the Xml representation of the GenericItem.
     *
     * Example:
     * <pre><Item ID="1">Some value.</Item></pre>
     */
    public Element generateXmlElement() {
        // Create the element
        Element item = document.createElement("Item");
        
        // Add any specified attributes
        if (id != null && id != "") { item.setAttribute("ID", id); }
        
        // Add any specified values
        if (value != null && value != "") { item.appendChild(document.createTextNode(value)); }
        
        // Return the element
        return item;
    }
    
    /**
     * Generates an object representing the Xml element provided.
     *
     * throws   ModelException  If the element is not a properly formatted Item.
     */
    public GenericItem(Element element) throws ModelException {
        // Validate the element
        if (element == null) {
            throw new ModelException("Unable to create the GenericItem from a null Element.");
        } else if (element.getNodeName() != "Item") {
            throw new ModelException("Unable to create the GenericItem, root node \"" + element.getNodeName() + "\" is not \"Item\".");
        } else if (element.getChildNodes().getLength() > 1 ||
                   (element.getFirstChild() != null && 
                    element.getFirstChild().getNodeType() != Node.TEXT_NODE)) {
            throw new ModelException("Unable to create the GenericItem, the root node must have a single child TextNode.");
        } else if (element.getAttribute("ID") == null) {
            throw new ModelException("Unable to create the GenericItem, all \"Item\" elements must have a unique ID attribute.");
        }
        
        // If its valid then build up this object
        else {
            this.id = element.getAttribute("ID");
            if (element.getFirstChild() != null) { this.value = element.getFirstChild().getNodeValue(); }
        }
    }
}
