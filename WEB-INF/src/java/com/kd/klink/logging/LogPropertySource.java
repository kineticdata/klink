package com.kd.klink.logging;

/**
 * This class represents the possible sources for the current logging 
 * configuration.  There are three possible instantiations of LogPropertySource.
 * ALTERNATIVE_CONFIG_FILE represents a logging configuration that was loaded
 * from a file that is different from the file specified via a Servlet parameter
 * to the KlinkLogManager.  CUSTOM represents a logging configuration that was
 * modified by hand using the logconfig framework call.  DEFAULT_CONFIG_FILE
 * represents a logging configuration that was loaded from the default log4j
 * configuration file specified via Servlet parameter to the KlinkLogManager.
 */
public class LogPropertySource {
    public static final LogPropertySource ALTERNATIVE_CONFIG_FILE = new LogPropertySource();
    public static final LogPropertySource CUSTOM = new LogPropertySource();
    public static final LogPropertySource DEFAULT_CONFIG_FILE = new LogPropertySource();

    private LogPropertySource() {}
}
