package com.kd.klink.ars.model;

import java.util.LinkedHashMap;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.model.*;
import com.kd.klink.model.entry.*;
import com.kd.klink.model.response.*;

import com.kd.klink.ars.util.ArsUtil;

/**
 *
 */
public class ArsEntry extends ArsObject {
    private static final Logger logger = Logger.getLogger(ArsEntry.class.getName());
    private GenericEntry genericEntry;
    private boolean includeNullElements = false;
    
    public GenericEntry getGenericEntry() { return this.genericEntry; }
    
    public ArsEntry(GenericEntry genericEntry) { this.genericEntry = genericEntry; }
    
    public ArsEntry(ArsContext context, String structure, String entryID) throws ARException {
        this(context, structure, entryID, null);
    }
    
    public ArsEntry(ArsContext context, String structure, String entryID, long[] items) throws ARException {
        logger.debug("Constructing new ArsEntry object...");
        
        logger.debug("Retrieving the Remedy Entry object...");
        EntryKey entryKey = new EntryKey(new NameID(structure), new EntryID(entryID));
        EntryCriteria criteria = null;
        if (items != null) {
            EntryListFieldInfo[] fldInfos = new EntryListFieldInfo[items.length];
            for (int i=0;i<items.length;i++) {
                fldInfos[i] = new EntryListFieldInfo(new FieldID(items[i]));
            }
            criteria = new EntryCriteria(fldInfos);
        }
        Entry remedyEntry = EntryFactory.findByKey(context.getContext(), entryKey, criteria);
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        logger.debug("Generating internal GenericEntry...");
        if (items == null) {
            genericEntry = ArsEntry.generateGenericEntry(context, remedyEntry, false);
        } else if (items.length == 0) {
            genericEntry = ArsEntry.generateGenericEntry(context, remedyEntry, true);
        } else {
            genericEntry = ArsEntry.generateGenericEntry(context, remedyEntry, items);
        }
        
        logger.debug("ArsEntry constructed.");
    }
    
    public ArsEntry(String genericRemedyEntryXml) throws ARException, ModelException {
        logger.debug("Constructing new ArsEntry object...");
        
        logger.debug("Generating the Generic Entry object.");
        genericEntry = ArsEntry.generateGenericEntry(genericRemedyEntryXml);
        
        logger.debug("ArsEntry constructed.");
    }
    
    public static ArsEntry create(ArsContext context, String structureID, String genericRemedyEntryXml) throws ARException, ModelException {
        logger.debug("Creating new remedy entry for structure: " + structureID);
        
        // Generate the remedy Entry object
        Entry remedyEntry = ArsEntry.generateArsEntry(context, genericRemedyEntryXml, structureID);
        
        // Throw an exception if a specific ID is requested
        if (remedyEntry.getEntryID() != null) {
            throw new ModelException("Unable to create an Entry with a pre-existing ID.");
        }
        
        // Set the entry context
        remedyEntry.setContext(context.getContext());
        
        // Create the entry
        remedyEntry.create();
        StatusInfo[] infos = context.getContext().getLastStatus();
        
        // Generate the genericEntry
        GenericEntry genericEntry = new GenericEntry(remedyEntry.getEntryID().toString(), remedyEntry.getSchemaID().toString());
        
        // Create the ArsEntry
        ArsEntry entry = new ArsEntry(genericEntry);
        
        // Add the Context Messages
        entry.addMessages(infos);
        entry.addMessages(context.getContext().getLastStatus());
        
        // Return a new RemedyEntry object
        return entry;
    }
    
    public static ArsEntryList update(ArsContext context, String structureID, String qualification, String genericRemedyEntryXml) throws ARException, ModelException {
        logger.debug("Updating remedy entry where: " + qualification);
        
        // Set the default object returned
        ArsEntryList entries = new ArsEntryList();
        
        // Generate the remedy Entry object
        Entry remedyEntry = ArsEntry.generateArsEntry(context, genericRemedyEntryXml, structureID);
        // Set the entry context
        remedyEntry.setContext(context.getContext());
        
        // If there is not any information on what entry/entries to update
        if (qualification != null && remedyEntry.getEntryID() != null) {
            throw new ModelException("Unable to update.  The entry id can't be set inside the entry when an explicit entry id or qualification parameter is used.");
        }
        // If the user is trying to update an explicit entry and all entries matching the qualification
        else if (qualification == null && remedyEntry.getEntryID() == null) {
            throw new ModelException("Unable to update.  Please include an entry ID with the request url or in the entry XML, or include the 'qualification' parameter.");
        }
        // If the user properly specified an update target
        else {
            // Initialize the list of entries to update
            EntryKey[] entryKeys = new EntryKey[0];
                    
            // If the user specified a specific entry to update.
            if (qualification == null) { 
                logger.debug("Updating single entry.");
                entryKeys = new EntryKey[1];
                entryKeys[0] = remedyEntry.getKey();
            }
            // If the user specified a qualification to update
            else {
                // Build up the FieldList
                logger.debug("Updating based on qualification.");
                logger.debug("Generating fieldList...");
                FieldListCriteria fieldListCriteria = new FieldListCriteria(new NameID(structureID), new Timestamp(0), FieldType.AR_DATA_FIELD);
                FieldCriteria fieldCriteria = new FieldCriteria();
                fieldCriteria.setRetrieveAll(true);
                Field[] fieldList = FieldFactory.findObjects(context.getContext(), fieldListCriteria, fieldCriteria);

                // Build up the qualifier
                logger.debug("Generating qualifierInfo...");
                QualifierInfo qualifierInfo = Util.ARGetQualifier(context.getContext(), qualification, fieldList, null, Constants.AR_QUALCONTEXT_DEFAULT);

                // Build the EntryListCriteria
                logger.debug("Generating listCriteria...");
                EntryListCriteria listCriteria = new EntryListCriteria(new NameID(structureID), qualifierInfo, 0, new Timestamp(0), new SortInfo[0], (EntryID[])null);

                // Retrieve the keys
                entryKeys = EntryFactory.find(context.getContext(), listCriteria, false, (Integer)null);
                
                // If there was nothing matching
                if (entryKeys == null) { entryKeys = new EntryKey[0]; }
            }
            
            // Update the matching items
            int successfulUpdates = 0;
            int failedUpdates = 0;
            for (int i=0;i<entryKeys.length;i++) {
                 // Create the default GenericEntry
                GenericEntry genericEntry = new GenericEntry(entryKeys[i].getEntryID().toString(), entryKeys[i].getSchemaID().toString());

                try {
                    // Set the ID
                    remedyEntry.setKey(entryKeys[i]);

                    // Update the entry
                    remedyEntry.store();
                    StatusInfo[] infos = context.getContext().getLastStatus();

                    Message[] messages = ArsObject.generateMessages(infos);
                    for (int j=0;j<messages.length;j++) {
                        messages[j].addAttribute("EntryID", genericEntry.getID());
                        entries.addMessage(messages[j]);
                    }

                    genericEntry.addAttribute("Success", "true");
                    entries.addGenericEntry(genericEntry);
                } catch (ARException e) {
                    StatusInfo[] infos = e.getLastStatus();
                    Message[] messages = ArsObject.generateMessages(infos);
                    for (int j=0;j<messages.length;j++) {
                        messages[j].addAttribute("EntryID", genericEntry.getID());
                        entries.addMessage(messages[j]);
                    }
                    
                    genericEntry.addAttribute("Success", "false");
                    entries.addGenericEntry(genericEntry);
                }
            }
        }

        // Return a new RemedyEntry object
        return entries;
    }
    
    public static ArsEntry delete(ArsContext context, String structure, String entryID) throws ARException {
        logger.debug("Deleting remedy entry: " + entryID);
        
        // Retrieve the remedy Entry
        EntryKey entryKey = new EntryKey(new NameID(structure), new EntryID(entryID));
        Entry remedyEntry = EntryFactory.findByKey(context.getContext(), entryKey, null);
        
        // Delete the Entry
        remedyEntry.remove();
        
        // Create the Entry
        ArsEntry entry = new ArsEntry(new GenericEntry(remedyEntry.getEntryID().toString(), remedyEntry.getSchemaID().toString()));
        
        // Add the Context Messages
        entry.addMessages(context.getContext().getLastStatus());
        
        // Return a childless GenericEntry with associated entryId and structureID
        return entry;
    }
    
    public static GenericEntry generateGenericEntry(String entryString) throws ModelException {
        // Build the base GenericEntry
        GenericEntry returnEntry = new GenericEntry();
        
        try {
            // Create a new Xml Document object
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(entryString)));
            
            // If the document is not null build up returnEntry
            Node firstChild = doc.getFirstChild();
            if (firstChild != null) {
                // Check for the structure information
                NamedNodeMap entryAttributes = firstChild.getAttributes();
                Node structureAttribute = entryAttributes.getNamedItem("Structure");
                if (structureAttribute != null) {
                    returnEntry.setStructure(structureAttribute.getNodeValue());
                } else {
                    throw new ModelException("Unable to process an Entry missing the Structure attribute.");
                }
                
                // Check for the Entry ID information
                Node entryIDAttribute = entryAttributes.getNamedItem("ID");
                if (entryIDAttribute != null) {
                    returnEntry.setID(entryIDAttribute.getNodeValue());
                }

                // Get a list of entry Items
                NodeList items = doc.getElementsByTagName("EntryItem");
                EntryItem[] entryItems = new EntryItem[items.getLength()];

                // Parse through the nodes
                for(int i=0;i<items.getLength();i++) {
                    // Grab the current node information
                    Node curNode = items.item(i);
                    NamedNodeMap curAttributes = curNode.getAttributes();

                    // Grab The attributes
                    Node curTypeAttribute = curAttributes.getNamedItem("Type");
                    Node curIDAttribute = curAttributes.getNamedItem("ID");
                    
                    // If there is not a type then this is a simple entry item
                    if (curIDAttribute == null) {
                        throw new ModelException("Unable to process and EntryItem missing the ID attribute.");
                    } if (curTypeAttribute == null) {
                        String id = curIDAttribute.getNodeValue();
                        if (curNode.getFirstChild() == null) {
                            returnEntry.addGenericEntryItem(new GenericEntryItem(id, ""));
                        } else {
                            returnEntry.addGenericEntryItem(new GenericEntryItem(id, curNode.getFirstChild().getNodeValue()));
                        }
                    }
                    // If there is a type then this is a complex entry item
                    else {
                        // Get the current type and ID
                        String id = curIDAttribute.getNodeValue();
                        String entryItemType = curTypeAttribute.getNodeValue();
                        
                        // Create the ComplexEntryItem
                        GenericEntryItem genericEntryItem = new GenericEntryItem(id);
                        
                        // Get the ComplexItems
                        NodeList complexItems = curNode.getChildNodes();
                        
                        // If the current Node is an ATTACHMENT
                        if (entryItemType == "ATTACHMENT") {
                            // Set the entryItemType
                            genericEntryItem.setType("ATTACHMENT");
                            
                            // Check for the correct number of ComplexItems
                            if (complexItems.getLength() != 1) { throw new ModelException("An ATTACHMENT EntryItem must have exactly 1 ComplexItem child."); }
                            else {
                                // Get a list of ATTACHMENT items
                                Node complexItemNode = complexItems.item(0);
                                NodeList attachmentItems = complexItemNode.getChildNodes();
                                
                                // Get a node for each generic Item
                                if (attachmentItems.getLength() != 3) { throw new ModelException("An ATTACHMENT EntryItem must have a ComplexItem containing exactly three Items (with attributes \"Data\", \"Name\", and \"Size\")."); }
                                else {
                                    Node dataItem = null, nameItem = null, sizeItem = null;
                                    // Traverse the list of items for the three nodes we want
                                    for(int j=0;j<attachmentItems.getLength();j++) {
                                        // Get the current ID attribute
                                        Node curItemNode = attachmentItems.item(j);
                                        NamedNodeMap curItemAttributes = curItemNode.getAttributes();
                                        
                                        // If there are any attributes, set the data/name/sizeItem nodes
                                        if (curItemAttributes != null) {
                                            // Check to see if there is an ID attribute and what it is
                                            Node itemIDAttribute = curItemAttributes.getNamedItem("ID");
                                            if (itemIDAttribute != null) {
                                                // Get the value of the ID attribute
                                                String itemID = itemIDAttribute.getNodeValue();
                                                
                                                // If its one of the nodes we care about, set the value
                                                if (itemID != null && itemID == "Data") { dataItem = curItemNode; }
                                                else if (itemID != null && itemID == "Name") { nameItem = curItemNode; }
                                                else if (itemID != null && itemID == "Size") { sizeItem = curItemNode; }
                                            }
                                        }
                                        
                                        // If we have all the required Items, build up the generic ComplexItem
                                        if (dataItem != null && nameItem != null && sizeItem != null) {
                                            // Get the values
                                            String data = new String();
                                            String name = new String();
                                            String size = new String();
                                            
                                            if (dataItem.getFirstChild() != null) { data = dataItem.getFirstChild().getNodeValue(); }
                                            if (nameItem.getFirstChild() != null) { name = nameItem.getFirstChild().getNodeValue(); }
                                            if (sizeItem.getFirstChild() != null) { size = sizeItem.getFirstChild().getNodeValue(); }
                                            
                                            // Generate the complex Item
                                            GenericComplexItem complexItem = new GenericComplexItem();
                                            complexItem.addGenericItem(new GenericItem("Data", data));
                                            complexItem.addGenericItem(new GenericItem("Name", name));
                                            complexItem.addGenericItem(new GenericItem("Size", size));
                                            
                                            // Add the complex Item
                                            genericEntryItem.addGenericComplexItem(complexItem);
                                        }
                                        // If we are missing one of the required elements
                                        else {
                                            throw new ModelException("An ATTACHMENT EntryItem must have a ComplexItem containing exactly three Items (with ID attributes \"Data\", \"Name\", and \"Size\").");
                                        }
                                    }
                                }
                            }
                        }
                        // If the current Node is a DIARY
                        else if (entryItemType == "DIARY") {
                            // Set the entryItemType
                            genericEntryItem.setType("DIARY");
                            
                            // For each DIARY_ENTRY complex item, build up and add the ComplexItem
                            for(int j=0;j<complexItems.getLength();j++) {
                                // Create the complexItem
                                GenericComplexItem complexItem = new GenericComplexItem();
                                
                                // Get the current ComplexItem
                                Node diaryEntryNode = complexItems.item(j);

                                // Get a list of DiaryEntry items
                                NodeList diaryEntryItems = diaryEntryNode.getChildNodes();
                                
                                // Get a node for each generic Item
                                if (diaryEntryItems.getLength() != 3) { throw new ModelException("A DIARY_ENTRY ComplexItem must have zero or more Items (with ID attributes of \"DiaryInfo\", \"Timestamp\", and \"User\")."); }
                                else {                                
                                    // Traverse the list of items for the three nodes we want
                                    Node diaryInfoItem = null, timestampItem = null, userItem = null;
                                    for(int k=0;k<diaryEntryItems.getLength();k++) {
                                        // Get the current ID attribute
                                        Node curItemNode = diaryEntryItems.item(k);
                                        NamedNodeMap curItemAttributes = curItemNode.getAttributes();

                                        // If there are any attributes, set the diaryInfo/timestamp/user nodes
                                        if (curItemAttributes != null) {
                                            // Check to see if there is an ID attribute and what it is
                                            Node itemIDAttribute = curItemAttributes.getNamedItem("ID");
                                            if (itemIDAttribute != null) {
                                                // Get the value of the ID attribute
                                                String itemID = itemIDAttribute.getNodeValue();

                                                // If its one of the nodes we care about, set the value
                                                if (itemID != null && itemID == "DiaryInfo") { diaryInfoItem = curItemNode; }
                                                else if (itemID != null && itemID == "Timestamp") { timestampItem = curItemNode; }
                                                else if (itemID != null && itemID == "User") { userItem = curItemNode; }
                                            }
                                        }
                                    }

                                    // If we have all the required Items, build up the generic ComplexItem
                                    if (diaryInfoItem != null && timestampItem != null && userItem != null) {
                                        // Obtain the values we want
                                        String diaryInfo = new String();
                                        String timestamp = new String();
                                        String user = new String();
                                        
                                        if (diaryInfoItem.getFirstChild() != null) { diaryInfo = diaryInfoItem.getFirstChild().getNodeValue(); } 
                                        if (timestampItem.getFirstChild() != null) { timestamp = timestampItem.getFirstChild().getNodeValue(); }
                                        if (userItem.getFirstChild() != null) { user = userItem.getFirstChild().getNodeValue(); }

                                        // Generate the Complex Item
                                        complexItem.setID(String.valueOf(j));
                                        complexItem.setType("DIARY_ENTRY");
                                        complexItem.addGenericItem(new GenericItem("DiaryInfo", diaryInfo));
                                        complexItem.addGenericItem(new GenericItem("Timestamp", timestamp));
                                        complexItem.addGenericItem(new GenericItem("User", user));

                                        // Add the complexItem to the entryItem
                                        genericEntryItem.addGenericComplexItem(complexItem);
                                    }
                                    // If we are missing one of the required elements
                                    else {
                                        throw new ModelException("A DIARY EntryItem must have zero or more ComplexItems containing exactly three Items (with ID attributes of \"DiaryInfo\", \"Timestamp\", and \"User\").");
                                    }
                                }
                            }
                        }
                        // If the current Node is a STATUS_HISTORY 
                        else if (entryItemType == "STATUS_HISTORY") {
                            // Set the entryItemType
                            genericEntryItem.setType("STATUS_HISTORY");
                            
                            // For each STATUS_HISTORY complex item, build up and add the ComplexItem
                            for(int j=0;j<complexItems.getLength();j++) {
                                // Create the complexItem
                                GenericComplexItem complexItem = new GenericComplexItem();
                                
                                // Get the current ComplexItem
                                Node statusHistoryEntryNode = complexItems.item(j);
                                
                                // Get the ID attribute
                                NamedNodeMap statusHistoryEntryAttributes = statusHistoryEntryNode.getAttributes();
                                if (statusHistoryEntryAttributes != null || statusHistoryEntryAttributes.getNamedItem("ID") == null) {
                                    throw new ModelException("A STATUS_HISTORY_ENTRY ComplexItem must have an ID.");
                                } else {
                                    complexItem.setID(statusHistoryEntryAttributes.getNamedItem("ID").getNodeValue());
                                }

                                // Get a list of DiaryEntry items
                                NodeList statusHistoryEntryItems = statusHistoryEntryNode.getChildNodes();
                                
                                if (statusHistoryEntryItems.getLength() != 2) { throw new ModelException("A STATUS_HISTORY_ENTRY ComplexItem must have exactly two child Items (with ID attributes of \"Timestamp\" and \"User\")."); }
                                else {
                                    // Traverse the list of items for the three nodes we want
                                    Node timestampItem = null, userItem = null;
                                    for(int k=0;k<statusHistoryEntryItems.getLength();k++) {
                                        // Get the current ID attribute
                                        Node curItemNode = statusHistoryEntryItems.item(k);
                                        NamedNodeMap curItemAttributes = curItemNode.getAttributes();

                                        // If there are any attributes, set the diaryInfo/timestamp/user nodes
                                        if (curItemAttributes != null) {
                                            // Check to see if there is an ID attribute and what it is
                                            Node itemIDAttribute = curItemAttributes.getNamedItem("ID");
                                            if (itemIDAttribute != null) {
                                                // Get the value of the ID attribute
                                                String itemID = itemIDAttribute.getNodeValue();

                                                // If its one of the nodes we care about, set the value
                                                if (itemID != null && itemID == "Timestamp") { timestampItem = curItemNode; }
                                                else if (itemID != null && itemID == "User") { userItem = curItemNode; }
                                            }
                                        }
                                    }

                                    // If we have all the required Items, build up the generic ComplexItem
                                    if (timestampItem != null && userItem != null) {
                                        // Obtain the values we want
                                        String timestamp = new String();
                                        String user = new String();
                                        
                                        if (timestampItem.getFirstChild() != null) { timestamp = timestampItem.getFirstChild().getNodeValue(); }
                                        if (userItem.getFirstChild() != null) { user = userItem.getFirstChild().getNodeValue(); }


                                        // Generate the Complex Item
                                        complexItem.setType("STATUS_HISTORY_ENTRY");
                                        complexItem.addGenericItem(new GenericItem("Timestamp", timestamp));
                                        complexItem.addGenericItem(new GenericItem("User", user));

                                        // Add the complexItem to the entryItem
                                        genericEntryItem.addGenericComplexItem(complexItem);
                                    }
                                    // If we are missing one of the required elements
                                    else {
                                        throw new ModelException("A DIARY EntryItem must have zero or more ComplexItems containing exactly three Items (with ID attributes of \"DiaryInfo\", \"Timestamp\", and \"User\").");
                                    }
                                }
                            }
                        }
                        // If the current Node is something else
                        else {
                            throw new ModelException("Unable to generate EntryItem, the type \"" + entryItemType + "\" is unrecognized.");
                        }
                        
                        // Add the generic EntryItem
                        returnEntry.addGenericEntryItem(genericEntryItem);
                    }
                }
            }
        } catch (Exception e) {
            throw new ModelException(e.toString());
        }
        
        return returnEntry;
    }

    public static GenericEntry generateGenericEntry(ArsContext context, Entry remedyEntry, long[] items) throws ARException {
        return generateGenericEntry(context, remedyEntry, items, true);
    }
    
    public static GenericEntry generateGenericEntry(ArsContext context, Entry remedyEntry, boolean includeNullEntryItems) throws ARException {
        return generateGenericEntry(context, remedyEntry, null, includeNullEntryItems);
    }
    
    public static GenericEntry generateGenericEntry(ArsContext context, Entry remedyEntry, long[] items, boolean includeNullEntryItems) throws ARException {
        // Build the base object
        String entryID = remedyEntry.getEntryID().toString();
        GenericEntry genericEntry = new GenericEntry(entryID);
        genericEntry.setStructure(remedyEntry.getSchemaID().toString());
        logger.debug("Generating a GenericEntry object for entry: " + entryID);
        
        // Build up the FieldList
        logger.debug("Generating fieldList.");
        FieldListCriteria fieldListCriteria = new FieldListCriteria(remedyEntry.getSchemaID(), new Timestamp(0), FieldType.AR_ALL_FIELD);
        if (items != null) {
            FieldID[] fieldIDs = new FieldID[items.length];
            for (int i=0;i<fieldIDs.length;i++) {
                fieldIDs[i] = new FieldID(items[i]);
            }
            fieldListCriteria = new FieldListCriteria(remedyEntry.getSchemaID(), fieldIDs);
        }
        FieldCriteria fieldCriteria = new FieldCriteria();
        fieldCriteria.setRetrieveAll(true);
        Field[] fieldList = FieldFactory.findObjects(context.getContext(), fieldListCriteria, fieldCriteria);
        
        // Build up the FieldID->fieldType map
        logger.debug("Generating field map.");
        LinkedHashMap fieldTypes = new LinkedHashMap();
        for (int i=0;i<fieldList.length;i++) {
            fieldTypes.put(fieldList[i].getFieldID(), fieldList[i]);
        }
        
        // Obtain the EntryItem list
        logger.debug("Building Remedy entry item list.");
        EntryItem[] remedyEntryItems = remedyEntry.getEntryItems();
        
        // Add a GenericEntryItem to our GenericEntry for each EntryItem int the Entry
        Field currentField;
        GenericEntryItem curGenericEntryItem;
        String entryItemID, entryItemValue, fieldType, dataType;
        for(int i=0;i<remedyEntryItems.length;i++) {
            // Get the EntryItemID
            entryItemID = remedyEntryItems[i].getFieldID().toString();
            logger.debug("Creating a new GenericEntryItem.  EntryItemID: " + entryItemID);
            
            // Get the Field Information
            currentField = (Field)fieldTypes.get(remedyEntryItems[i].getFieldID());
            fieldType = ArsUtil.decodeFieldType(currentField.getFieldType());
            logger.debug("FieldType: " + fieldType);
            
            // Create the Base EntryItem Object
            curGenericEntryItem = new GenericEntryItem(entryItemID);
            if (entryItemID.equals("15")) {
                // Set the current EntryType
                curGenericEntryItem.setType("STATUS_HISTORY");
                logger.debug("DataType: STATUS_HISTORY");
                
                // Obtain Status History Information
                StatusHistory history = new StatusHistory(remedyEntryItems[i].getValue().toString());
                logger.debug("Status history field obtained.");
                
                // Generate an array of status values
                Field statusField = (Field)fieldTypes.get(new FieldID(7));
                EnumLimitInfo info = (EnumLimitInfo)statusField.getFieldLimit();
                NameID[] names = ArsMultiversionManager.getNameIDs(info);
                
                // If there are status history entries
                if (history != null) {
                    // Obtain the history items
                    logger.debug("Docoding status history.");
                    StatusHistoryInfo[] historyInfo = history.decode(context.getContext());
                    logger.debug("Status history decoded.");
                    
                    // For each history item
                    for (int j=0;j<historyInfo.length;j++) {
                        // Construct the base StatusHistory object
                        GenericComplexItem complexItem = new GenericComplexItem(names[j].toString());
                        complexItem.setType("STATUS_HISTORY_ENTRY");
                        
                        // Break out the current values
                        String user = historyInfo[j].getUser().toString();
                        String timestamp = ArsUtil.convertArsTimeToIso8601Format(historyInfo[j].getTimestamp().toString());
                        
                        // Add the values to the current status history entry item
                        complexItem.addGenericItem(new GenericItem("Timestamp", timestamp));
                        complexItem.addGenericItem(new GenericItem("User", user));
                        
                        // Add the DiaryEntry object to the Diary Object
                        logger.debug("Adding STATUS_HISTORY_ENTRY GenericComplexItem(" + user + ", " + timestamp + ")");
                        curGenericEntryItem.addGenericComplexItem(complexItem);
                    }
                    genericEntry.addGenericEntryItem(curGenericEntryItem);
                }
            } else if (fieldType == "DATA") {
                dataType = ArsUtil.decodeDataType(currentField.getDataType().toInt());
                logger.debug("DataType: " + dataType);
                if (dataType == "DIARY") {
                    // Set the current EntryType
                    curGenericEntryItem.setType("DIARY");
                    logger.debug("Adding DIARY GenericEntryItem(" + entryItemID + ")");
                    
                    // Decode the Diary Information
                    Diary diary = (Diary)remedyEntryItems[i].getValue().getValue();
                    
                    // If there are diary entries
                    if (diary != null) {
                        // Decode the diary
                        DiaryInfo[] diaryInfo = diary.decode(context.getContext());

                        // For each diary entry object, add it to the Diary object
                        for(int j=0;j<diaryInfo.length;j++) {
                            // Construct the base DiaryEntry object
                            GenericComplexItem complexItem = new GenericComplexItem(String.valueOf(j+1));
                            complexItem.setType("DIARY_ENTRY");

                            // Break out the current values
                            String diaryData = diaryInfo[j].getDiaryInfo();
                            String timestamp = ArsUtil.convertArsTimeToIso8601Format(diaryInfo[j].getTimestamp().toString());
                            String user = diaryInfo[j].getUser().toString();

                            // Add the simple Items
                            complexItem.addGenericItem(new GenericItem("DiaryInfo", diaryData));
                            complexItem.addGenericItem(new GenericItem("Timestamp", timestamp));
                            complexItem.addGenericItem(new GenericItem("User", user));

                            // Add the DiaryEntry object to the Diary Object
                            logger.debug("Adding DIARY_ENTRY GenericComplexItem(" + user + ", " + timestamp + ", " + diaryData + ")");
                            curGenericEntryItem.addGenericComplexItem(complexItem);
                        }
                        
                        // Add the Diary object to the Entry object
                        genericEntry.addGenericEntryItem(curGenericEntryItem);
                    } else {
                        if (includeNullEntryItems) {
                            GenericEntryItem entryItem = new GenericEntryItem(entryItemID);
                            entryItem.setType("DIARY");
                            genericEntry.addGenericEntryItem(entryItem);
                        }
                    }
                } else if (dataType == "ENUM") {
                    // Get the current selection
                    entryItemValue = remedyEntryItems[i].getValue().toString();
                    
                    // The curValue can be null if nothing is selected in the enum
                    if (entryItemValue != null) {
                        logger.debug("entryItemValue: " + entryItemValue);
                        for(int j=0;j<entryItemValue.length();j++) {
                            // Check if the current character is a string
                            if (!Character.isDigit(entryItemValue.charAt(j))) {
                                break;
                            }
                            // If we are at the last element and all the characters are digits
                            else if (j+1==entryItemValue.length()) {
                                // Get a list of names for the Enum
                                EnumLimitInfo info = (EnumLimitInfo)currentField.getFieldLimit();
                                NameID[] names = ArsMultiversionManager.getNameIDs(info);

                                // Set the enum value
                                entryItemValue = names[Integer.parseInt(entryItemValue)].toString();
                            }
                        }

                        logger.debug("Adding ENUM GenericEntryItem(" + entryItemID + ", " + entryItemValue + ")");
                        curGenericEntryItem.setValue(entryItemValue);
                        genericEntry.addGenericEntryItem(curGenericEntryItem);
                    } else {
                        if (includeNullEntryItems) {
                            genericEntry.addGenericEntryItem(new GenericEntryItem(entryItemID));
                        }
                    }
                } else {
                    entryItemValue = remedyEntryItems[i].getValue().toString();
                    
                    if (entryItemValue != null) {
                        if (dataType == "DATE") {
                            entryItemValue = ArsUtil.convertArsDateToIso8601Format((DateInfo)remedyEntryItems[i].getValue().getValue());
                        } else if (dataType == "TIME") {
                            entryItemValue = ArsUtil.convertArsTimeToIso8601Format(entryItemValue);
                        } else if (dataType == "TIME_OF_DAY") {
                            entryItemValue = ArsUtil.convertArsTimeOfDayToIso8601Format(entryItemValue);
                        }
                    }

                    if (entryItemValue == null) { entryItemValue = ""; }
                    if (includeNullEntryItems || (entryItemValue != "" && entryItemValue != "null")) { 
                        logger.debug("Adding generic GenericEntryItem(" + entryItemID + ", " + entryItemValue + ")");
                        curGenericEntryItem.setValue(entryItemValue);
                        genericEntry.addGenericEntryItem(curGenericEntryItem);
                    }
                }
            } else if (fieldType == "ATTACHMENT") {
                // EntryItem attachmentItem = new EntryItem(remedyEntryItems[j].getFieldID().toString());
                logger.debug("Generating Attachment Object...");
                AttachmentInfo attachment = (AttachmentInfo)remedyEntryItems[i].getValue().getValue();
                
                if (attachment != null) {
                    curGenericEntryItem.setType("ATTACHMENT");
                    // Obtain parameters
                    String attachmentName = attachment.getName();
                    String attachmentSize = String.valueOf(attachment.getOriginalSize());

                    // Build the ComplexItem
                    GenericComplexItem complexItem = new GenericComplexItem();
                    complexItem.addGenericItem(new GenericItem("Name", attachmentName));
                    complexItem.addGenericItem(new GenericItem("Size", attachmentSize));

                    // Add the complexItem to our attachment object
                    curGenericEntryItem.addGenericComplexItem(complexItem);

                    // Add the attachment objec to our entry
                    genericEntry.addGenericEntryItem(curGenericEntryItem);
                } else {
                    if (includeNullEntryItems) {
                        GenericEntryItem entryItem = new GenericEntryItem(entryItemID);
                        entryItem.setType("ATTACHMENT");
                        genericEntry.addGenericEntryItem(entryItem);
                    }
                }
            } else {
                logger.debug("Its not a field with data.");
            }
        }
        
        return genericEntry;
    }
    
    public static Entry generateArsEntry(ArsContext context, String genericRemedyEntryString) throws ModelException {
        return generateArsEntry(context, genericRemedyEntryString, null);
    }
    
    public static Entry generateArsEntry(ArsContext context, String genericRemedyEntryString, String structureIDString) throws ModelException {
        return generateArsEntry(context, genericRemedyEntryString, structureIDString, null);
    }
    
    public static Entry generateArsEntry(ArsContext context, String genericRemedyEntryString, String structureIDString, String entryIDString) throws ModelException {
        logger.debug("Generating new Remedy Entry.");
        logger.debug("GenericEntryString: " + genericRemedyEntryString);
        Entry entry = (Entry)EntryFactory.getFactory().newInstance();
        EntryItem[] entryItems = new EntryItem[0];
        FieldID curFieldID = new FieldID();
        
        try {
            // Create a new Xml Document object
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(genericRemedyEntryString)));
            
            // Get the Schema ID
            Node firstChild = doc.getFirstChild();
            if (firstChild != null) {
                // Check for the structure information
                NamedNodeMap entryAttributes = firstChild.getAttributes();
                Node schemaIDAttribute = entryAttributes.getNamedItem("Structure");
                Node entryIDAttribute = entryAttributes.getNamedItem("ID");
                
                if (schemaIDAttribute != null) {
                    if (structureIDString != null && !structureIDString.equals(schemaIDAttribute.getNodeValue())) {
                        throw new ModelException("Unable to process an entry with conflicting inherent (specified as Entry Xml attribute) and explicit structure IDs.");
                    }
                    logger.debug("Setting SchemaID inherantly: " + schemaIDAttribute.getNodeValue());
                    entry.setSchemaID(new NameID(schemaIDAttribute.getNodeValue()));
                } else if (structureIDString != null) {
                    logger.debug("Setting SchemaID explicitly: " + structureIDString);
                    entry.setSchemaID(new NameID(structureIDString));
                } else {
                    String msg = "Unable to process an entry missing the Structure attribute.";
                    logger.warn(msg);
                    throw new ModelException(msg);
                }
                
                if (entryIDAttribute != null) {
                    if (entryIDString != null && !entryIDString.equals(entryIDAttribute.getNodeValue())) {
                        throw new ModelException("Unable to process an entry with conflicting inherent (specified as Entry Xml attribute) and explicit IDs.");
                    }
                    logger.debug("Setting entry ID inherantly: " + entryIDAttribute.getNodeValue());
                    entry.setEntryID(new EntryID(entryIDAttribute.getNodeValue()));
                } else if (entryIDString != null) {
                    logger.debug("Setting entry ID explicitly: " + entryIDString);
                    entry.setEntryID(new EntryID(entryIDString));
                }

                // Get a list of entry Items
                NodeList items = doc.getElementsByTagName("EntryItem");
                entryItems = new EntryItem[items.getLength()];
                logger.debug("Retrieved " + items.getLength() + " EntryItems.");
                
                // Build up the FieldList
                logger.debug("Generating fieldList.");
                FieldListCriteria fieldListCriteria = new FieldListCriteria(entry.getSchemaID(), new Timestamp(0), FieldType.AR_ALL_FIELD);
                FieldCriteria fieldCriteria = new FieldCriteria();
                fieldCriteria.setRetrieveAll(true);
                Field[] fieldList = FieldFactory.findObjects(context.getContext(), fieldListCriteria, fieldCriteria);

                // Build up the FieldID->fieldType map
                logger.debug("Generating field map.");
                LinkedHashMap fieldTypes = new LinkedHashMap();
                for (int i=0;i<fieldList.length;i++) {
                    fieldTypes.put(fieldList[i].getFieldID(), fieldList[i]);
                }  

                // Parse through the nodes
                Node curNode, curTypeAttribute, curIDAttribute;
                NamedNodeMap curAttributes;
                Value curValue;
                for(int i=0;i<items.getLength();i++) {
                    // Grab the current node information
                    curNode = items.item(i);
                    curAttributes = curNode.getAttributes();

                    // Grab The attributes
                    curTypeAttribute = curAttributes.getNamedItem("Type");
                    curIDAttribute = curAttributes.getNamedItem("ID");
                    
                    // Check for a valid ID attribute
                    if (curIDAttribute == null) {
                        throw new ModelException("All EntryItem objects must have an \"ID\" attribute.");
                    }
                    
                    logger.debug("Current ID: " + curIDAttribute.getNodeValue());

                    // Generate the fieldID
                    try {
                        curFieldID = new FieldID(Long.parseLong(curIDAttribute.getNodeValue()));
                    } catch (NumberFormatException e) {
                        throw new ModelException("The string \"" + curIDAttribute.getNodeValue() + "\" is not a valid field id.");
                    }
                    
                    // If there is not a type then this is a simple entry item
                    if (curTypeAttribute == null) {
                        logger.debug("Creating the " + i + " entry item.");
                        Field curField = (Field)fieldTypes.get(curFieldID);
                        if (curField.getDataType() == DataType.DATE) {
                            if (curNode.getFirstChild() != null) {
                                entryItems[i] = new EntryItem(curFieldID, new Value(ArsUtil.convertIso8601FormatToArsDate(curNode.getFirstChild().getNodeValue())));
                            } else {
                                entryItems[i] = new EntryItem(curFieldID, new Value());
                            }
                        } else if (curField.getDataType() == DataType.TIME) {
                            if (curNode.getFirstChild() != null) {
                                entryItems[i] = new EntryItem(curFieldID, new Value(ArsUtil.convertIso8601FormatToArsTime(curNode.getFirstChild().getNodeValue())));
                            } else {
                                entryItems[i] = new EntryItem(curFieldID, new Value());
                            }
                        } else if (curField.getDataType() == DataType.TIME_OF_DAY) {
                            if (curNode.getFirstChild() != null) {
                                entryItems[i] = new EntryItem(curFieldID, new Value(ArsUtil.convertIso8601FormatToArsTimeOfDay(curNode.getFirstChild().getNodeValue())));
                            } else {
                                entryItems[i] = new EntryItem(curFieldID, new Value());
                            }
                        } else {
                            if (curNode.getFirstChild() != null) {
                                entryItems[i] = new EntryItem(curFieldID, new Value(curNode.getFirstChild().getNodeValue()));
                            } else {
                                entryItems[i] = new EntryItem(curFieldID, new Value());
                            }
                        }
                    }
                    // If the current node has a type of ATTACHMENT
                    else if (curTypeAttribute.getNodeValue().equalsIgnoreCase("ATTACHMENT")) {
                        NodeList attachmentComplexItem = curNode.getChildNodes();
                        NodeList attachmentItems = attachmentComplexItem.item(0).getChildNodes();
                        String data = null, name = null, size = null;
                        
                        for (int j=0;j<attachmentItems.getLength();j++) {
                            Node curItem = attachmentItems.item(j);
                            
                            String curItemValue = new String();
                            if (curItem.getFirstChild() != null) { curItemValue = curItem.getFirstChild().getNodeValue(); }
                            
                            NamedNodeMap curItemAttributes = curItem.getAttributes();
                            
                            if (curItemAttributes != null && curItemAttributes.getNamedItem("ID") != null) {
                                if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("Data")) {
                                    data = curItemValue;
                                } else if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("Name")) {
                                    name = curItemValue;
                                } else if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("Size")) {
                                    size = curItemValue;
                                }
                            }
                        }
                        
                        if (data != null && name != null && size != null) {
                            byte[] buf = new sun.misc.BASE64Decoder().decodeBuffer(data);
                            AttachmentInfo attachInfo = new AttachmentInfo(name, Long.parseLong(size), 0, buf);
                            entryItems[i] = new EntryItem(curFieldID, new Value(attachInfo));
                        } else {
                            throw new ModelException("Unable to generate ArsEntry object, ATTACHMENT EntryItem malformed.");
                        }
                    }
                    // If the current node has a type of DIARY
                    else if (curTypeAttribute.getNodeValue().equalsIgnoreCase("DIARY")) {
                        NodeList diaryComplexItems = curNode.getChildNodes();
                        DiaryInfo[] diaryEntries = new DiaryInfo[diaryComplexItems.getLength()];
                        
                        for (int j=0;j<diaryComplexItems.getLength();j++) {
                            NodeList diaryItems = diaryComplexItems.item(j).getChildNodes();
                            
                            String diaryInfo = null, timestamp = null, user = null;
                            for (int k=0;k<diaryItems.getLength();k++) {
                                Node curItem = diaryItems.item(k);
                                NamedNodeMap curItemAttributes = curItem.getAttributes();
                                
                                String curItemValue = new String();
                                if (curItem.getFirstChild() != null) { curItemValue = curItem.getFirstChild().getNodeValue(); }

                                if (curItemAttributes != null && curItemAttributes.getNamedItem("ID") != null) {
                                    if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("DiaryInfo")) {
                                        diaryInfo = curItemValue;
                                    } else if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("TimeStamp")) {
                                        timestamp = curItemValue;
                                    } else if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("User")) {
                                        user = curItemValue;
                                    }
                                }
                            }

                            if (diaryInfo != null && timestamp != null && user != null) {
                                DiaryInfo diaryInfoEntry = new DiaryInfo(new AccessNameID(user), diaryInfo, new Timestamp(Long.parseLong(timestamp)));
                                diaryEntries[j] = diaryInfoEntry;
                            } else {
                                throw new ModelException("Unable to generate ArsEntry object, DIARY EntryItem malformed.");
                            }
                        }
                        
                        Diary diary = new Diary(Util.AREncodeDiary(new ARServerUser(), diaryEntries));
                        entryItems[i] = new EntryItem(curFieldID, new Value(diary));
                    }
                    // If the current node has a type of STATUS_HISTORY
                    else if (curTypeAttribute.getNodeValue().equalsIgnoreCase("STATUS_HISTORY")) {
                        NodeList statusHistoryComplexItems = curNode.getChildNodes();
                        StatusHistoryInfo[] historyEntries = new StatusHistoryInfo[statusHistoryComplexItems.getLength()];
                        
                        for (int j=0;j<statusHistoryComplexItems.getLength();j++) {
                            NodeList historyEntryItems = statusHistoryComplexItems.item(j).getChildNodes();
                            
                            String timestamp = null, user = null;
                            for (int k=0;k<historyEntryItems.getLength();k++) {
                                Node curItem = historyEntryItems.item(k);
                                NamedNodeMap curItemAttributes = curItem.getAttributes();

                                if (curItemAttributes != null && curItemAttributes.getNamedItem("ID") != null) {
                                    if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("Timestamp")) {
                                        if (curItem.getFirstChild() != null) {
                                            timestamp = ArsUtil.convertIso8601FormatToArsTime(curItem.getFirstChild().getNodeValue());
                                        }
                                    } else if (curItemAttributes.getNamedItem("ID").getNodeValue().equalsIgnoreCase("User")) {
                                        if (curItem.getFirstChild() != null) {
                                            user = curItem.getFirstChild().getNodeValue();
                                        }
                                    }
                                }
                            }

                            if (timestamp != null && user != null) {
                                StatusHistoryInfo statusHistoryInfoEntry = new StatusHistoryInfo(new AccessNameID(user), new Timestamp(Long.parseLong(timestamp)));
                                historyEntries[j] = statusHistoryInfoEntry;
                            } else {
                                throw new ModelException("Unable to generate ArsEntry object, STATUS_HISTORY EntryItem malformed.");
                            }
                        }
                        
                        StatusHistory history = new StatusHistory(Util.AREncodeStatusHistory(new ARServerUser(), historyEntries));
                        entryItems[i] = new EntryItem(curFieldID, new Value(history.getValue()));
                    }
                    // If the current node has a type we don't recognize
                    else {
                        logger.error("Unable to generate ArsEntry object, don't know how to handle a " + curTypeAttribute.getNodeValue() + " EntryItem.");
                        throw new ModelException("Unable to generate ArsEntry object, don't know how to handle a " + curTypeAttribute.getNodeValue() + " EntryItem.");
                    }
                }
                
                // Set the entry items
                logger.debug("Setting the entry items.");
                entry.setEntryItems(entryItems);
            }
        } catch (java.text.ParseException e) {
            String msg = "There was a problem translating field " + curFieldID.getValue() + ": " + e.getMessage() + ", the number starting at the index " + e.getErrorOffset() + " is not valid.";
            throw new ModelException(msg);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg == "") { msg = e.toString(); }
            throw new ModelException(msg);
        }
        
        logger.debug("Returning the entry.");
        return entry;
    }
    
    // Attributes Beanish Methods
    public String[] getAttributeNames() { return genericEntry.getAttributeNames(); }
    public String getAttribute(String name) { return genericEntry.getAttribute(name); }
    public void addAttribute(String name, String value) { genericEntry.addAttribute(name, value); }
    public void clearAttributes() { genericEntry.clearAttributes(); }
    public void removeAttribute(String name) { genericEntry.removeAttribute(name); }
    
    /**
     * Retrieve the xml representation of this object.
     */
    public Element generateXmlElement() {
        genericEntry.includeNullEntryItems(this.includeNullElements);
        Element entryElement = genericEntry.generateXmlElement();
        
        return entryElement;
    }
}
