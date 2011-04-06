package com.kd.klink.ars.verification;

import com.kd.klink.verification.VerificationUtils;

/**
 * A class that provides a set of static methods used to verify the presense of
 * system libraries required by the Ars api java library.
 */
public class ArsApiVerifier {
    public static final String VERSION_UNSUPPORTED = "VERSION_UNSUPPORTED";
    public static final String VERSION_60 = "VERSION_60";
    public static final String VERSION_63 = "VERSION_63";
    public static final String VERSION_70 = "VERSION_70";
    
    /** 
     * Determines the version of the API that is currently loaded.  Currently
     * supported versions include: 
     *   VERSION_60
     *   VERSION_63
     *   VERSION_70
     * If the version is not supported this method will return VERSION_UNSUPPORTED.
     */
    public static String getApiVersion() throws MissingApiException {
        // Set the default version
        String currentVersion = VERSION_UNSUPPORTED;

        // Ensure the Api is accessible
        if (!VerificationUtils.classExists("com.remedy.arsys.api.Util")) {
            throw new MissingApiException();
        }
        // If com.remedy.arsys.api.internal.Constants exists this is 70
        else if (VerificationUtils.classExists("com.remedy.arsys.api.internal.Constants")) {
            return ArsApiVerifier.VERSION_70;
        }
        // If com.remedy.arsys.api.internal.InternalConstants exists this is 63
        else if (VerificationUtils.classExists("com.remedy.arsys.api.internal.InternalConstants")) {
            return ArsApiVerifier.VERSION_63;
        }
        // If com.remedy.arsys.api.ArchiveInfo exists and Constants/InternalConstants doesnt this is 60 
        else if (VerificationUtils.classExists("com.remedy.arsys.api.ArchiveInfo")) {
            return ArsApiVerifier.VERSION_60;
        }
        
        return currentVersion;
    }
    
    
    /** Returns an array of the string names of requires sytem libraries for the current version. */
    public static String[] getRequiredSystemLibraries() throws MissingApiException, UnsupportedVersionException {
        return getRequiredSystemLibraries(ArsApiVerifier.getApiVersion());
    }
    
    /** Returns an array of the string names of requires sytem libraries for the version specified. */
    public static String[] getRequiredSystemLibraries(String apiVersion) throws UnsupportedVersionException {
        String[] requiredLibraries = new String[0];
        
        if (apiVersion.equals(ArsApiVerifier.VERSION_60)) {
            requiredLibraries = ArsApiVerifier.getRequired60SystemLibraries();
        } else if (apiVersion.equals(ArsApiVerifier.VERSION_63)) {
            requiredLibraries = ArsApiVerifier.getRequired63SystemLibraries();
        } else if (apiVersion.equals(ArsApiVerifier.VERSION_70)) {
            requiredLibraries = ArsApiVerifier.getRequired70SystemLibraries();
        } else {
            throw new UnsupportedVersionException("Remedy version " + apiVersion + " is not a supported version.");
        }
        
        return requiredLibraries;
    }
    
    /** Returns an array of the string names of required system libraries for Ars 60. */
    public static String[] getRequired60SystemLibraries() {
        String[] requiredLibraries = {"arapi60", "arjni60", "arrpc60", "arutl60","icuin20","icudt20","icuuc20"};
        return requiredLibraries;
    }
    
    /** Returns an array of the string names of required system libraries for Ars 63. */
    public static String[] getRequired63SystemLibraries() {
        String[] requiredLibraries = {"arapi63","arjni63","arrpc63","arutl63","icudt28l","icuin28","icuuc28"};
        return requiredLibraries;
    }
    
    /** Returns an array of the string names of required system libraries for Ars 70. */
    public static String[] getRequired70SystemLibraries() {
        String[] requiredLibraries = {"arapi70","arjni70","arrpc70","arutl70","icudt32","icuin32","icuuc32"};
        return requiredLibraries;
    }
}
