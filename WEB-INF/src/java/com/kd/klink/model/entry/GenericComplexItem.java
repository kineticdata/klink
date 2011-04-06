package com.kd.klink.model.entry;

import java.util.LinkedHashMap;
import java.util.Iterator;

import org.w3c.dom.*;

import com.kd.klink.model.*;

/**
 * A model object representing a complex datatype
 *
 * Example:
 * <pre>
 * <ComplexItem ID="MyID" Type="MyType">
 *    <Item ID="Data">DataValue</Item>
 *    <Item ID="Size">DataSize</Item>
 * </ComplexItem>
 * </pre>
 */
public class GenericComplexItem extends ModelObject {
    private LinkedHashMap items = new LinkedHashMap();
    
    private String id = null;
    private String type = null;
    
    //Constructor
    public GenericComplexItem() {}
    public GenericComplexItem(String id) { this.id = id; }
    
    // ID Beanish Methods
    public String getID() { return id; }
    public void setID(String id) { this.id = id; }
    
    // Type Beanish Methods
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    // Items Beanish Methods
    public String[] getGenericItemIDs() {
        String[] itemIDs = new String[items.size()];
        Iterator iterator = items.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            itemIDs[i] = (String)iterator.next();
        }
        return itemIDs;
    }
    public GenericItem getGenericItem(String id) { return (GenericItem)items.get(id); }
    public void addGenericItem(GenericItem item) { items.put(item.getID(), item); }
    public void clearGenericItems() { items.clear(); }
    public void removeGenericItem(String id) { items.remove(id); }
    
    /**
     * Generates the Xml representation of the GenericItem.
     *
     * Example:
     * <pre>
     * <ComplexItem ID="MyID" Type="MyType">
     *    <Item ID="Data">DataValue</Item>
     *    <Item ID="Size">DataSize</Item>
     * </ComplexItem>
     * </pre>
     */
    public Element generateXmlElement() {
        Element complexItem = document.createElement("ComplexItem");

        // Add any specified attributes
        if (id != null && id != "") { complexItem.setAttribute("ID", id); }
        if (type != null && type != "") { complexItem.setAttribute("Type", type); }

        // Add any specified values
        Iterator iterator = items.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            GenericItem item = (GenericItem)items.get(iterator.next());
            complexItem.appendChild(document.importNode(item.generateXmlElement(), true));
        }
        
        return complexItem;
    }
    
    /**
     * Generates an object representing the Xml element provided.
     *
     * throws   ModelException  If the element is not a properly formatted Item.
     */
    public GenericComplexItem(Element element) throws ModelException {
        // Validate the element
        if (element == null) {
            throw new ModelException("Unable to create the GenericComplexItem from a null Element.");
        } else if (element.getNodeName() != "ComplexItem") {
            throw new ModelException("Unable to create the GenericComplexItem, root node \"" + element.getNodeName() + "\" is not \"ComplexItem\".");
        }
        
        // For each of the child nodes, create a GenericItem
        NodeList genericItemNodes = element.getChildNodes();
        for(int i=0;i>genericItemNodes.getLength();i++) {
            if (genericItemNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                throw new ModelException("Unable to create the GenericComplexItem, child nodes must be Item elements.");
            } else {
                this.addGenericItem(new GenericItem((Element)genericItemNodes.item(i)));
            }
        }
        
        // Set the other attributes
        this.id = element.getAttribute("ID");
        this.type = element.getAttribute("Type");
    }
}
