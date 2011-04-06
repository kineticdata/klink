package com.kd.klink.verification;

import com.remedy.arsys.api.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;

/**
 * A class that provides a set of static methods used to verify the presense of
 * required java libraries.
 */
public class VerificationUtils {
    private static Library[] requiredLibraries = {
                             new Library("commons-beanutils", "org.apache.commons.beanutils.BeanUtils"),
                             new Library("commons-chain", "org.apache.commons.chain.Chain"),
                             new Library("commons-digester", "org.apache.commons.digester.Digester"),
                             new Library("commons-logging", "org.apache.commons.logging.Log"),
                             new Library("commons-validator", "org.apache.commons.validator.Validator"),
                             new Library("klink-commons-model", "com.kd.klink.model.ModelObject"),
                             new Library("log4j", "org.apache.log4j.LogManager"),
                             new Library("servlet-api", "javax.servlet.Servlet"),
                             new Library("struts-core", "org.apache.struts.Globals"),
                             new Library("struts-extra", "org.apache.struts.actions.ActionDispatcher")};
    
    /** 
     * Checks if a class exists in the current java environment.
     */
    public static boolean classExists(String classname) {
        try {
            Class.forName(classname);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Returns a string representing the location of the class definition.
     *
     * throws ClassNotFoundException    If the class was not found in the current
     *                                  environment.
     * throws IOException               If the class is located in a jar and there
     *                                  was a problem opening a connection to 
     *                                  obtain the jar file location.
     */
    public static String getClassLocation(String className) throws ClassNotFoundException, IOException {
        // Try to create an instance of the Class
        Class myClass = Class.forName(className);
        
        // Get the intial location
        URL locationUrl = myClass.getProtectionDomain().getCodeSource().getLocation();
        String location = locationUrl.toString();
        
        // If the location is a jar file
        if(location.startsWith("jar")) {
            locationUrl = ((JarURLConnection)locationUrl.openConnection()).getJarFileURL();
            location = locationUrl.toString();
        }
        // If the location is a class file
        if(location.startsWith("file")) {
            File file = new File(locationUrl.getFile());
            location = file.getAbsolutePath();
        }
        
        // Return the location
        return location;
    }
    
    /**
     * Returns an array of the directories in the java.library.path property.
     * This can be used to verify that the required DLLs are actually within
     * the java.library.path.
     */
    public static String[] getJavaLibraryPath() {
        String[] paths = System.getProperty("java.library.path").split(";");
        return paths;
    }
    
    /**
     * Returns the java version number used to run this code.
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    /** Return the implementation version string from the library. **/
    public static String getLibraryImplementationVersion(String className) {
        String implVersion = null;
        try {
            Class curClass = Class.forName(className);
            implVersion = curClass.getPackage().getImplementationVersion();
        } catch (Exception e) { 
            // Do Nothing
        } 

        return implVersion;
    }
                            
    /** Return the specification version string from the library. **/
    public static String getLibrarySpecificationVersion(String className) {
        String specVersion = null;
        try {
            Class curClass = Class.forName(className);
            specVersion = curClass.getPackage().getSpecificationVersion();
        } catch (Exception e) {
            // Do Nothing
        }
        return specVersion;
    }
    
    /**
     * Returns the operating system used to run this code.
     */
    public static String getOperationSystem() {
        return System.getProperty("os.name");
    }
    
    /**
     * Return an array of Library objects representing the java libraries
     * required by the klink-commons library.
     */
    public static Library[] getRequiredLibraries() {
        return requiredLibraries; 
    }
    
    /**
     * Return the absolute path where the provided library exists.
     *
     * throws   FileNotFoundException   If the library was not found and does
     *                                  not exist in anywhere in the
     *                                  java.library.path.
     */
    public static String getSystemLibraryLocation(String library) throws FileNotFoundException {
        File libraryFile = new File(library);
        if (!libraryFile.isAbsolute()) {
            // Map it to the platform-dependent library name
            String libraryFileName = System.mapLibraryName(library);
            
            // Get a list of paths in our java.library.path
            String[] javaLibraryPath = VerificationUtils.getJavaLibraryPath();
            
            // For each path
            for (int i=0;i<javaLibraryPath.length;i++) {
                // Create a pointer to the full path of where the library file would be
                libraryFile = new File(javaLibraryPath[i] + File.separator + libraryFileName);
                
                // If the file does exist in that path, this is the one that will be used
                if (libraryFile.exists()) {
                    // Return the absolute path
                    return libraryFile.getAbsolutePath();
                }
            }
            
            // Throw an Exception since it was nowhere in the java.library.path
            throw new FileNotFoundException();
        }
        // If the library given in the parameter is an absolute path and doesn't exist
        else if (!libraryFile.exists()) {
            // Throw an exception since we didn't find it
            throw new FileNotFoundException();
        }
        
        return library;
    }
    
    /**
     * Checks for the presence of a system library (.dll or .so file) by attempting
     * to load it.
     *
     * throws UnsatisfiedLinkError  If the system library could not be found.
     */
    public static void loadSystemLibrary(String library) throws UnsatisfiedLinkError {
        File libraryFile = new File(library);
        if (libraryFile.isAbsolute()) {
            System.load(library);
        } else {
            System.loadLibrary(library);
        }
    }
}
