package com.kd.klink.ars.model;

import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.model.entry.*;
import com.kd.klink.model.response.*;

/**
 *
 */
public class ArsEntryList extends ArsObject {
    private static final Logger logger = Logger.getLogger(ArsEntryList.class.getName());
    private GenericEntryList genericEntryList = new GenericEntryList();
    
    public ArsEntryList() {}
    
    public void addGenericEntry(GenericEntry entry) {
        genericEntryList.addGenericEntry(entry);
    }
    public GenericEntryList getGenericEntryList() {
        return genericEntryList;
    }
    
    public ArsEntryList(ArsContext context, 
                        String structure, 
                        long[] includeItems, 
                        Integer limit,
                        String qualification, 
                        int[][] ranges, 
                        long[] sortItems, 
                        String target,
                        boolean includeNullEntryItems) throws ARException {
        
        logger.debug("Constructing new RemedyEntryList object...");

        // Generate the base object for our internal representation
        genericEntryList = new GenericEntryList();
        genericEntryList.setStructure(structure);

        // Set the default limit based on the server setting
        if (limit == null) {
            int[] maxQueryEntriesReturnedConfigList = {Constants.AR_SERVER_INFO_MAX_ENTRIES};
            ServerInfo[] retrievedConfigItems = Util.ARGetServerInfo(context.getContext(), maxQueryEntriesReturnedConfigList);
            limit = new Integer(Integer.parseInt(retrievedConfigItems[0].getValue().toString()));
        }
        genericEntryList.addAttribute("Limit", limit.toString());
        logger.debug("Server Limit: " + limit.toString());
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        // Set the default qualification attribute
        logger.debug("Adding Qualification attribute...");
        if (qualification == null || qualification == "" || qualification == new String()) {
            qualification = "1=1";
        } else if (qualification != "1=1") {
            genericEntryList.addAttribute("Qualification", qualification);
        }
        
        // Set the default ranges attribute
        logger.debug("Adding Ranges attribute...");
        if (ranges == null) {
            ranges = new int[0][0];
        } else if ( ranges.length > 0) {
            String rangesAttributeValue = "";
            for (int i=0;i<ranges.length;i++) {
                if (ranges[i] != null && ranges[i].length > 0) {
                    if (rangesAttributeValue != "") { rangesAttributeValue += ","; }
                    if (ranges[i].length > 1) {
                        rangesAttributeValue += ranges[i][0] + "-" + ranges[i][ranges[i].length-1];
                    } else {
                        rangesAttributeValue += ranges[i][0];
                    }
                }
            }
            genericEntryList.addAttribute("Ranges", rangesAttributeValue);
        }
        
        // Set the default sortItems Array
        logger.debug("Adding SortItems attribute...");
        if (sortItems == null || sortItems.length == 0) { 
            sortItems = new long[1];
            sortItems[0] = 1;
        } else {
            String sortItemsString = "";
            for (int i=0;i<sortItems.length;i++) {
                // If we are not the first element then add a comma to separate from the previous element
                if (sortItemsString != "") { sortItemsString += ","; }
                
                // Append the numerical value of the item
                sortItemsString += String.valueOf(Math.abs(sortItems[i]));
                
                // Append the proper sort order
                if (sortItems[i] < 0) { sortItemsString += "-"; }
                else { sortItemsString += "+"; }
            }
            genericEntryList.addAttribute("SortItems", sortItemsString);
        }
        
        // Set the default sortTarget
        logger.debug("Adding Target attribute...");
        if (target == null || target == "" || target == new String()) {
            target = "all";
        } else if (target != "all") {
            genericEntryList.addAttribute("Target", target);
        }
        
        // Build up the SortInfo
        logger.debug("Building SortInfo array...");
        SortInfo[] sortInfos = new SortInfo[sortItems.length];
        for (int i=0;i<sortItems.length;i++) {
            // Create a new sort info ascending (1); decending is 2
            if (sortItems[i] < 0) { 
                sortInfos[i] = new SortInfo(new InternalID(Math.abs(sortItems[i])), 2);
            } else {
                sortInfos[i] = new SortInfo(new InternalID(sortItems[i]), 1);
            }
        }
        
        // Build up the FieldList
        logger.debug("Generating fieldList...");
        FieldListCriteria fieldListCriteria = new FieldListCriteria(new NameID(structure), new Timestamp(0), FieldType.AR_DATA_FIELD);
        FieldCriteria fieldCriteria = new FieldCriteria();
        fieldCriteria.setRetrieveAll(true);
        Field[] fieldList = FieldFactory.findObjects(context.getContext(), fieldListCriteria, fieldCriteria);
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        // Build the EntryListCriteria
        logger.debug("Generating listCriteria...");
        QualifierInfo qual = Util.ARGetQualifier(context.getContext(), qualification, fieldList, null, Constants.AR_QUALCONTEXT_DEFAULT);
        EntryListCriteria listCriteria = new EntryListCriteria(new NameID(structure), qual, limit.intValue(), new Timestamp(0), sortInfos, (EntryID[])null);
        
        // Set the default target list
        GenericEntry[] targeteableEntries = new GenericEntry[0];
        
        // If we don't need to include any fields then retrieve EntryListInfos
        if (includeItems == null || includeItems.length == 0) {
            logger.debug("Obtaining EntryKey objects...");
            EntryKey[] keys = EntryFactory.find(context.getContext(), listCriteria, false, null);
            
            // Add the Context Messages
            super.addMessages(context.getContext().getLastStatus());
            
            if (keys == null) {
                genericEntryList.setCount("0");
            } else {
                genericEntryList.setCount(String.valueOf(keys.length));
                targeteableEntries = new GenericEntry[keys.length];
                for (int i=0;i<keys.length;i++) {
                    targeteableEntries[i] = new GenericEntry(keys[i].getEntryID().toString());
                }
            }
        }
        // If we do need to include fields then go through and retrieve the entier Entry object
        else {
            // Build up the FieldID->fieldType map
            logger.debug("Generating field map...");
            Map fieldTypes = new LinkedHashMap();
            for (int i=0;i<fieldList.length;i++) {
                fieldTypes.put(fieldList[i].getFieldID(), fieldList[i]);
            }
            
            // Build the EntryCriteria
            logger.debug("Generating entryCriteria...");
            EntryListFieldInfo[] listFieldInfo = new EntryListFieldInfo[includeItems.length];
            for (int i=0;i<includeItems.length;i++) {
                listFieldInfo[i] = new EntryListFieldInfo(new FieldID(includeItems[i]));
            }
            EntryCriteria criteria = new EntryCriteria(listFieldInfo);
            
            // Build up our entryList
            logger.debug("Obtaining Entry objects...");
            EntryItem[] remedyEntryItems;
            Entry[] remedyEntries = EntryFactory.findObjects(context.getContext(), listCriteria, criteria, false, null);
            genericEntryList.setCount(String.valueOf(remedyEntries.length));
            
            // Add the Context Messages
            super.addMessages(context.getContext().getLastStatus());
            
            // Build up the targeteableEntries
            targeteableEntries = new GenericEntry[remedyEntries.length];
            for (int i=0;i<remedyEntries.length;i++) {
                // Generate the genericEntry
                GenericEntry genericEntry = ArsEntry.generateGenericEntry(context, remedyEntries[i], includeNullEntryItems);
                genericEntry.includeNullEntryItems(includeNullEntryItems);

                // Remove the structure info since we already know the structure form the EntryList
                genericEntry.setStructure("");

                // Add the genericEntry as a targeteable entry
                targeteableEntries[i] = genericEntry;
            }
        }
        
        // If there are specific ranges we want
        if (ranges.length > 0) {
            logger.debug("Initial targeteableEntries size: " + targeteableEntries.length);
            // Create an ArrayList we can use to add targeteable entries
            ArrayList rangedTargeteableEntries = new ArrayList();

            // For each of the ranges
            for (int curRangeIndex=0;curRangeIndex<ranges.length;curRangeIndex++) {
                int[] currentRange = ranges[curRangeIndex];
                // For each item in that range
                for (int curItemIndex=0;curItemIndex<currentRange.length;curItemIndex++) {
                    // The current item (must subract one since items are indexed by 1 and java indexes arrays by 0
                    int currentEntryIndex = currentRange[curItemIndex]-1;

                    // If the currentEntryIndex is within the range of remedyEntries retrieved
                    if (currentEntryIndex >= 0 && currentEntryIndex < targeteableEntries.length) {
                        // Add the genericEntry as a targeteable entry
                        rangedTargeteableEntries.add(targeteableEntries[currentEntryIndex]);
                    }
                }
            }

            // Build up the new targeteableEntry array
            targeteableEntries = new GenericEntry[rangedTargeteableEntries.size()];
            for (int i=0;i<rangedTargeteableEntries.size();i++) {
                targeteableEntries[i] = (GenericEntry)rangedTargeteableEntries.get(i);
            }
        }
        
        // Figure out the target
        if (target.equalsIgnoreCase("min")) {
            genericEntryList.addGenericEntry(targeteableEntries[0]);
        } else if (target.equalsIgnoreCase("max")) {
            genericEntryList.addGenericEntry(targeteableEntries[targeteableEntries.length-1]);
        } else if (target.equalsIgnoreCase("rand")) {
            Random rand = new Random();
            genericEntryList.addGenericEntry(targeteableEntries[rand.nextInt(targeteableEntries.length)]);
        } else {
            logger.debug("targeteableentries Length: " + targeteableEntries.length);
            for (int i=0;i<targeteableEntries.length;i++) {
                genericEntryList.addGenericEntry(targeteableEntries[i]);
            }
        }
        
        logger.debug("RemedyEntryList constructed.");
    }
    
    public Element generateXmlElement() {
        Element entryListElement = genericEntryList.generateXmlElement();
        
        return entryListElement;
    }
}
