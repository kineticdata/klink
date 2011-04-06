package com.kd.klink.ars.model;

import java.util.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

/**
 * This class represents a list of Ars forms/schemas and contains the logic to 
 * retrieve a sorted list of forms/schemas available to the requesting user on
 * an Ars server.
 */
public class ArsStructureList extends ArsObject {
    // Initialize the logger for this class
    private static final Logger logger = Logger.getLogger(ArsStructureList.class.getName());
    
    // Create the comparer class used to compare SchemaKeys
    private final SchemaKeyComparer schemaKeyComparer = new SchemaKeyComparer();
    
    // Declare the internal array used to represent the list of Ars Schemas
    private SchemaKey[] schemas = new SchemaKey[0];
    
    /**
     * This constructor retrieves a list of all Ars forms/schemas which the
     * provided Context has permissions for and builds up an internal array
     * of corresponding SchemaKeys.
     */
    public ArsStructureList(ArsContext context) throws ARException {
        logger.debug("Constructing new ArsStructureList...");
        SchemaListCriteria criteria = new SchemaListCriteria(SchemaType.ALL, true, new Timestamp(0), null);
        schemas = SchemaFactory.find(context.getContext(), criteria);
        logger.debug("Found " + schemas.length + " structures on the server.");
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        Arrays.sort(schemas, schemaKeyComparer);
        logger.debug("ArsStructureList constructed.");
    }
    
    /**
     * Displays the xml representation of this object.  For Example:
     * 
     * <Structures>
     *    <Structure ID="AR System Administrator Preference"/>
     *    <Structure ID="AR System Application State"/>
     *    <Structure ID="AR System Currency Codes"/>
     *    ...
     *    <Structure ID="User"/>
     * </Structure>
     */
    public Element generateXmlElement() {
        Element structureList = document.createElement("Structures");
            
        // Add the child elements to the root
        if (schemas != null) {
            // Add the count
            structureList.setAttribute("Count", String.valueOf(schemas.length));
            
            // Add each of the structures
            for (int i=0;i<schemas.length;i++) {
                Element curElement = document.createElement("Structure");
                curElement.setAttribute("ID", schemas[i].getValue().toString());
                structureList.appendChild(curElement);
            }
        }

        return structureList;
    }
    
    /**
     * Private helper class used to compare two Schema keys.
     */
    private class SchemaKeyComparer implements Comparator {
        /**
        * The interface implementation should compare the two
        * objects and return an int using these rules:
        * if (a > b)  return > 0;
        * if (a == b) return 0;
        * if (a < b)  return < 0;
        */
        public int compare(Object a, Object b) {
            SchemaKey key_a = (SchemaKey)a;
            SchemaKey key_b = (SchemaKey)b;
            return key_a.getValue().compareTo(key_b.getValue());
        } 
    }
}
