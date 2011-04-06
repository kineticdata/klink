package com.kd.klink.ars.model;

import java.util.ArrayList;

import com.remedy.arsys.api.*;

import com.kd.klink.model.*;
import com.kd.klink.model.response.*;
import com.kd.klink.ars.util.ArsUtil;

/**
 * This abstract class provides the general functionality used by all of the
 * com.kd.klink.ars.model classes.  Currently, this includes mechanisms for
 * maintaining information about Ars messages that were produced during their
 * creation/generation/instantiation.
 */
public abstract class ArsObject extends ModelObject {
    // Internal representation of the collection of messages associated with this object
    private ArrayList messageList = new ArrayList();
    
    /** Add a message to the current object. */
    public void addMessage(Message message) { messageList.add(message); }
    /** Convert the StatusInfo object into a Message and add it to the current object */
    public void addMessage(StatusInfo info) {  this.addMessage(this.generateMessage(info)); }
    /** Add an array of messages to the current object. */
    public void addMessages(Message[] messages) { 
        for (int i=0;i<messages.length;i++) {
            messageList.add(messages[i]);
        }
    }
    /** Convert the array of StatusInfo objects into Messages and add them to the current object. */
    public void addMessages(StatusInfo[] infos) { 
        for (int i=0;i<infos.length;i++) {
            this.addMessage(infos[i]);
        }
    }

    /** Convert the StatusInfo object into a Message object. */
    public static Message generateMessage(StatusInfo info) {
        Message message = new Message();
        
        // Obtain the required information from the StatusInfo
        String type = ArsUtil.decodeMessageType(info.getMessageType());
        long num = info.getMessageNum();
        String messageText = info.getMessageText();
        String appendedText = info.getAppendedText();
        
        // Set the Message type
        message.setType(type);

        // Set the Message's message
        boolean appendedTextIsBlank = (appendedText == null || appendedText.equals("") || appendedText == new String());
        boolean messageTextIsBlank = (messageText == null || messageText.equals("") || messageText == new String());

        if (appendedTextIsBlank && messageTextIsBlank) {
            message.setMessage("");
        } else if (appendedTextIsBlank) {
            message.setMessage(messageText);
        } else if (messageTextIsBlank) {
            message.setMessage(appendedText);
        } else {
            message.setMessage(messageText + ": " + appendedText);
        }
        
        // Add the Message Number
        message.addAttribute("MessageNumber", String.valueOf(num));
        
        return message;
    }
    
    /** Convert the array of StatusInfo objects into an array of Message objects. */
    public static Message[] generateMessages(StatusInfo[] infos) {
        Message[] messages = new Message[infos.length];
        for (int i=0;i<infos.length;i++) {
            messages[i] = generateMessage(infos[i]);
        }
        return messages;
    }
    
    /** Retrieve the array of messages associated with the current object. */
    public Message[] getMessages() {
        Message[] messages = new Message[messageList.size()];
        for (int i=0;i<messages.length;i++) {
            messages[i] = (Message)messageList.get(i);
        }
        return messages;
    }
}
