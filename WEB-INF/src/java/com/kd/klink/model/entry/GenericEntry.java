package com.kd.klink.model.entry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;

import org.w3c.dom.*;

import com.kd.klink.model.*;
import com.kd.klink.model.response.*;

/**
 *
 */
public class GenericEntry extends ModelObject {
    private boolean includeNullEntryItems = false;
    
    private LinkedHashMap attributes = new LinkedHashMap();
    private LinkedHashMap entryItems = new LinkedHashMap();
    private ArrayList messages = new ArrayList();
    
    private String id = null;
    private String structure = null;
    
    // Constructure
    public GenericEntry() {}
    public GenericEntry(String id) { this.id = id; }
    public GenericEntry(String id, String structure) {
        this.id = id;
        this.structure = structure;
    }
    
    // includeNullEntryItems Beanish Methods
    public boolean includeNullEntryItems() { return this.includeNullEntryItems; }
    public void includeNullEntryItems(boolean val) { this.includeNullEntryItems = val; }
    
    // Structure Beanish Methods
    public String getStructure() { return this.structure; }
    public void setStructure(String structure) { this.structure = structure; }
    
    // id Beanish Methods
    public String getID() { return this.id; }
    public void setID(String id) { this.id = id; }
    
    // EntryItem Beanish Methods
    public String[] getGenericEntryItemIDs() {
        String[] entryItemIDs = new String[entryItems.size()];
        Iterator iterator = entryItems.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            entryItemIDs[i] = (String)iterator.next();
        }
        return entryItemIDs;
    }
    public GenericEntryItem getGenericEntryItem(String id) { return (GenericEntryItem)entryItems.get(id); }
    public void addGenericEntryItem(GenericEntryItem entryItem) { entryItems.put(entryItem.getID(), entryItem); }
    public void clearGenericEntryItems() { entryItems.clear(); }
    public void removeGenericEntryItem(String id) { entryItems.remove(id); }
    
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
    public Element generateXmlElement() {
        Element entry = document.createElement("Entry");
        
        // Add any specified attributes
        if (id != null && id != "") { entry.setAttribute("ID", id); }
        if (structure != null && structure != "") { entry.setAttribute("Structure", structure); }
        Iterator attributeIterator = attributes.keySet().iterator();
        for(int i=0;attributeIterator.hasNext();i++) {
            String name = (String)attributeIterator.next();
            entry.setAttribute(name, (String)attributes.get(name));
        }
        
        // Add any specified values
        Iterator iterator = entryItems.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            GenericEntryItem entryItem = (GenericEntryItem)entryItems.get(iterator.next());
            if (this.includeNullEntryItems || entryItem.getType() != "" || entryItem.getValue() != "") {
                entry.appendChild(document.importNode(entryItem.generateXmlElement(), true));
            }
        }
        
        return entry;
    }
    
    public GenericEntry(Element element) throws ModelException {
        // Validate the element
        if (element == null) {
            throw new ModelException("Unable to create the GenericEntry from a null Element.");
        } else if (element.getNodeName() != "Entry") {
            throw new ModelException("Unable to create the GenericEntry, root node \"" + element.getNodeName() + "\" is not \"Entry\".");
        }
        
        // For each of the child nodes, create a GenericItem
        NodeList genericItemNodes = element.getChildNodes();
        for(int i=0;i<genericItemNodes.getLength();i++) {
            if (genericItemNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                throw new ModelException("Unable to create the GenericComplexItem, child nodes must be Item elements.");
            } else {
                this.addGenericEntryItem(new GenericEntryItem((Element)genericItemNodes.item(i)));
            }
        }
        
        // Set the other attributes
        this.id = element.getAttribute("ID");
        this.structure = element.getAttribute("Structure");
        NamedNodeMap attributes = element.getAttributes();
        for(int i=0;i<attributes.getLength();i++) {
            String name = attributes.item(i).getNodeName();
            if (!name.equals("ID") && !name.equals("Structure")) {
                this.addAttribute(name, attributes.item(i).getNodeValue());
            }
        }
    }
}
