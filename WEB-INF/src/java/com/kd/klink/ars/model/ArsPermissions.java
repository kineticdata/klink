package com.kd.klink.ars.model;

import java.util.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

import com.remedy.arsys.api.*;

import com.kd.klink.ars.util.ArsUtil;

/**
 * This class is used to represent the permissions of both form/schemas and 
 * fields and contains logic required to retrieve this information from an Ars
 * system.
 */
public class ArsPermissions extends ArsObject {
    // Initialise the logger for this class
    private static final Logger logger = Logger.getLogger(ArsPermissions.class.getName());
    
    // Create the comparer used for sorting our results
    private final PermissionsComparer permissionsComparer = new PermissionsComparer();
    
    // Declare the string which represents the structure we are currently actively working on.
    private String structureID = new String();
    // Declare the array of Permissions that represent the actual form permissions.
    private Permission[] structurePermissions = new Permission[0];
    // Declare the HashMap used to contain the permissions of the different field permissions
    private Map itemPermissions = new LinkedHashMap();

    /** Provides defaults for the contructor including the actuall logic. */
    public ArsPermissions(ArsContext context, String structureName) throws ARException {
        this(context, structureName, true, new long[0]);
    }
    
    /**
     * This constructor retrieves permission information for forms and fields
     * and stores the information internally.
     */
    public ArsPermissions(ArsContext context, String structureName, boolean includeStructurePermissions, long[] itemList) throws ARException {
        logger.debug("Constructing new ArsPermissions(" + structureName + "])");
        
        // Set the structureID
        this.structureID = structureName;
        
        // Get the Schema (Used for the schema permissions and field permissions)
        SchemaCriteria schemaCriteria = new SchemaCriteria();
        schemaCriteria.setRetrieveAll(true);
        Schema schema = SchemaFactory.findByKey(context.getContext(), new SchemaKey(structureName), schemaCriteria);
        logger.debug("Retrieved schema: " + schema.getName().toString());
        
        // Add the Context Messages
        super.addMessages(context.getContext().getLastStatus());
        
        // Get SchemaPermission Info
        PermissionInfo[] schemaPermissions = schema.getPermissions();
        if (schemaPermissions == null) { schemaPermissions = new PermissionInfo[0]; }
        logger.debug("Retrieved " + schemaPermissions.length + " structure permissions.");
        
        // Set the default FieldList (Used to obtain the field permissions)
        Field[] fieldArray = new Field[0];
        
        // If we want field information
        if (itemList != null) {
            FieldListCriteria fieldListCriteria = null;
            if (itemList.length == 0) {
                logger.debug("Retrieving all item permissions...");
                fieldListCriteria = new FieldListCriteria(new NameID(structureName), new Timestamp(0), FieldType.AR_ALL_FIELD);
            } else {
                logger.debug("Retrieving specific item permissions...");
                FieldID[] fieldIDList = new FieldID[itemList.length];
                for (int i=0;i<itemList.length;i++) {
                    fieldIDList[i] = new FieldID(itemList[i]);
                }
                fieldListCriteria = new FieldListCriteria(new NameID(structureName), fieldIDList);
            }

            // Build FieldCriteria
            FieldCriteria criteria = new FieldCriteria();
            criteria.setPropertiesToRetrieve(FieldCriteria.PERMISSIONS);

            // Get the Field Permissions Array
            fieldArray = FieldFactory.findObjects(context.getContext(), fieldListCriteria, criteria);
            logger.debug("Retrieved " + fieldArray.length + " structure item permissions.");

            // Add the Context Messages
            super.addMessages(context.getContext().getLastStatus());
        }
        
        // If either the structure permissions are not null or we have at least one field build up this ArsPermissions.
        if (schemaPermissions != null || fieldArray.length > 0) {
            // Build up the GroupInfo hashtable
            GroupInfo[] groupInfo = Util.ARGetListGroup(context.getContext(), null, null);
            Map groupMap = new LinkedHashMap();
            for(int i=0;i<groupInfo.length;i++) {
                groupMap.put(groupInfo[i].getGroupID().toString(), groupInfo[i].getGroupName()[0].toString());
            }
            logger.debug("Retrieved group list containing " + groupMap.size() + " items.");

            // Add the Context Messages
            super.addMessages(context.getContext().getLastStatus());
            
            // If there are structure permissions
            if (includeStructurePermissions && schemaPermissions != null) {
                // Build up the schemaPermissions array
                structurePermissions = new Permission[schemaPermissions.length];
                for(int i=0;i<schemaPermissions.length;i++) {
                    structurePermissions[i] = this.generateSchemaPermission(schemaPermissions[i], groupMap);
                }

                // Sort the permissions Array since schema.getPermission() doesn't return any specific order
                Arrays.sort(structurePermissions, permissionsComparer);
                logger.debug("Stored the structure permissions.");
            }

            // Build up the fieldPermissionMap
            for(int i=0;i<fieldArray.length;i++) {
                Field curField = fieldArray[i];
                String fieldID = curField.getFieldID().toString();
                PermissionInfo[] fieldPermissions = curField.getPermissions();
                if (fieldPermissions == null) { fieldPermissions = new PermissionInfo[0]; }
                logger.debug("Field " + fieldID + " has " + fieldPermissions.length + " permissions.");

                // Generate the Permissions list for this field
                Permission[] currentPermissions = new Permission[fieldPermissions.length];
                for (int j=0;j<fieldPermissions.length;j++) {
                    currentPermissions[j] = this.generateFieldPermission(fieldPermissions[j], groupMap);
                }
                itemPermissions.put(fieldID, currentPermissions);
                logger.debug("Stored the structure item permissions.");
            }
        }
        
        logger.debug("ArsPermissions constructed.");
    }
    
    /** Private helper method used to generate a permission container object. */
    private Permission generateFieldPermission(PermissionInfo info, Map groupMap) {
        String groupID = info.getGroupID().toString();
        String groupName = (String)groupMap.get(groupID);
        String permission = ArsUtil.decodeFieldPermissionType(info.getPermissionValue());
        return new Permission(groupID, groupName, permission);
    }
    
    /** Private helper method used to generate a permission container object. */
    private Permission generateSchemaPermission(PermissionInfo info, Map groupMap) {
        String groupID = info.getGroupID().toString();
        String groupName = (String)groupMap.get(groupID);
        String permission = ArsUtil.decodeSchemaPermissionType(info.getPermissionValue());
        return new Permission(groupID, groupName, permission);
    }

    /**
     * Displays the xml representation of this object.  For Example:
     *
     * <Permissions>
     *    <StructurePermissions StructureID="SampleForm">
     *       <Permission GroupID="0" GroupName="Public">VISIBLE</Permission>
     *       <Permission GroupID="482" GroupName="APP-Administrator">VISIBLE</Permission>
     *    </StructurePermissions>
     *    <StructureItemPermissions>
     *       <StructureItemPermissions ItemID="1">
     *          <Permission GroupID="0" GroupName="Public">VIEW</Permission>
     *          <Permission GroupID="3" GroupName="Submitter">VIEW</Permission>
     *          <Permission GroupID="4" GroupName="Assignee">VIEW</Permission>
     *       </StructureItemPermissions>
     *       ...
     *    </StructureItemPermissions>
     * </Permissions>
     */
    public Element generateXmlElement() {
        Element arsPermissions = document.createElement("Permissions");
        arsPermissions.setAttribute("StructureID", this.structureID);
        Element structurePermissionsElement = document.createElement("StructurePermissions");
        Element structureItemPermissionsElement = document.createElement("StructureItemPermissions");

        // Build Up StructurePermissions
        for(int i=0;i<this.structurePermissions.length;i++) {
            Element curElement = document.createElement("Permission");
            curElement.setAttribute("GroupName", structurePermissions[i].groupName);
            curElement.setAttribute("GroupID", structurePermissions[i].groupID);
            curElement.appendChild(document.createTextNode(structurePermissions[i].permission));
            structurePermissionsElement.appendChild(curElement);
        }

        // Build Up StructureItemPermissions
        Iterator iterator = this.itemPermissions.keySet().iterator();
        for(int i=0;iterator.hasNext();i++) {
            String curID = (String)iterator.next();
            Permission[] curPermissions = (Permission[])this.itemPermissions.get(curID);
            Element curItemElement = document.createElement("StructureItemPermissions");
            curItemElement.setAttribute("ItemID", curID);

            for(int j=0;j<curPermissions.length;j++) {
                Element curPermissionElement = document.createElement("Permission");
                curPermissionElement.setAttribute("GroupName", curPermissions[j].groupName);
                curPermissionElement.setAttribute("GroupID", curPermissions[j].groupID);
                curPermissionElement.appendChild(document.createTextNode(curPermissions[j].permission));
                curItemElement.appendChild(curPermissionElement);
            }
            structureItemPermissionsElement.appendChild(curItemElement);
        }

        // Add the StructurePermissions and StructureItemPermissions to the root element
        if (structurePermissions.length > 0) { arsPermissions.appendChild(structurePermissionsElement); }
        if (itemPermissions.size() > 0) { arsPermissions.appendChild(structureItemPermissionsElement); }

        return arsPermissions;
    }
    
    /**
     * Private helper class used to contain information about a Permission.
     */
    private class Permission {
        public String groupID;
        public String groupName;
        public String permission;
        
        public Permission(String groupID, String groupName, String permission) {
            this.groupID = groupID;
            this.groupName = groupName;
            this.permission = permission;
        }
    }
    
    /**
     * Private helper class used to compare two permission objects.
     */
    public class PermissionsComparer implements Comparator {
        /**
        * The interface implementation should compare the two
        * objects and return an int using these rules:
        * if (a > b)  return > 0;
        * if (a == b) return 0;
        * if (a < b)  return < 0;
        */
        public int compare(Object a, Object b) {
            Permission perm_a = (Permission)a;
            Permission perm_b = (Permission)b;
            Integer int_a = Integer.valueOf(perm_a.groupID);
            Integer int_b = Integer.valueOf(perm_b.groupID);
            return int_a.compareTo(int_b);
        } 
    }
}
