package com.kd.klink.logging;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import com.kd.klink.model.*;

/**
 * Provides a wrapper beanish object for maintaining current logging 
 * configuration information.
 */
public class LogConfig extends ModelObject {
    // Specify a logger
    private static final Logger logger = Logger.getLogger(LogConfig.class.getName());
    
    // Declare the private variables
    private String defaultConfigFile = null;
    private String currentConfigFile = null;
    private LogPropertySource currentPropertySource = null;
    private TreeMap sortedProperties = new TreeMap();
    
    /** Default Constructor. */
    public LogConfig() {}
    /** Create a new LogConfig object and set the default configuraiton file. */
    public LogConfig(String defaultConfigFile) { this.defaultConfigFile = defaultConfigFile; }
    
    /**
     * Remove all of the current logging properties and set the current property
     * source to LogPropertySource.CUSTOM.
     */
    public void clearProperties() {
        this.currentPropertySource = LogPropertySource.CUSTOM;
        this.sortedProperties.clear(); 
    }
    
    /** Return a boolean value representing the presence of a configuration property. */
    public boolean containsProperty(String key) { return this.sortedProperties.containsKey(key); }
    
    /** Return the path to the current configuration file. */
    public String getCurrentConfigFile() { return this.currentConfigFile; }
    
    /** Return the current property source. */
    public LogPropertySource getCurrentPropertySource() { return this.currentPropertySource; }
    
    /** Return the path to the default configuration file. */
    public String getDefaultConfigFile() { return this.defaultConfigFile; }
    
    /**
     * Return a java Properties object containing the key/value pairs used in
     * the current logging configuration.
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        Iterator keys = sortedProperties.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            properties.put(key, sortedProperties.get(key));
        }
        return properties;
    }
    
    /** Return the value for a current logging property (or null if it doesn't exist. */
    public String getProperty(String key) { return (String)this.sortedProperties.get(key); }
    
    /**
     * Set the current property source to LogPropertySource.DEFAULT_CONFIG_FILE,
     * clear the current properties, and load them from the default 
     * configuration file.
     */
    public void loadDefaultConfigFile() throws IOException {
        this.sortedProperties.clear();
        this.setCurrentPropertySource(LogPropertySource.DEFAULT_CONFIG_FILE);
        loadConfigFile(this.getDefaultConfigFile());
    }

    /**
     * Clear the current configuration and load new logging configuration
     * properties from the profided configuration file.  This will set the
     * current property source (CUSTOM if the parameter is null, 
     * DEFAULT_CONFIG_FILE if the parameter matches the current default
     * configuration file, or ALTERNATIVE_CONFIG_FILE if it doesn't.
     */
    public void loadNewConfigFile(String file) throws IOException {
        if (!this.getCurrentConfigFile().equals(file)) {
            this.sortedProperties.clear();
            if (file == null) {
                this.setCurrentPropertySource(LogPropertySource.CUSTOM);
            } else if (file.equals(this.getDefaultConfigFile())) {
                this.setCurrentPropertySource(LogPropertySource.DEFAULT_CONFIG_FILE);
            } else {
                this.setCurrentPropertySource(LogPropertySource.ALTERNATIVE_CONFIG_FILE);
            }
            loadConfigFile(this.getCurrentConfigFile());
        }
    }
    
    /**
     * Set the current property source to LogPropertySource.CUSTOM and manually
     * load logging configuration properties from the Properties parameter.
     * This does NOT clear the current properties, but will overwrite any 
     * properties with the same name.
     */
    public void loadProperties(Properties properties) {
        this.setCurrentPropertySource(LogPropertySource.CUSTOM);

        this.updateProperties(properties);
    }

    /**
     * Set the default configuration file and intelligently modifies the current
     * property source.
     */
    public void setDefaultConfigFile(String configFile) { 
        // If this is a new file
        if (!this.defaultConfigFile.equals(configFile)) {
            // If we were currently logging according to the default config file and 
            if (this.currentPropertySource == LogPropertySource.DEFAULT_CONFIG_FILE) {
                // The previous default config file is not longer the same so 
                this.currentPropertySource = LogPropertySource.ALTERNATIVE_CONFIG_FILE;
            }
            // If the current configuration file is the same as our new default config file
            else if (this.currentPropertySource == LogPropertySource.ALTERNATIVE_CONFIG_FILE &&
                     this.currentConfigFile.equals(configFile)) {
                // Set the log property source to be the default file
                this.currentPropertySource = LogPropertySource.DEFAULT_CONFIG_FILE;
            }
            
            // Set the defaut configuration file
            this.defaultConfigFile = configFile; 
        }
    }

    /**
     * Manually sets a single logging configuration property and changes the
     * log property source to LogPropertySource.CUSTOM.
     */
    public void setProperty(String key, String value) {
        this.setCurrentPropertySource(LogPropertySource.CUSTOM);
        
        this.currentPropertySource = LogPropertySource.CUSTOM;
        this.sortedProperties.put(key, value);
    }

    
    /**
     * Helper method for loading configuration from a file.  If the provided
     * path is null all logging properties will be cleared.
     *
     * throws   IOException When there is a problem reading from the file 
     *                      designated by the config file path.
     */
    private void loadConfigFile(String configFilePath) throws IOException {
        logger.debug("Loading configuration file: " + configFilePath);
        
        // If a default config file was specified
        if (configFilePath != null) {
            // Load the properties from the file
            Properties properties = new Properties();
            properties.load(new FileInputStream(configFilePath));

            // Add the properties to our sorted Properties map
            this.updateProperties(properties);
        } else {
            this.sortedProperties.clear();
        }
    }
    
    /** 
     * Helper method used to automatically set the property source based on
     * what other requests were made.  LogConfig.clearProperties(), 
     * LogConfig.loadDefaultConfigFile(), LogConfig.loadCurrentConfigFile(), 
     * LogConfig.loadProperties(), and LogConfig.setProperty() all automatically
     * modify the property source.
     */
    private void setCurrentPropertySource(LogPropertySource source) { 
        // Log the source change
        if (source == null) {
            logger.debug("Changing property to a NULL source."); 
        } else if (source == LogPropertySource.ALTERNATIVE_CONFIG_FILE) {
            logger.debug("Changing property source: ALTERNATIVE_CONFIG_FILE"); 
        } else if (source == LogPropertySource.CUSTOM) {
            logger.debug("Changing property source: CUSTOM" ); 
        } else if (source == LogPropertySource.DEFAULT_CONFIG_FILE) {
            logger.debug("Changing property source: DEFAULT_CONFIG_FILE"); 
        }  
        
        // Set the source
        this.currentPropertySource = source; 
    }
    
    /**
     * Helper method for updating the current logging properties based on the
     * provided properties.
     */
    private void updateProperties(Properties properties) {
        logger.debug("Loading " + properties.size() + " properties.");
        
        // Clear our current properties
        this.sortedProperties.clear();
        
        // Verify we arn't being sent null
        if (properties == null) { properties = new Properties(); }
        
        // Add each of the properties to our sortedProperties object
        Iterator propertyKeys = properties.keySet().iterator();
        while(propertyKeys.hasNext()) {
            String key = (String)propertyKeys.next();
            sortedProperties.put(key.toString(), properties.getProperty(key).toString());
        }
    }
    
    /**
     * Return the xml representation of the element.
     *
     * Example: 
     * <LogConfig CurrentPropertySourc="DEFAULT_CONFIG_FILE">
     *    <DefaultConfigFile>C:\Program Files\Klink\config\log4j.conf</DefaultConfigFile>
     *    <Properties>
     *       <Property Key="log4j.logger.com.kd.klink">DEBUG, fileAppender</Property>
     *       <Property Key="log4j.appender.fileAppender">org.apache.log4j.FileAppender</Property>
     *       <Property Key="log4j.appender.fileAppender.File">C:\Program Files\Klink\logs\klink.log</Property>
     *       <Property Key="log4j.appender.fileAppender.layout">org.apache.log4j.PatternLayout</Property>
     *       <Property Key="log4j.appender.fileAppender.layout.ConversionPattern">%p %c - %m%n</Property>
     *    </Properties>
     * </LogConfig>
     */
    public Element generateXmlElement() {
        // Generate the root element
        Element logConfigElement = document.createElement("LogConfig");
        if (this.getCurrentPropertySource() == LogPropertySource.ALTERNATIVE_CONFIG_FILE) {
            logConfigElement.setAttribute("CurrentPropertySource", this.getCurrentConfigFile());
        } else if (this.getCurrentPropertySource() == LogPropertySource.CUSTOM) {
            logConfigElement.setAttribute("CurrentPropertySource", "CUSTOM_PROPERTIES");
        } else if (this.getCurrentPropertySource() == LogPropertySource.DEFAULT_CONFIG_FILE) {
            logConfigElement.setAttribute("CurrentPropertySource", "DEFAULT_CONFIG_FILE");
        }

        // Generate the DefaultConfigFile child element
        if (this.getDefaultConfigFile() != null) {
            Element defaultConfigFileElement = document.createElement("DefaultConfigFile");
            defaultConfigFileElement.appendChild(document.createTextNode(this.getDefaultConfigFile()));
            logConfigElement.appendChild(defaultConfigFileElement);
        }

        // Generate the Properties Child Element
        Element propertiesElement = document.createElement("Properties");
        Iterator iterator = this.sortedProperties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String)iterator.next();
            Element propertyElement = document.createElement("Property");
            propertyElement.setAttribute("Key", key);
            propertyElement.appendChild(document.createTextNode(this.getProperty(key)));
            propertiesElement.appendChild(propertyElement);
        }
        logConfigElement.appendChild(propertiesElement);

        // Return the element
        return logConfigElement;
    }
}
