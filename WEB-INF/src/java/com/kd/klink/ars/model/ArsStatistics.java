package com.kd.klink.ars.model;

import java.util.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

/**
 *
 */
public class ArsStatistics extends ArsObject {
    private static final Logger logger = Logger.getLogger(ArsStatistics.class.getName());
    private String[] itemList;
    
    // A hash (String->Value) representing the values of the context's server options
    private Map statisticItemValues = new LinkedHashMap();
    
    // A hash (String->Integer) representing which server options to retrieve
    private static Map statisticItems;

    /**
    *
    */
    public ArsStatistics(ArsContext context, String[] itemList) throws ARException {
        logger.debug("Constructing new ArsStatistics object...");
        
        // Set the itemList so that when we generate the Xml representation we know which items are required
        this.itemList = itemList;
        
        // configure the default request list
        int[] statisticRequestList = new int[0];
        
        // If the itemList has no elements then the request list should retrieve everything
        if (itemList.length == 0) {
            statisticRequestList = new int[statisticItems.size()];
            logger.debug("Building statisticRequestList [" + statisticRequestList.length + "]");
         
            // Build up the statisticRequestList
            Iterator statisticItemIterator = statisticItems.keySet().iterator();
            for(int i=0;statisticItemIterator.hasNext();i++) {
                String curStatisticItemName = (String)statisticItemIterator.next();
                statisticRequestList[i] = ((Integer)statisticItems.get(curStatisticItemName)).intValue();
            }
        }
        // If the itemList does contain elemented then the request should only retrieve the requested items
        else {
            statisticRequestList = new int[itemList.length];
            logger.debug("Building statisticRequestList [" + statisticRequestList.length + "]");
            
            // Build up the statisticRequestList
            for (int i=0;i<itemList.length;i++) {
                statisticRequestList[i] = ((Integer)statisticItems.get(itemList[i])).intValue();
            }
        }
        
        // As long as there are items to retrieve
        if (statisticRequestList.length > 0) {
            // Retrive the values from the server
            ServerInfo[] retrievedStatisticItems = Util.ARGetServerStatistics(context.getContext(), statisticRequestList);
            logger.debug("Number of retrievedStatisticItems: " + retrievedStatisticItems.length);

            // Add the Context Messages
            super.addMessages(context.getContext().getLastStatus());
            
            // Obtain all of the values and map them to the operation
            Integer operation;
            String value;
            for(int i=0;i<retrievedStatisticItems.length;i++) {
                operation = new Integer(retrievedStatisticItems[i].getOperation());
                if (retrievedStatisticItems[i].getValue() == null || retrievedStatisticItems[i].getValue().toString() == null) { 
                    value = "";
                } else {
                    value = retrievedStatisticItems[i].getValue().toString();
                }
                
                logger.debug("Adding (" + operation + ", " + value + ") to statisticItemValues.");
                statisticItemValues.put(operation, value);
            }
        }
        
        logger.debug("ArsStatistics object constructed.");
    }
    
   /**
    *
    */
    public Element generateXmlElement() {
        // Create the root element
        Element statistics = document.createElement("Statistics");

        // If specific items were requested only generate those
        if (this.itemList.length > 0) {
            for (int i=0;i<itemList.length;i++) {
                Element curElement = document.createElement("Statistic");
                curElement.setAttribute("Name", itemList[i]);
                curElement.appendChild(document.createTextNode((String)statisticItemValues.get(statisticItems.get(itemList[i]))));
                statistics.appendChild(curElement);
            }
        }
        // If no items were explicitely requested return all statisticuration items
        else {
            Iterator statisticIterator = statisticItems.keySet().iterator();
            String curName;
            while(statisticIterator.hasNext()) {
                curName = (String)statisticIterator.next();
                Element curElement = document.createElement("Statistic");
                curElement.setAttribute("Name", curName);
                curElement.appendChild(document.createTextNode((String)statisticItemValues.get(statisticItems.get(curName))));
                statistics.appendChild(curElement);
            }
        }

        return statistics;
    }
    
    static {
        statisticItems = new LinkedHashMap();
        
        statisticItems.put("API_REQUESTS", new Integer(Constants.AR_SERVER_STAT_API_REQUESTS));
        statisticItems.put("API_TIME", new Integer(Constants.AR_SERVER_STAT_API_TIME));
        statisticItems.put("AR_CREATE_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_CREATE_E_COUNT));
        statisticItems.put("AR_CREATE_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_CREATE_E_TIME));
        statisticItems.put("AR_DELETE_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_DELETE_E_COUNT));
        statisticItems.put("AR_DELETE_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_DELETE_E_TIME));
        statisticItems.put("AR_GET_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_GET_E_COUNT));
        statisticItems.put("AR_GET_ENTRY_STATISTICS_CALLS", new Integer(Constants.AR_SERVER_STAT_E_STATS_COUNT));
        statisticItems.put("AR_GET_ENTRY_STATISTICS_TIME", new Integer(Constants.AR_SERVER_STAT_E_STATS_TIME));
        statisticItems.put("AR_GET_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_GET_E_TIME));
        statisticItems.put("AR_GET_LIST_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_GETLIST_E_COUNT));
        statisticItems.put("AR_GET_LIST_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_GETLIST_E_TIME));
        statisticItems.put("AR_MERGE_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_MERGE_E_COUNT));
        statisticItems.put("AR_MERGE_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_MERGE_E_TIME));
        statisticItems.put("AR_SET_ENTRY_CALLS", new Integer(Constants.AR_SERVER_STAT_SET_E_COUNT));
        statisticItems.put("AR_SET_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_SET_E_TIME));
        statisticItems.put("BAD_PASSWORDS", new Integer(Constants.AR_SERVER_STAT_BAD_PASSWORD));
        statisticItems.put("BLOCKED_PROCESSES", new Integer(Constants.AR_SERVER_STAT_NUMBER_BLOCKED));
        statisticItems.put("CACHE_TIME", new Integer(Constants.AR_SERVER_STAT_CACHE_TIME));
        statisticItems.put("CONNECTED_USERS_WITHOUT_FULL_TEXT_SEARCH", new Integer(Constants.AR_SERVER_STAT_FULL_NONE));
        statisticItems.put("CPU_TOTAL_TIME", new Integer(Constants.AR_SERVER_STAT_CPU));
        statisticItems.put("CURRENT_FIXED_FULL_TEXT_SEARCH_USERS", new Integer(Constants.AR_SERVER_STAT_FULL_FIXED));
        statisticItems.put("CURRENT_FIXED_WRITE_USERS", new Integer(Constants.AR_SERVER_STAT_WRITE_FIXED));
        statisticItems.put("CURRENT_FLOATING_FULL_TEXT_SEARCH_USERS", new Integer(Constants.AR_SERVER_STAT_FULL_FLOATING));
        statisticItems.put("CURRENT_FLOATING_WRITE_USERS", new Integer(Constants.AR_SERVER_STAT_WRITE_FLOATING));
        statisticItems.put("CURRENT_NO_WRITE_USERS", new Integer(Constants.AR_SERVER_STAT_WRITE_READ));
        statisticItems.put("CURRENT_USERS", new Integer(Constants.AR_SERVER_STAT_CURRENT_USERS));
        statisticItems.put("ESCALATIONS_DISABLED", new Integer(Constants.AR_SERVER_STAT_ESCL_DISABLE));
        statisticItems.put("ESCALATIONS_EXECUTED", new Integer(Constants.AR_SERVER_STAT_ESCL_PASSED));
        statisticItems.put("ESCALATIONS_SKIPPED", new Integer(Constants.AR_SERVER_STAT_ESCL_FAILED));
        statisticItems.put("FILTERS_DISABLED", new Integer(Constants.AR_SERVER_STAT_FILTER_DISABLE));
        statisticItems.put("FILTERS_EXECUTED", new Integer(Constants.AR_SERVER_STAT_FILTER_PASSED));
        statisticItems.put("FILTERS_SKIPPED", new Integer(Constants.AR_SERVER_STAT_FILTER_FAILED));
        statisticItems.put("FULL_TEXT_SEARCH_CALLS", new Integer(Constants.AR_SERVER_STAT_FTS_SRCH_COUNT));
        statisticItems.put("FULL_TEXT_SEARCH_TIME", new Integer(Constants.AR_SERVER_STAT_FTS_SRCH_TIME));
        statisticItems.put("NO_FULL_TOKEN", new Integer(Constants.AR_SERVER_STAT_NO_FULL_TOKEN));
        statisticItems.put("NO_WRITE_TOKEN", new Integer(Constants.AR_SERVER_STAT_NO_WRITE_TOKEN));
        statisticItems.put("PERFORMED_LOG_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_LOG));
        statisticItems.put("PERFORMED_LOG_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_LOG));
        statisticItems.put("PERFORMED_MESSAGE_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_MESSAGE));
        statisticItems.put("PERFORMED_NOTIFY_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_NOTIFY));
        statisticItems.put("PERFORMED_NOTIFY_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_NOTIFY));
        statisticItems.put("PERFORMED_PROCESS_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_PROCESS));
        statisticItems.put("PERFORMED_PROCESS_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_PROCESS));
        statisticItems.put("PERFORMED_PUSH_FIELD_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_FIELDP));
        statisticItems.put("PERFORMED_PUSH_FIELD_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_FIELDP));
        statisticItems.put("PERFORMED_SET_FIELD_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_FIELDS));
        statisticItems.put("PERFORMED_SET_FIELD_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_FIELDS));
        statisticItems.put("PERFORMED_SQL_ESCALATIONS", new Integer(Constants.AR_SERVER_STAT_ESCL_SQL));
        statisticItems.put("PERFORMED_SQL_FILTERS", new Integer(Constants.AR_SERVER_STAT_FILTER_SQL));
        statisticItems.put("PROCESSES_BLOCKED", new Integer(Constants.AR_SERVER_STAT_TIMES_BLOCKED));
        statisticItems.put("QUEUED_THREADS", new Integer(Constants.AR_SERVER_STAT_NUM_THREADS));
        statisticItems.put("SERVER_START_TIME", new Integer(Constants.AR_SERVER_STAT_START_TIME));
        statisticItems.put("SQL_DATABASE_COMMAND_COUNT", new Integer(Constants.AR_SERVER_STAT_SQL_DB_COUNT));
        statisticItems.put("SQL_DATABASE_COMMAND_TIME", new Integer(Constants.AR_SERVER_STAT_SQL_DB_TIME));
        statisticItems.put("TOTAL_DATABASE_RESTRUCTURE_TIME", new Integer(Constants.AR_SERVER_STAT_RESTRUCT_TIME));
        statisticItems.put("TOTAL_ENTRY_TIME", new Integer(Constants.AR_SERVER_STAT_ENTRY_TIME));
        statisticItems.put("TOTAL_ESCALATION_TIME", new Integer(Constants.AR_SERVER_STAT_ESCL_TIME));
        statisticItems.put("TOTAL_FILTER_TIME", new Integer(Constants.AR_SERVER_STAT_FILTER_TIME));
        statisticItems.put("TOTAL_IDLE_TIME", new Integer(Constants.AR_SERVER_STAT_IDLE_TIME));
        statisticItems.put("TOTAL_NETWORK_RESPONSE_TIME", new Integer(Constants.AR_SERVER_STAT_NET_RESP_TIME));
        statisticItems.put("TOTAL_OTHER_TIME", new Integer(Constants.AR_SERVER_STAT_OTHER_TIME));
        statisticItems.put("UPTIME", new Integer(Constants.AR_SERVER_STAT_SINCE_START));
    }
}