package com.kd.klink.model.entry;

import java.util.LinkedHashMap;
import java.util.Iterator;

import org.w3c.dom.*;

import com.kd.klink.model.*;

/**
 *
 */
public class GenericEntryItem extends ModelObject {
    private LinkedHashMap attributes = new LinkedHashMap();
    private LinkedHashMap complexItems = new LinkedHashMap();

    private String id = "";
    private String type = ""; 
    private String value = "";
    
    // Constructure
    public GenericEntryItem(String id) { this.id = id; }
    public GenericEntryItem(String id, String value) {
        this.id = id;
        this.value = value;
    }
    
    // ID Beanish Methods
    public String getID() { return id; }
    public void setID(String id) { this.id = id; }
    
    // Type Beanish Methods
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    // Type Beanish Methods
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

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
    
    // ComplexItems Beanish Methods
    public String[] getGenericComplexItemIDs() {
        String[] complexItemIDs = new String[complexItems.size()];
        Iterator iterator = complexItems.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            complexItemIDs[i] = (String)iterator.next();
        }
        return complexItemIDs;
    }
    public GenericComplexItem getGenericComplexItem(String id) { return (GenericComplexItem)complexItems.get(id); }
    public void addGenericComplexItem(GenericComplexItem complexItem) { 
        complexItems.put(complexItem.getID(), complexItem); 
        if (type == null || type == "") { type = "COMPLEX"; }
    }
    public void clearGenericComplexItems() { complexItems.clear(); }
    public void removeGenericComplexItem(String id) { complexItems.remove(id); }
    
    // Element Generation Method
    public Element generateXmlElement(){
        Element entryItem = document.createElement("EntryItem");
        
        // Add any specified attributes
        if (id != null && id != "") { entryItem.setAttribute("ID", id); }
        if (type != null && type != "") { entryItem.setAttribute("Type", type); }
        Iterator attributeIterator = attributes.keySet().iterator();
        for(int i=0;attributeIterator.hasNext();i++) {
            String name = (String)attributeIterator.next();
            entryItem.setAttribute(name, (String)attributes.get(name));
        }
        
        // Add any specified values
        if (complexItems.size() > 0) {
            Iterator iterator = complexItems.keySet().iterator();
            for(int i=0;iterator.hasNext();i++) {
                GenericComplexItem complexItem = (GenericComplexItem)complexItems.get(iterator.next());
                entryItem.appendChild(document.importNode(complexItem.generateXmlElement(), true));
            }
        } else {
            entryItem.appendChild(document.createTextNode(value));
        }
        
        return entryItem;
    }
    
    public GenericEntryItem(Element element) throws ModelException {
        // Validate the element
        if (element == null) {
            throw new ModelException("Unable to create the GenericEntryItem from a null Element.");
        } else if (element.getNodeName() != "EntryItem") {
            throw new ModelException("Unable to create the GenericEntryItem, root node \"" + element.getNodeName() + "\" is not \"EntryItem\".");
        }
        
        // Set the other attributes
        this.id = element.getAttribute("ID");
        this.type = element.getAttribute("Type");
        NamedNodeMap attributes = element.getAttributes();
        for(int i=0;i<attributes.getLength();i++) {
            String name = attributes.item(i).getNodeName();
            if (!name.equals("ID") && !name.equals("Type")) {
                this.addAttribute(name, attributes.item(i).getNodeValue());
            }
        }
        
        // If this is a simple GenericEntryItem
        if (type == null || type.equals("")) {
            if (element.getFirstChild() != null) {
                this.setValue(element.getFirstChild().getNodeValue());
            }
        }
        // If there are complex type items
        else {
            // For each of the child nodes, create a GenericItem
            NodeList genericComplexItemNodes = element.getChildNodes();
            for(int i=0;i>genericComplexItemNodes.getLength();i++) {
                if (genericComplexItemNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    throw new ModelException("Unable to create the GenericEntryItem, child nodes must be ComplexItem elements.");
                } else {
                    this.addGenericComplexItem(new GenericComplexItem((Element)genericComplexItemNodes.item(i)));
                }
            }
        }
    }
}
