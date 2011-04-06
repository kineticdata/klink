package com.kd.klink.ars.model;

import java.util.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.ars.verification.*;

/**
 * 
 */
public class ArsConfiguration extends ArsObject {
    private static final Logger logger = Logger.getLogger(ArsConfiguration.class.getName());
    private String[] itemList;
    
    // A hash (String->Value) representing the values of the context's server options
    private Map configItemValues = new LinkedHashMap();
    
    // A hash (String->Integer) representing which server options to retrieve
    private static Map configItems;

    /**
    *
    */
    public ArsConfiguration(ArsContext context, String[] itemList) throws ARException {
        logger.debug("Constructing new ArsConfiguration object...");
        
        // Set the itemList so that when we generate the Xml representation we know which items are required
        this.itemList = itemList;
        
        // Configure the default request list
        int[] configRequestList = new int[0];
        
        // If the itemList has no elements then the request list should retrieve everything
        if (itemList.length == 0) {
            configRequestList = new int[configItems.size()];
            logger.debug("Building configRequestList [" + configRequestList.length + "]");
         
            // Build up the configRequestList
            Iterator configItemIterator = configItems.keySet().iterator();
            for(int i=0;configItemIterator.hasNext();i++) {
                String curConfigItemName = (String)configItemIterator.next();
                configRequestList[i] = ((Integer)configItems.get(curConfigItemName)).intValue();
            }
        }
        // If the itemList does contain elemented then the request should only retrieve the requested items
        else {
            configRequestList = new int[itemList.length];
            logger.debug("Building configRequestList [" + configRequestList.length + "]");
            
            // Build up the configRequestList
            for (int i=0;i<itemList.length;i++) {
                configRequestList[i] = ((Integer)configItems.get(itemList[i])).intValue();
            }
        }
        
        // As long as there are items to retrieve
        if (configRequestList.length > 0) {
            // Retrive the values from the server
            ServerInfo[] retrievedConfigItems = Util.ARGetServerInfo(context.getContext(), configRequestList);
            logger.debug("Number of retrievedConfigItems: " + retrievedConfigItems.length);
            
            // Set the Messages
            super.addMessages(context.getContext().getLastStatus());

            // Obtain all of the values and map them to the operation
            Integer operation;
            String value;
            for(int i=0;i<retrievedConfigItems.length;i++) {
                operation = new Integer(retrievedConfigItems[i].getOperation());
                if (retrievedConfigItems[i].getValue() == null || retrievedConfigItems[i].getValue().toString() == null) { 
                    value = "";
                } else {
                    value = retrievedConfigItems[i].getValue().toString();
                }
                
                logger.debug("Adding (" + operation + ", " + value + ") to configItemValues.");
                configItemValues.put(operation, value);
            }
        }
        
        logger.debug("ArsConfiguration constructed.");
    }
    
   /**
    *
    */
    public Element generateXmlElement() {
        logger.debug("Generating an ArsConfiguration Xml Element...");
        Element configurationItems = document.createElement("Configurations");
            
        // If specific items were requested only generate those
        if (this.itemList.length > 0) {
            for (int i=0;i<itemList.length;i++) {
                Element curElement = document.createElement("Configuration");
                curElement.setAttribute("Name", itemList[i]);
                curElement.appendChild(document.createTextNode((String)configItemValues.get(configItems.get(itemList[i]))));
                configurationItems.appendChild(curElement);
            }
        }
        // If no items were explicitely requested return all configuration items
        else {
            Iterator configIterator = configItems.keySet().iterator();
            String curName;
            while(configIterator.hasNext()) {
                curName = (String)configIterator.next();
                Element curElement = document.createElement("Configuration");
                curElement.setAttribute("Name", curName);
                curElement.appendChild(document.createTextNode((String)configItemValues.get(configItems.get(curName))));
                configurationItems.appendChild(curElement);
            }
        }

        return configurationItems;
    }
    
    static {
        configItems = new LinkedHashMap();
        
        configItems.put("ACTIVE_LINK_EXTERNAL_PROCESS_DIRECTORY", new Integer(Constants.AR_SERVER_INFO_ACTLINK_DIR));
        configItems.put("ACTIVE_LINK_SHELL", new Integer(Constants.AR_SERVER_INFO_ACTLINK_SHELL));
        configItems.put("ADMINISTRATOR_ONLY", new Integer(Constants.AR_SERVER_INFO_ADMIN_ONLY));
        configItems.put("ADMIN_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_ADMIN_TCP_PORT));
        configItems.put("ALERT_AR_SYSTEM_SOURCE_TAG", new Integer(Constants.AR_SERVER_INFO_ALERT_SOURCE_AR));
        configItems.put("ALERT_FLASHBOARD_SOURCE_TAG", new Integer(Constants.AR_SERVER_INFO_ALERT_SOURCE_FB));
        configItems.put("ALERT_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_ALERT_LOG_FILE));
        configItems.put("ALERT_OUTBOUND_PORT", new Integer(Constants.AR_SERVER_INFO_ALERT_OUTBOUND_PORT));
        configItems.put("ALERT_SCHEMA", new Integer(Constants.AR_SERVER_INFO_ALERT_SCHEMA));
        configItems.put("ALLOW_GUESTS", new Integer(Constants.AR_SERVER_INFO_ALLOW_GUESTS));
        configItems.put("ALLOW_UNQUALIFIED_QUERIES", new Integer(Constants.AR_SERVER_INFO_UNQUAL_QUERIES));
        configItems.put("API_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_API_LOG_FILE));
        configItems.put("APPLICATION_COMMANDS_PENDING_SCHEMA", new Integer(Constants.AR_SERVER_INFO_APPL_PENDING));
        configItems.put("APPLICATION_DEFINITION_CHECK_INTERVAL", new Integer(Constants.AR_SERVER_INFO_AP_DEFN_CHECK));
        configItems.put("APPLICATION_LICENSE_AUDIT_LINE", new Integer(Constants.AR_SERVER_INFO_APPLICATION_AUDIT));
        configItems.put("APPLICATION_SERVICE_PASSWORD", new Integer(Constants.AR_SERVER_INFO_APP_SERVICE_PASSWD));
        configItems.put("APPROVAL_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_AP_LOG_FILE));
        configItems.put("APPROVAL_SERVER_RPC_NUMBER", new Integer(Constants.AR_SERVER_INFO_AP_RPC_SOCKET));
        configItems.put("CACHE_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_CACHE_LOG_FILE));
        configItems.put("CACHE_SEG_SIZE", new Integer(Constants.AR_SERVER_INFO_CACHE_SEG_SIZE));
        configItems.put("CACHE_UTILITIES", new Integer(Constants.AR_SERVER_INFO_USER_CACHE_UTILS));
        configItems.put("CANCEL_USER_QUERY", new Integer(Constants.AR_SERVER_INFO_CANCEL_QUERY));
        configItems.put("CASE_SENSITIVE", new Integer(Constants.AR_SERVER_INFO_CASE_SENSITIVE));
        configItems.put("CHECK_ALERT_USERS", new Integer(Constants.AR_SERVER_INFO_CHECK_ALERT_USERS));
        configItems.put("CLUSTERED_INDEX", new Integer(Constants.AR_SERVER_INFO_CLUSTERED_INDEX));
        configItems.put("CROSS_REFERENCE_PASSWORDS", new Integer(Constants.AR_SERVER_INFO_XREF_PASSWORDS));
        configItems.put("CURRENCY_RATIO_CLIENT_REFRESH_INTERVAL", new Integer(Constants.AR_SERVER_INFO_CURRENCY_INTERVAL));
        configItems.put("DATABASE_CONFIGURATION", new Integer(Constants.AR_SERVER_INFO_DBCONF));
        configItems.put("DATABASE_HOME_DIRECTORY", new Integer(Constants.AR_SERVER_INFO_DBHOME_DIR));
        configItems.put("DATABASE_NAME", new Integer(Constants.AR_SERVER_INFO_DB_NAME));
        //AR_SERVER_INFO_DB_PASSWORD blows up for some reason giving error code 218
        //configItems.put("DATABASE_PASSWORD", new Integer(Constants.AR_SERVER_INFO_DB_PASSWORD));
        configItems.put("DATABASE_TYPE", new Integer(Constants.AR_SERVER_INFO_DB_TYPE));
        configItems.put("DATABASE_USER", new Integer(Constants.AR_SERVER_INFO_DB_USER));
        configItems.put("DATABASE_VERSION", new Integer(Constants.AR_SERVER_INFO_DB_VERSION));
        configItems.put("DEBUG_GROUPID", new Integer(Constants.AR_SERVER_INFO_DEBUG_GROUPID));
        configItems.put("DEBUG_MODE", new Integer(Constants.AR_SERVER_INFO_DEBUG_MODE));
        configItems.put("DEFAULT_ALLOWED_CURRENCIES", new Integer(Constants.AR_SERVER_INFO_DFLT_ALLOW_CURRENCIES));
        configItems.put("DEFAULT_FUNCTIONAL_CURRENCIES", new Integer(Constants.AR_SERVER_INFO_DFLT_FUNC_CURRENCIES));
        configItems.put("DEFAULT_ORDER_BY", new Integer(Constants.AR_SERVER_INFO_DEFAULT_ORDER_BY));
        configItems.put("DEFAULT_WEB_PATH", new Integer(Constants.AR_SERVER_INFO_DEFAULT_WEB_PATH));
        configItems.put("DELAYED_CACHE", new Integer(Constants.AR_SERVER_INFO_DELAYED_CACHE));
        configItems.put("DISABLED_CLIENT", new Integer(Constants.AR_SERVER_INFO_DISABLED_CLIENT));
        configItems.put("DISABLE_ADMIN_OPERATIONS", new Integer(Constants.AR_SERVER_INFO_DISABLE_ADMIN_OPERATIONS));
        configItems.put("DISABLE_ALERTS", new Integer(Constants.AR_SERVER_INFO_DISABLE_ALERTS));
        configItems.put("DISABLE_ARCHIVE", new Integer(Constants.AR_SERVER_INFO_DISABLE_ARCHIVE));
        configItems.put("DISABLE_ESCALATIONS", new Integer(Constants.AR_SERVER_INFO_DISABLE_ESCALATIONS));
        configItems.put("DISABLE_FTS_INDEXER", new Integer(Constants.AR_SERVER_INFO_DISABLE_FTS_INDEXER));
        configItems.put("DISTRIBUTED_MAPPING_DEFINITION_SCHEMA", new Integer(Constants.AR_SERVER_INFO_DS_MAPPING));
        configItems.put("DISTRIBUTED_MAPPING_POOL_SCHEMA", new Integer(Constants.AR_SERVER_INFO_DS_POOL));
        configItems.put("DISTRIBUTED_PENDING_OPERATION_LIST_SCHEMA", new Integer(Constants.AR_SERVER_INFO_DS_PENDING));
        configItems.put("DISTRIBUTED_RPC_NUMBER", new Integer(Constants.AR_SERVER_INFO_DS_RPC_SOCKET));
        configItems.put("DISTRIBUTED_SERVER_LICENSE", new Integer(Constants.AR_SERVER_INFO_DS_SVR_LICENSE));
        configItems.put("DISTRIBUTED_SERVER_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_DS_LOG_FILE));
        configItems.put("DSO_DESTINATION_PORT", new Integer(Constants.AR_SERVER_INFO_DSO_DEST_PORT));
        configItems.put("DSO_MERGE_STYLE", new Integer(Constants.AR_SERVER_INFO_DSO_MERGE_STYLE));
        configItems.put("DSO_PLACEHOLDER_MODE", new Integer(Constants.AR_SERVER_INFO_DSO_PLACEHOLDER_MODE));
        configItems.put("DSO_POLLING_INTERVAL", new Integer(Constants.AR_SERVER_INFO_DSO_POLLING_INTERVAL));
        configItems.put("DSO_SOURCE_SERVER", new Integer(Constants.AR_SERVER_INFO_DSO_SOURCE_SERVER));
        configItems.put("DSO_TARGET_CONNECTION", new Integer(Constants.AR_SERVER_INFO_DSO_TARGET_CONNECTION));
        configItems.put("DSO_TARGET_PASSWORD", new Integer(Constants.AR_SERVER_INFO_DSO_TARGET_PASSWD));
        configItems.put("DSO_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_DSO_TIMEOUT_NORMAL));
        configItems.put("DSO_USER_PASSWORD", new Integer(Constants.AR_SERVER_INFO_DSO_USER_PASSWD));
        configItems.put("EMAIL_FROM", new Integer(Constants.AR_SERVER_INFO_EMAIL_FROM));
        configItems.put("EMAIL_LINE_LENGTH", new Integer(Constants.AR_SERVER_INFO_EMAIL_LINE_LEN));
        configItems.put("EMAIL_SYSTEM", new Integer(Constants.AR_SERVER_INFO_EMAIL_SYSTEM));
        configItems.put("EMAIL_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_EMAIL_TIMEOUT));
        configItems.put("EMBEDDED_SQL", new Integer(Constants.AR_SERVER_INFO_EMBEDDED_SQL));
        configItems.put("ENCRYPTION_DATA_ALGORITHM", new Integer(Constants.AR_SERVER_INFO_ENC_DATA_ENCR_ALG));
        configItems.put("ENCRYPTION_DATA_KEY_EXPIRATION", new Integer(Constants.AR_SERVER_INFO_ENC_DATA_KEY_EXP));
        configItems.put("ENCRYPTION_PUBLIC_ALGORITHM", new Integer(Constants.AR_SERVER_INFO_ENC_PUB_KEY_ALG));
        configItems.put("ENCRYPTION_PUBLIC_KEY EXPIRATION", new Integer(Constants.AR_SERVER_INFO_ENC_PUB_KEY_EXP));
        configItems.put("ENCRYPTION_PUBLIC_KEY", new Integer(Constants.AR_SERVER_INFO_ENC_PUB_KEY));
        configItems.put("ENCRYPTION_SECURITY_POLICY", new Integer(Constants.AR_SERVER_INFO_ENC_SEC_POLICY));
        configItems.put("ENCRYPTION_SESSION_HASH_ENTRIES", new Integer(Constants.AR_SERVER_INFO_ENC_SESS_H_ENTRIES));
        configItems.put("ENCRYPT_AL_SQL", new Integer(Constants.AR_SERVER_INFO_ENCRYPT_AL_SQL));
        configItems.put("ESCALATION_DAEMON", new Integer(Constants.AR_SERVER_INFO_ESCL_DAEMON));
        configItems.put("ESCALATION_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_ESCALATION_LOG_FILE));
        configItems.put("ESCALATION_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_ESCL_TCP_PORT));
        configItems.put("EXPORT_VERSION_NUMBER", new Integer(Constants.AR_SERVER_INFO_EXPORT_VERSION));
        configItems.put("EXTERNAL_AUTHENTICATION_RPC_NUMBER", new Integer(Constants.AR_SERVER_INFO_EA_RPC_SOCKET));
        configItems.put("EXTERNAL_AUTHENTICATION_RPC_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_EA_RPC_TIMEOUT));
        configItems.put("EXTERNAL_AUTHENTICATION_SYNCHRONIZATION_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_EA_SYNC_TIMEOUT));
        configItems.put("FAST_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_FAST_TCP_PORT));
        configItems.put("FILTER_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_FILTER_LOG_FILE));
        configItems.put("FILTER_MAX_STACK", new Integer(Constants.AR_SERVER_INFO_FILT_MAX_STACK));
        configItems.put("FILTER_MAX_TOTAL", new Integer(Constants.AR_SERVER_INFO_FILT_MAX_TOTAL));
        configItems.put("FILTER_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_FILTER_API_RPC_TIMEOUT));
        configItems.put("FIXED_LICENSES", new Integer(Constants.AR_SERVER_INFO_FIXED_LICENSE));
        configItems.put("FLASH_DAEMON", new Integer(Constants.AR_SERVER_INFO_FLASH_DAEMON));
        configItems.put("FLASH_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_FLASH_TCP_PORT));
        configItems.put("FLOATING_LICENSES", new Integer(Constants.AR_SERVER_INFO_FLOAT_LICENSE));
        configItems.put("FLOATING_LICENSE_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_FLOAT_TIMEOUT));
        configItems.put("FLUSH_LOG_LINES", new Integer(Constants.AR_SERVER_INFO_FLUSH_LOG_LINES));
        configItems.put("FORK_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_ARFORK_LOG_FILE));
        configItems.put("FULL_HOSTNAME", new Integer(Constants.AR_SERVER_INFO_FULL_HOSTNAME));
        configItems.put("FULL_TEXT_SEARCH_FIXED_LICENSES", new Integer(Constants.AR_SERVER_INFO_FTEXT_FIXED));
        configItems.put("FULL_TEXT_SEARCH_FLOATING_LICENSES", new Integer(Constants.AR_SERVER_INFO_FTEXT_FLOAT));
        configItems.put("FULL_TEXT_SEARCH_FLOATING_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_FTEXT_TIMEOUT));
        configItems.put("HARDWARE", new Integer(Constants.AR_SERVER_INFO_HARDWARE));
        configItems.put("HOMEPAGE_FORM", new Integer(Constants.AR_SERVER_INFO_HOMEPAGE_FORM));
        configItems.put("HOSTNAME", new Integer(Constants.AR_SERVER_INFO_HOSTNAME));
        configItems.put("INFORMIX_DATABASE_NAME", new Integer(Constants.AR_SERVER_INFO_INFORMIX_DBN));
        configItems.put("INGRES_VNODE", new Integer(Constants.AR_SERVER_INFO_INGRES_VNODE));
        configItems.put("INITIAL_FORM", new Integer(Constants.AR_SERVER_INFO_INIT_FORM));
        configItems.put("LAST_CACHE_CHANGE", new Integer(Constants.AR_SERVER_INFO_U_CACHE_CHANGE));
        configItems.put("LAST_GROUP_CACHE_CHANGE", new Integer(Constants.AR_SERVER_INFO_G_CACHE_CHANGE));
        configItems.put("LAST_STRUCT_CHANGE", new Integer(Constants.AR_SERVER_INFO_STRUCT_CHANGE));
        configItems.put("LIST_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_LIST_TCP_PORT));
        configItems.put("LOCALIZED_SERVER_OPTION", new Integer(Constants.AR_SERVER_INFO_LOCALIZED_SERVER));
        configItems.put("LOGFILE_APPEND", new Integer(Constants.AR_SERVER_INFO_LOGFILE_APPEND));
        configItems.put("MAX_AUDIT_LOG_FILE_SIZE", new Integer(Constants.AR_SERVER_INFO_MAX_AUDIT_LOG_FILE_SIZE));
        configItems.put("MAX_F_DAEMONS", new Integer(Constants.AR_SERVER_INFO_MAX_F_DAEMONS));
        configItems.put("MAX_LOG_FILE_SIZE", new Integer(Constants.AR_SERVER_INFO_MAX_LOG_FILE_SIZE));
        configItems.put("MAX_L_DAEMONS", new Integer(Constants.AR_SERVER_INFO_MAX_L_DAEMONS));
        configItems.put("MAX_RETURNED_QUERY_ENTRIES", new Integer(Constants.AR_SERVER_INFO_MAX_ENTRIES));
        configItems.put("MAX_SCHEMAS", new Integer(Constants.AR_SERVER_INFO_MAX_SCHEMAS));
        configItems.put("MESSAGE_CATALOG_SCHEMA", new Integer(Constants.AR_SERVER_INFO_MESSAGE_CAT_SCHEMA));
        configItems.put("MID_TIER_PASSWORD", new Integer(Constants.AR_SERVER_INFO_MID_TIER_PASSWD));
        configItems.put("MINIMUM_API_VERSION", new Integer(Constants.AR_SERVER_INFO_MINIMUM_API_VER));
        configItems.put("MULTIPLE_ASSIGN_GROUPS", new Integer(Constants.AR_SERVER_INFO_MULT_ASSIGN_GROUPS));
        configItems.put("MULTI_SERVER", new Integer(Constants.AR_SERVER_INFO_MULTI_SERVER));
        configItems.put("NOTIFYER_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_NFY_TCP_PORT));
        configItems.put("NOTIFYER_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_NOTIF_SEND_TIMEOUT));
        configItems.put("OPERATING_SYSTEM", new Integer(Constants.AR_SERVER_INFO_OS));
        configItems.put("ORACLE_QUERY_ON_CLOB", new Integer(Constants.AR_SERVER_INFO_ORACLE_QUERY_ON_CLOB));
        configItems.put("ORACLE_SYSTEM_ID", new Integer(Constants.AR_SERVER_INFO_ORACLE_SID));
        configItems.put("ORACLE_TWO_TASK", new Integer(Constants.AR_SERVER_INFO_ORACLE_TWO_T));
        configItems.put("PER_THREAD_LOGS", new Integer(Constants.AR_SERVER_INFO_PER_THREAD_LOGS));
        configItems.put("PLUGIN_ALIASES", new Integer(Constants.AR_SERVER_INFO_PLUGIN_ALIAS));
        configItems.put("PLUGIN_CLIENT_PASSWORD", new Integer(Constants.AR_SERVER_INFO_PLUGIN_TARGET_PASSWD));
        configItems.put("PLUGIN_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_PLUGIN_LOG_FILE));
        configItems.put("PLUGIN_SERVER_PASSWORD", new Integer(Constants.AR_SERVER_INFO_PLUGIN_PASSWD));
        configItems.put("PREFERENCE_PRIORITY", new Integer(Constants.AR_SERVER_INFO_PREFERENCE_PRIORITY));
        configItems.put("PRIVATE_SERVER_RPC_NUMBER", new Integer(Constants.AR_SERVER_INFO_PS_RPC_SOCKET));
        configItems.put("RECORDED_SERVER_EVENTS", new Integer(Constants.AR_SERVER_INFO_SVR_EVENT_LIST));
        configItems.put("REGISTER_WITH_PORTMAPPER", new Integer(Constants.AR_SERVER_INFO_REGISTER_PORTMAPPER));
        configItems.put("RESERVED_ADMIN_LICENSES", new Integer(Constants.AR_SERVER_INFO_RESERV1_C));
        configItems.put("RESERVED_FIXED_LICENSES", new Integer(Constants.AR_SERVER_INFO_RESERV1_A));
        configItems.put("RESERVED_FLOATING_LICENSES", new Integer(Constants.AR_SERVER_INFO_RESERV1_B));
        configItems.put("SAVE_LOGIN", new Integer(Constants.AR_SERVER_INFO_SAVE_LOGIN));
        configItems.put("SERVERGROUP_INTERVAL", new Integer(Constants.AR_SERVER_INFO_SERVERGROUP_INTERVAL));
        configItems.put("SERVERGROUP_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_SERVERGROUP_LOG_FILE));
        configItems.put("SERVERGROUP_MEMBER", new Integer(Constants.AR_SERVER_INFO_SERVERGROUP_MEMBER));
        configItems.put("SERVERGROUP_NAME", new Integer(Constants.AR_SERVER_INFO_SERVERGROUP_NAME));
        configItems.put("SERVER_DATA_DIRECTORY", new Integer(Constants.AR_SERVER_INFO_SERVER_DIR));
        configItems.put("SERVER_GROUP_ADMIN_SERVER_NAME", new Integer(Constants.AR_SERVER_INFO_SG_ADMIN_SERVER_NAME));
        configItems.put("SERVER_GROUP_EMAIL_STATE", new Integer(Constants.AR_SERVER_INFO_SG_EMAIL_STATE));
        configItems.put("SERVER_GROUP_FLASHBOARD_STATE", new Integer(Constants.AR_SERVER_INFO_SG_FLASHBOARDS_STATE));
        configItems.put("SERVER_GROUP_SERVER_PORT", new Integer(Constants.AR_SERVER_INFO_SG_ADMIN_SERVER_PORT));
        configItems.put("SERVER_ID", new Integer(Constants.AR_SERVER_INFO_SERVER_IDENT));
        configItems.put("SERVER_LANGUAGE", new Integer(Constants.AR_SERVER_INFO_SERVER_LANG));
        configItems.put("SERVER_LICENSE", new Integer(Constants.AR_SERVER_INFO_SERVER_LICENSE));
        configItems.put("SERVER_LICENSE_ID", new Integer(Constants.AR_SERVER_INFO_REM_SERV_ID));
        configItems.put("SERVER_NAME", new Integer(Constants.AR_SERVER_INFO_SERVER_NAME));
        configItems.put("SERVER_SECURITY_CACHE", new Integer(Constants.AR_SERVER_INFO_SVR_SEC_CACHE));
        configItems.put("SERVER_STATS_RECORDING_INTERVAL", new Integer(Constants.AR_SERVER_INFO_SVR_STATS_REC_INTERVAL));
        configItems.put("SERVER_STATS_RECORDING_MODE", new Integer(Constants.AR_SERVER_INFO_SVR_STATS_REC_MODE));
        configItems.put("SERVER_TIME", new Integer(Constants.AR_SERVER_INFO_SERVER_TIME));
        configItems.put("SERVER_VERSION", new Integer(Constants.AR_SERVER_INFO_VERSION));
        configItems.put("SET_FIELDS_PROCESS_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_SET_PROC_TIME));
        configItems.put("SHARED_CACHE", new Integer(Constants.AR_SERVER_INFO_SHARED_CACHE));
        configItems.put("SHARED_MEMORY", new Integer(Constants.AR_SERVER_INFO_SHARED_MEM));
        configItems.put("SOURCE_CODE_CONTROL_COMMENT_CHECKIN", new Integer(Constants.AR_SERVER_INFO_SCC_COMMENT_CHECKIN));
        configItems.put("SOURCE_CODE_CONTROL_COMMENT_CHECKOUT", new Integer(Constants.AR_SERVER_INFO_SCC_COMMENT_CHECKOUT));
        configItems.put("SOURCE_CODE_CONTROL_ENABLED", new Integer(Constants.AR_SERVER_INFO_SCC_ENABLED));
        configItems.put("SOURCE_CODE_CONTROL_INTEGRATION_MODE", new Integer(Constants.AR_SERVER_INFO_SCC_INTEGRATION_MODE));
        configItems.put("SOURCE_CODE_CONTROL_PROVIDER_NAME", new Integer(Constants.AR_SERVER_INFO_SCC_PROVIDER_NAME));
        configItems.put("SOURCE_CODE_CONTROL_TARGET_DIRECTORY", new Integer(Constants.AR_SERVER_INFO_SCC_TARGET_DIR));
        configItems.put("SQL_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_SQL_LOG_FILE));
        configItems.put("SUBMITTER_MODE", new Integer(Constants.AR_SERVER_INFO_SUBMITTER_MODE));
        configItems.put("SUPPRESSED_WARNING_MESSAGES", new Integer(Constants.AR_SERVER_INFO_SUPPRESS_WARN));
        configItems.put("SYBASE_CHARSET", new Integer(Constants.AR_SERVER_INFO_SYBASE_CHARSET));
        configItems.put("SYBASE_SERVER", new Integer(Constants.AR_SERVER_INFO_SYBASE_SERV));
        configItems.put("TCD_TCP_PORT", new Integer(Constants.AR_SERVER_INFO_TCD_TCP_PORT));
        configItems.put("THREAD_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_THREAD_LOG_FILE));
        configItems.put("TWO_DIGIT_YEAR_CUTOFF", new Integer(Constants.AR_SERVER_INFO_TWO_DIGIT_YEAR_CUTOFF));
        configItems.put("USER_INFO_LISTS", new Integer(Constants.AR_SERVER_INFO_USER_INFO_LISTS));
        configItems.put("USER_INSTANCE_TIMEOUT", new Integer(Constants.AR_SERVER_INFO_USER_INST_TIMEOUT));
        configItems.put("USER_LOG_FILE", new Integer(Constants.AR_SERVER_INFO_USER_LOG_FILE));
        configItems.put("USE_ETC_PASSWD", new Integer(Constants.AR_SERVER_INFO_USE_ETC_PASSWD));
    }
}

