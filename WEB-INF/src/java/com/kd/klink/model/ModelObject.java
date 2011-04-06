package com.kd.klink.model;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * An abstract representation of Klink model objects.  In addition to including
 * an implementation of the abstract method generateXmlElement it it suggested
 * that all child classes include a constructor which generates an instance of
 * that object from an Xml Element object.
 */
public abstract class ModelObject {
    // Pre-create the Xml document used for each model object
    protected Document document = documentBuilder.newDocument();
    
    // Declare a static DocumentBuilder (This is done for efficiency purposes)
    private static DocumentBuilder documentBuilder;
    // Create a single static document builder that can be reused for all ModelObjects
    static {
        // Try to create the builder
        try { documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); }
        // If there was a problem write it to StandardError
        catch (Exception e) {
            System.err.print("There was a problem initializing the static klink model members.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Returns the Xml representation of the ModelObject in the form of an Element.
     */
    public abstract Element generateXmlElement();
}