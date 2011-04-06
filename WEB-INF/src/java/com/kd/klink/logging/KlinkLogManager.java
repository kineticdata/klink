package com.kd.klink.logging;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

/**
 * This class automates the manipulation of logging across the Klink web
 * application.  This servlet should be loaded upon initialization of the Klink
 * web application even if logging is disabled.
 *
 * Initialization will check for two servlet initialization parameters, 
 * log4j_configuration_file and profile_name.  If profile_name is included it
 * will attempt to load one of the two default profiles.  If 
 * log4j_configuration_file is specified it will attempt to load the logging
 * configuration from the file specified (overwriting any conflicting 
 * configurations made by loading a profile).
 */
public class KlinkLogManager extends HttpServlet {
    // Define the two layouts used for the preconfigured logging profiles
    private static final PatternLayout debugLayout = new PatternLayout("%d{yyyy-MM-dd hh:mm:ss} %-5p %-30c{1}  %m%n");
    private static final PatternLayout stableLayout = new PatternLayout("%d{yyyy-MM-dd hh:mm:ss} %m%n");
    
    // Create the LogConfig class to manage the manipulation of logging
    private static LogConfig logConfig = new LogConfig();
    
    /** Returns the object used to manage the manipulation of logging. */
    public static LogConfig getLogConfig() { return logConfig; }
    
    /**
     * Initializes the servlet and the logging configurations used accross the
     * Klink web application.  This will check for two servlet initialization
     * parameters, log4j_configuration_file and profile_name.  If profile_name
     * is included it will attempt to load one of the two default profiles.  If
     * log4j_configuration_file is specified it will attempt to load the logging
     * configuration from the file specified (overwriting any conflicting 
     * configurations made by loading a profile).
     *
     * @throws ServletException If a configuration file was specified but an 
     *                          error occured attempting to access it or if
     *                          a preconfigured profile is used but there was a
     *                          problem accessing the default logfile.
     */
    public void init(ServletConfig config) throws ServletException {
        // Retrieve the two expected initialization parameters
        String configFilePath = config.getInitParameter("log4j_configuration_file");
        String profileName = config.getInitParameter("profile_name");
        
        // If a profile name was specified
        if (profileName != null) {
            // Build up the logpath
            String basePath = config.getServletContext().getRealPath("");
            String webappPath = "WEB-INF" + File.separator + "logs" + File.separator + "klink.log";
            String logPath =  basePath + File.separator + webappPath;
            config.getServletContext().log("Klink log: " + logPath);
            
            // Try to load one of the two default profiles
            try {
                // If the debug default profile was specified
                if (profileName.equalsIgnoreCase("DEBUG")) {
                    RollingFileAppender fileAppender = new RollingFileAppender(debugLayout, logPath, true);
                    fileAppender.setMaxBackupIndex(10);
                    fileAppender.setMaxFileSize("5MB");

                    LogManager.getLogger("com.kd.klink").setLevel(Level.DEBUG);
                    LogManager.getLogger("com.kd.klink").addAppender(fileAppender);
                } else {
                    RollingFileAppender fileAppender = new RollingFileAppender(debugLayout, logPath, true);
                    fileAppender.setMaxBackupIndex(5);
                    fileAppender.setMaxFileSize("1MB");

                    LogManager.getLogger("com.kd.klink").setLevel(Level.ERROR);
                    LogManager.getLogger("com.kd.klink").addAppender(fileAppender);
                }
            }
            // If there was a problem accessing/locking the logfile
            catch (IOException e) {
                throw new ServletException("There was a problem initializing logging, " + e.toString());
            }
        }
        
        // If a configuration file was specified
        if (configFilePath != null) {
            // Try to load the file and initialize logging
            try {
                logConfig = new LogConfig(configFilePath);
                KlinkLogManager.loadDefaultConfigFile();
            }
            // If there was a problem accessing the log configuration file
            catch (IOException e) {
                throw new ServletException("There was a problem initializing logging, " + e.toString());
            }
        }
        
        // Initialize the logger
        Logger logger = Logger.getLogger(KlinkLogManager.class.getName());
        logger.info("Logging started...");
    }
    
    /**
     * Shuts down the servlet.  This includes safely closing and removing all
     * appenders in all categories, including the root logger.
     */
    public void destroy() { LogManager.shutdown(); }
    
    /**
     * Attempts to load a new log4j configuration from the specified file.
     */
    public static LogConfig loadConfigFile(String configFilePath) throws IOException {
        logConfig.loadNewConfigFile(configFilePath);
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(logConfig.getProperties());
        return logConfig;
    }
    
    /**
     * Reloads the default configuration file (the file specified by the
     * initialization parameter log4j_configuration_file).  If there was no
     * default configuration file specified it will remove all current logging
     * configurations.
     */
    public static LogConfig loadDefaultConfigFile() throws IOException {
        logConfig.loadDefaultConfigFile();
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(logConfig.getProperties());
        return logConfig;
    }
    
    /**
     * Loads the collection of logging configurations held in the provided
     * properties variable.  This will overwrite any previous properties that
     * have the same name as the properties provided.
     */
    public static LogConfig loadProperties(Properties properties) {
        Properties currentProperties = logConfig.getProperties();
        Iterator iterator = properties.keySet().iterator();
        while(iterator.hasNext()) {
            String key = (String)iterator.next();
            currentProperties.put(key, properties.get(key));
        }
        LogManager.resetConfiguration();
        logConfig.loadProperties(currentProperties);
        PropertyConfigurator.configure(logConfig.getProperties());
        return logConfig;
    }
}
