package com.kd.klink.ars.model;

import org.w3c.dom.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.ars.util.ArsUtil;

/**
 * This class represents an Ars forms/schemas and contains the logic to retrieve
 * a description of the forms/schemas associated with the provided parameters.
 */
public class ArsStructure extends ArsObject {
    // Initialize the logger for this class
    private static final Logger logger = Logger.getLogger(ArsStructure.class.getName());
    
    // Declare the internal representation of the form/schema
    private Schema schema = null;
    
    // Declare the internal representation of the fields to include with the request
    private Field[] fieldList = new Field[0];
    
    /**
     * This constructure retrieves the description of the form/schema and fields
     * associated with the parameters provided and stores the information
     * internally.
     */
    public ArsStructure(ArsContext context, String structure, long[] fields) throws ARException {
        logger.debug("Constructing new ArsStructure(" + structure + ")...");
        SchemaCriteria schemaCriteria = new SchemaCriteria();
        schemaCriteria.setRetrieveAll(true);
        schema = SchemaFactory.findByKey(context.getContext(), new SchemaKey(structure), schemaCriteria);
        logger.debug("Structure retrieved.");
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        // Build the list of fields to retrieve
        FieldListCriteria fieldListCriteria = null;
        if (fields == null) {
            fieldListCriteria = new FieldListCriteria(new NameID(structure), new Timestamp(0), FieldType.AR_DATA_FIELD + FieldType.AR_ATTACH_FIELD);
        } else if (fields.length == 0) {
            fieldListCriteria = new FieldListCriteria(new NameID(structure), new Timestamp(0), FieldType.AR_ALL_FIELD);
        } else {
            FieldID[] fieldIDs = new FieldID[fields.length];
            for(int i=0;i<fieldIDs.length;i++) {
                fieldIDs[i] = new FieldID(fields[i]);
            }
            fieldListCriteria = new FieldListCriteria(new NameID(structure), fieldIDs);
        }

        // Build the field information to retrieve
        FieldCriteria fieldCriteria = new FieldCriteria();
        fieldCriteria.setRetrieveAll(true);
        
        // Retrive the fields
        this.fieldList = FieldFactory.findObjects(context.getContext(), fieldListCriteria, fieldCriteria);
        if (fieldList == null) { fieldList = new Field[0]; }
        logger.debug("Retrieved " + fieldList.length + " fields.");
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        logger.debug("ArsStructure created.");
    }
    
    /**
     * Displays the xml representation of this object.  For Example:
     * 
     * <Structure ID="User" Type="BASE">
     *    <StructureItem ID="1" Name="Request ID" Type="DATA">
     *       <DataType>CHAR</DataType>
     *       <EntryMode>SYSTEM</EntryMode>
     *    </StructureItem>
     *    <StructureItem ID="2" Name="Creator" Type="DATA">
     *       <DataType>CHAR</DataType>
     *       <EntryMode>REQUIRED</EntryMode>
     *    </StructureItem>
     *    ...
     *    <StructureItem ID="490000100" Name="Object ID" Type="DATA">
     *       <DataType>CHAR</DataType>
     *       <EntryMode>OPTIONAL</EntryMode>
     *    </StructureItem>
     * </Structure>
     */
    public Element generateXmlElement() {
        Element structure = document.createElement("Structure");
        structure.setAttribute("ID", schema.getName().toString());
        structure.setAttribute("Type", ArsUtil.decodeSchemaType(schema.getSchemaType()));
            
        // Add the child elements to the root
        for (int i=0;i<fieldList.length;i++) {
            String fieldID =  fieldList[i].getFieldID().toString();
            String fieldName = fieldList[i].getName().toString();
            String fieldType = ArsUtil.decodeFieldType(fieldList[i].getFieldType());

            // Generate a child element
            Element curElement = document.createElement("StructureItem");
            curElement.setAttribute("ID", fieldID);
            curElement.setAttribute("Name", fieldName);
            curElement.setAttribute("Type", fieldType);

            // If we are looking at an attachment element get the type and max size
            if (fieldType == "ATTACHMENT") {
                AttachmentLimitInfo info = (AttachmentLimitInfo)fieldList[i].getFieldLimit();

                // Add the attachment type child
                Element attachmentType = document.createElement("AttachmentType");
                attachmentType.appendChild(document.createTextNode(ArsUtil.decodeAttachmentType(info.getAttachType())));
                curElement.appendChild(attachmentType);

                // Add the max size child
                Element maxSize = document.createElement("MaxSize");
                maxSize.appendChild(document.createTextNode(String.valueOf(info.getMaxSize())));
                curElement.appendChild(maxSize);
            }
            // If we are looking at a data element
            else if (fieldType == "DATA") {
                String dataType = ArsUtil.decodeDataType(fieldList[i].getDataType().toInt());
                String defaultValue = fieldList[i].getDefaultValue().toString();

                // If the datatype is ENUM display the selection attributes
                if (dataType == "ENUM") {
                    Element attributesElement = document.createElement("Attributes");
                    EnumLimitInfo info = (EnumLimitInfo)fieldList[i].getFieldLimit(); 
                    NameID[] names = ArsMultiversionManager.getNameIDs(info);
                    for(int j=0;j<names.length;j++) {
                        Element attributeElement = document.createElement("Attribute");
                        attributeElement.appendChild(document.createTextNode(names[j].toString()));
                        attributesElement.appendChild(attributeElement);
                    }
                    curElement.appendChild(attributesElement);
                }

                // Add the datatype of the current data item
                Element typeElement = document.createElement("DataType");
                typeElement.appendChild(document.createTextNode(dataType));
                curElement.appendChild(typeElement);

                // If there is a defined default value add it as a child element
                if (defaultValue != null && defaultValue != "" && defaultValue != "null") {
                    Element defaultElement = document.createElement("DefaultValue");
                    defaultElement.appendChild(document.createTextNode(defaultValue));
                    curElement.appendChild(defaultElement);
                }

                // Add the entry mode of the current data item
                Element entryModeElement = document.createElement("EntryMode");
                entryModeElement.appendChild(document.createTextNode(ArsUtil.decodeFieldOption(fieldList[i].getFieldOption())));
                curElement.appendChild(entryModeElement);
            }

            structure.appendChild(curElement);
        }

        return structure;
    }
}
