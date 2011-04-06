package com.kd.klink.ars.model;

import org.w3c.dom.*;
import org.apache.log4j.*;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

import com.remedy.arsys.api.*;

import com.kd.klink.model.*;
import com.kd.klink.model.entry.*;

/**
 * This class represents an Ars Attachment Field and contains the logic to
 * retrieve attachment information from an Ars server.
 */
public class ArsAttachment extends ArsObject {
    // Initialize the logger for this class
    private static final Logger logger = Logger.getLogger(ArsAttachment.class.getName());
    
    // Create the base64 encoder/decoder used to manipulate the attachment data
    private static final BASE64Encoder encoder = new BASE64Encoder();
    private static final BASE64Decoder decoder = new BASE64Decoder();

    // Declare the generic representation of the attachment
    private GenericEntryItem genericEntryItem;
    
    /**
     * This constructor retrieves an attachment stored in the Ars database
     * (using the ARGetEntryBlob method) and builds up the GenericEntryItem used
     * to internally represent that attachment.
     */
    public ArsAttachment(ArsContext context, String structure, String entryID, String attachmentID) throws ARException, ModelException {
        logger.debug("Constructing new RemedyAttachment object...");
        genericEntryItem = new GenericEntryItem(attachmentID);
        genericEntryItem.setType("ATTACHMENT");
        
        // Build the Criteria to only retrieve the attachment
        logger.debug("Criteria includes field: " + attachmentID);
        EntryListFieldInfo[]  entryFieldList = new EntryListFieldInfo[1];
        entryFieldList[0] = new EntryListFieldInfo(new FieldID(Long.parseLong(attachmentID)));
        EntryCriteria criteria = new EntryCriteria(entryFieldList);
        
        // Retrieve the Record
        logger.debug("Retrieving the record.");
        EntryKey entryKey = new EntryKey(new NameID(structure), new EntryID(entryID));
        Entry remedyEntry = EntryFactory.findByKey(context.getContext(), entryKey, criteria);
        
        // Retrieve the Attachment
        logger.debug("Retrieving the attachment.");
        EntryItem[] entryItems = remedyEntry.getEntryItems();
        if (entryItems != null && entryItems.length == 1) {
            if (entryItems[0].getValue().getDataType() != DataType.ATTACHMENT) {
                throw new ModelException("The field requested is not an attachment field.");
            } else {
                AttachmentInfo attachment = (AttachmentInfo)entryItems[0].getValue().getValue();

                if (attachment != null) {
                    // Obtain parameters
                    String attachmentName = attachment.getName();
                    String attachmentSize = String.valueOf(attachment.getOriginalSize());

                    // Obtain the data
                    byte[] data = new byte[(int)attachment.getOriginalSize()];
                    AttachmentInfo attach = new AttachmentInfo(attachmentName, 0, 0, data);
                    Util.ARGetEntryBlob(context.getContext(), new NameID(structure), new EntryID(entryID), new FieldID(Long.parseLong(attachmentID)), attach);

                    // Add the Context Messages
                    super.addMessages(context.getContext().getLastStatus());

                    // Build the ComplexItem
                    GenericComplexItem complexItem = new GenericComplexItem();
                    complexItem.addGenericItem(new GenericItem("Data", encoder.encode((byte[])attach.getValue())));
                    complexItem.addGenericItem(new GenericItem("Name", attachmentName));
                    complexItem.addGenericItem(new GenericItem("Size", attachmentSize));

                    // Add the complexItem to our attachment object
                    genericEntryItem.addGenericComplexItem(complexItem);
                }
            }
        }
    }
    
    /**
     * Displays the xml representation of this object.  For Example:
     * 
     * <EntryItem ID="[AttachmentFieldId]" Type="ATTACHMENT">
     *    <ComplexItem>
     *       <Item ID="Data">[Base64EncodedData]</Item>
     *       <Item ID="Name">[FilePathOrName]</Item>
     *       <Item ID="Size">[SizeOfFileInBytes]</Item>
     *    </CompexItem>
     * </EntryItem>
     */
    public Element generateXmlElement() {
        return genericEntryItem.generateXmlElement();
    }
}
