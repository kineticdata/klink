package com.kd.klink.verification;

/**
 * This class is a simplified representation of java libraries (typically 
 * classes packaged within a jar file).  This class has four members: name, 
 * implementationVersion, specificationVersion, and libraryIndicatorClassName.
 * 
 * <li>Name is the name of the library (for example "log4j").
 * <li>SpecificaitonVersion is the version of the library specification to which
 * the library adheres (for example version 1.0.0).
 * <li>ImplementationVersion is the version of the library implementation (for
 * example version 1.0.0-NIGHTLY-SNAPSHOT-2006.01.01).
 * <li>LibraryIndicatorClassName is the class that should be used to check for
 * the presense of the library and the libraries version.
 */
public class Library {
    private String name = null;
    private String implementationVersion = null;
    private String specificationVersion = null;
    private String libraryIndicatorClassName = null;

    public Library(String name) {
        this(name, null, null);
    }
    public Library(String name, String libraryIndicatorClassName) {
        this(name, null, null, libraryIndicatorClassName);
    }
    public Library(String name, String implementationVersion, String specificationVersion) {
        this (name, implementationVersion, specificationVersion, null);
    }
    public Library(String name, String implementationVersion, String specificationVersion, String libraryIndicatorClassName) {
        this.setName(name);
        this.setImplementationVersion(implementationVersion);
        this.setSpecificationVersion(specificationVersion);
        this.libraryIndicatorClassName = libraryIndicatorClassName;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getImplementationVersion() { return this.implementationVersion; }
    public void setImplementationVersion(String implementationVersion) { this.implementationVersion = implementationVersion; }

    public String getSpecificationVersion() { return this.specificationVersion; }
    public void setSpecificationVersion(String specificationVersion) { this.specificationVersion = specificationVersion; }
    
    public String getLibraryIndicatorClassName() { return this.libraryIndicatorClassName; }
    public void setLibraryIndicatorClassName(String libraryIndicatorClassName) { this.libraryIndicatorClassName = libraryIndicatorClassName; }
}