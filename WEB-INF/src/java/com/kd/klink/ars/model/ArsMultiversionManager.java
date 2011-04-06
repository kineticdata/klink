package com.kd.klink.ars.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.remedy.arsys.api.*;

import org.apache.log4j.*;

/**
 *
 */
public class ArsMultiversionManager {
    private static final Logger logger = Logger.getLogger(ArsEntry.class.getName());
    
    public static final String VERSION_50 = "VERSION_50";
    public static final String VERSION_60 = "VERSION_60";
    public static final String VERSION_63 = "VERSION_63";
    public static final String VERSION_70 = "VERSION_70";

    private ArsMultiversionManager() {}
    
    /** 
     * Checks if a class exists in the current java environment.
     */
    private static boolean classExists(String classname) {
        try {
            Class.forName(classname);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /** 
     * Determines the version of the API that is currently loaded.  Currently
     * supported versions include: 
     *   VERSION_50
     *   VERSION_60
     *   VERSION_63
     *   VERSION_70
     */
    public static String getApiVersion() {
        // Set the default version
        String currentVersion = VERSION_50;

        // Ensure the Api is accessible
        if (!classExists("com.remedy.arsys.api.Util")) {
            throw new Error("Missing API Exception.");
        }
        // If com.remedy.arsys.api.internal.Constants exists this is 70
        else if (classExists("com.remedy.arsys.api.internal.Constants")) {
            currentVersion = VERSION_70;
        }
        // If com.remedy.arsys.api.internal.InternalConstants exists this is 63
        else if (classExists("com.remedy.arsys.api.internal.InternalConstants")) {
            currentVersion = VERSION_63;
        }
        // If com.remedy.arsys.api.ArchiveInfo exists and Constants/InternalConstants doesnt this is 60 
        else if (classExists("com.remedy.arsys.api.ArchiveInfo")) {
            currentVersion = VERSION_60;
        }
        
        return currentVersion;
    }
    
    public static NameID[] getNameIDs(EnumLimitInfo enumLimitInfo) {
        NameID[] names = new NameID[0];
        Class enumLimitClass = enumLimitInfo.getClass();

        try {
            // If the imported Ars API is for version 7
            if (ArsMultiversionManager.getApiVersion() == ArsMultiversionManager.VERSION_70) {
                Method getEnumListStyleMethod = enumLimitClass.getDeclaredMethod("getEnumListStyle", new Class[0]);
                Field regularField = enumLimitClass.getField("REGULAR");

                if (((Integer)getEnumListStyleMethod.invoke(enumLimitInfo, new Object[0])).intValue() == regularField.getInt(null)) {
                    Method getNames = enumLimitClass.getMethod("getEnumRegularLimit", null); 
                    names = (NameID[])getNames.invoke(enumLimitInfo, null);
                } else {
                    Method getEnumItems = enumLimitClass.getMethod("getEnumCustomLimit", null);
                    Object[] enumItems = (Object[])getEnumItems.invoke(enumLimitInfo, null);
                    names = new NameID[enumItems.length];

                    Class enumItemClass = Class.forName("com.remedy.arsys.api.EnumItem");
                    Method getEnumItemName = enumItemClass.getMethod("getEnumItemName", null);
                    for (int i=0;i<names.length;i++) {
                        names[i] = (NameID)getEnumItemName.invoke(enumItems[i], null);
                    }
                }
            }
            // If the imported Ars API is for a version older than 7
            else {
                Method getNames = enumLimitClass.getMethod("getEnumLimit", null);
                names = (NameID[])getNames.invoke(enumLimitInfo, null);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
        }

        return names;
    }
}