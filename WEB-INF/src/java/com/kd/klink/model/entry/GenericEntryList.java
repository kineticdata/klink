package com.kd.klink.model.entry;

import java.util.LinkedHashMap;
import java.util.Iterator;

import org.w3c.dom.*;

import com.kd.klink.model.*;

/**
 *
 */
public class GenericEntryList extends ModelObject {
    // For Remedy: Limit, Matches, Structure, Query, Sorting, 
    private LinkedHashMap attributes = new LinkedHashMap();
    private LinkedHashMap entries = new LinkedHashMap();

    private String structure = null;
    private String count = null;
    
    // Constructure
    public GenericEntryList() {}
    
    // Structure Beanish Methods
    public String getStructure() { return this.structure; }
    public void setStructure(String structure) { this.structure = structure; }
    
    // Count Beanish Methods
    public String getCount() { return this.count; }
    public void setCount(String count) { this.count = count; }
    
    // Entries Beanish Methods
    public String[] getEntryIDs() {
        String[] ids = new String[entries.size()];
        Iterator iterator = entries.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            ids[i] = (String)iterator.next();
        }
        return ids;
    }
    public GenericEntry getGenericEntry(String id) { return (GenericEntry)entries.get(id); }
    public void addGenericEntry(GenericEntry entry) { entries.put(entry.getID(), entry); }
    public void clearGenericEntries() { entries.clear(); }
    public void removeGenericEntry(String id) { entries.remove(id); }
    
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
        Element entryList = document.createElement("EntryList");
        
        // Add any specified attributes
        if (count != null && count != "") { entryList.setAttribute("Count", count); }
        if (structure != null && structure != "") { entryList.setAttribute("Structure", structure); }
        Iterator attributeIterator = attributes.keySet().iterator();
        for(int i=0;attributeIterator.hasNext();i++) {
            String name = (String)attributeIterator.next();
            entryList.setAttribute(name, (String)attributes.get(name));
        }
        
        // Add any specified values
        Iterator iterator = entries.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            GenericEntry entry = (GenericEntry)entries.get(iterator.next());
            entryList.appendChild(document.importNode(entry.generateXmlElement(), true));
        }
        
        return entryList;
    }
    
    public GenericEntryList(Element element) throws ModelException {
        // Validate the element
        if (element == null) {
            throw new ModelException("Unable to create the GenericEntryList from a null Element.");
        } else if (element.getNodeName() != "EntryList") {
            throw new ModelException("Unable to create the GenericEntryList, root node \"" + element.getNodeName() + "\" is not \"EntryList\".");
        }
        
        // Set the other attributes
        this.count = element.getAttribute("Count");
        this.structure = element.getAttribute("Structure");
        NamedNodeMap attributes = element.getAttributes();
        for(int i=0;i<attributes.getLength();i++) {
            String name = attributes.item(i).getNodeName();
            if (!name.equals("Count") && !name.equals("Structure")) {
                this.addAttribute(name, attributes.item(i).getNodeValue());
            }
        }
        
        // For each of the child nodes, create a GenericEntry
        NodeList genericEntryNodes = element.getChildNodes();
        for(int i=0;i<genericEntryNodes.getLength();i++) {
            if (genericEntryNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                throw new ModelException("Unable to create the GenericEntryItem, child nodes must be Entry elements.");
            } else {
                this.addGenericEntry(new GenericEntry((Element)genericEntryNodes.item(i)));
            }
        }
    }
}
