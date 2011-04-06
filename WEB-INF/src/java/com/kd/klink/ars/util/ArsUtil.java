package com.kd.klink.ars.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.remedy.arsys.api.*;

import org.apache.log4j.*;

/**
 * This is a utility class used for operations common to Ars as a datasource.
 */
public class ArsUtil {
    private static final Logger logger = Logger.getLogger(ArsUtil.class.getName());
    
    private static final DecimalFormat twoDigitFormatter = new DecimalFormat("00");
    private static final SimpleDateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat iso8601DateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** Converts a string in ISO8601 Format into the string corresponding to an Ars internal date representation */
    public static String convertIso8601FormatToArsDate(String dateString) throws ParseException {
        int years, months, days;
        DateInfo dateInfo = new DateInfo();
        try { years = Integer.parseInt(dateString.substring(0,4)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable date: " + dateString, 0); }
        try { months = Integer.parseInt(dateString.substring(5,7)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable date: " + dateString, 5); }
        try { days = Integer.parseInt(dateString.substring(8)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable date: " + dateString, 8); }
            
        dateInfo.setDate(years, months, days);
        
        return String.valueOf(dateInfo.getValue());
    }
    
    /** Converts a string in ISO8601 Format into the string corresponding to an Ars internal time representation */
    public static String convertIso8601FormatToArsTime(String timeString) throws ParseException {
        Date date = iso8601DateTimeFormatter.parse(timeString);
        int secondsSinceEpoch = (int)(date.getTime() / 1000);
        return String.valueOf(secondsSinceEpoch);
    }
    
    /** Converts a string in ISO8601 Format into the string corresponding to an Ars internal time of day representation */
    public static String convertIso8601FormatToArsTimeOfDay(String timeOfDayString) throws ParseException {
        int hours, minutes, seconds;
        int total = 0;
        
        try { hours = Integer.parseInt(timeOfDayString.substring(0,2)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable dime: " + timeOfDayString, 0); }
        
        try { minutes = Integer.parseInt(timeOfDayString.substring(3,5)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable dime: " + timeOfDayString, 3); }
        
        try { seconds = Integer.parseInt(timeOfDayString.substring(6)); }
        catch (NumberFormatException e) { throw new ParseException("Unparseable dime: " + timeOfDayString, 6); }

        total = hours*3600 + minutes*60 + seconds;
        
        return String.valueOf(total);
    }
    
    /** Converts a date string in Ars internal format into the corresponding iso8601 string. */
    public static String convertArsDateToIso8601Format(DateInfo dateInfo) {
        Date date = new Date(dateInfo.GetDate().getTimeInMillis());
        return iso8601DateFormatter.format(date);
    }

    /** Converts a time string in Ars internal format into the corresponding iso8601 string. */
    public static String convertArsTimeToIso8601Format(String time) {
        Date date = new Date(Long.parseLong(time) * 1000);
        return iso8601DateTimeFormatter.format(date);
    }
    
    /** Converts a time of day string in Ars internal format into the corresponding iso8601 string. */
    public static String convertArsTimeOfDayToIso8601Format(String timeOfDay) {
        int time = Integer.parseInt(timeOfDay);
        
        int hours, minutes, seconds;
        for(hours=0; time >= 3600; hours++) { time -= 3600; }
        for(minutes=0; time >= 60; minutes++) { time -= 60; }
        seconds = time;
        
        return twoDigitFormatter.format(hours) + ":" + twoDigitFormatter.format(minutes) + ":" + twoDigitFormatter.format(seconds);
    }
    
    /** Returns the string value associated with the internal integer reprsentation of an attachment type. */
    public static String decodeAttachmentType(int attachmentType) {
        java.lang.String attachmentTypeString;
        
        switch (attachmentType) {
            case Constants.AR_ATTACH_FIELD_TYPE_EMBED:
                attachmentTypeString = "EMBEDDED";
                break;
            case Constants.AR_ATTACH_FIELD_TYPE_LINK:
                attachmentTypeString = "LINK";
                break;
            default:
                logger.debug("Unknown AttachmentType: " + attachmentType);
                attachmentTypeString = "UNKNOWN_TYPE";
                break;
        }
        
        return attachmentTypeString;
    }
    
    /** Returns internal integer reprsentation associated with the string value of an attachment type. */
    public static int encodeAttachmentType(String attachmentType) {
        int attachmentTypeInt;
        if (attachmentType.equals("EMBEDDED")) {
            attachmentTypeInt = Constants.AR_ATTACH_FIELD_TYPE_EMBED;
        } else if (attachmentType.equals("LINK")) {
            attachmentTypeInt = Constants.AR_ATTACH_FIELD_TYPE_LINK;
        } else {
            logger.debug("Unknown AttachmentType: " + attachmentType);
            attachmentTypeInt = 0;
        }
        return attachmentTypeInt;
    }
    
    /** Returns the string value associated with the internal integer reprsentation of a permission type. */
    public static String decodeFieldPermissionType(int permissionType) {
        java.lang.String permissionTypeString;
        
        switch (permissionType) {
            case Constants.AR_PERMISSIONS_CHANGE:
                permissionTypeString = "CHANGE";
                break;
            case Constants.AR_PERMISSIONS_NONE:
                permissionTypeString = "NONE";
                break;
            case Constants.AR_PERMISSIONS_VIEW:
                permissionTypeString = "VIEW";
                break;
            default:
                logger.debug("Unknown FieldPermissionType: " + permissionType);
                permissionTypeString = "UNKNOWN_TYPE";
                break;
        }
        
        return permissionTypeString;
    }
    
    /** Returns the internal integer reprsentation associaated with the string value of a permission type. */
    public static int encodeFieldPermissionType(String permissionType) {
        int permissionTypeInt;
        if (permissionType.equals("CHANGE")) {
            permissionTypeInt = Constants.AR_RETURN_OK;
        } else if (permissionType.equals("NONE")) {
            permissionTypeInt = Constants.AR_RETURN_ERROR;
        } else if (permissionType.equals("VIEW")) {
            permissionTypeInt = Constants.AR_RETURN_FATAL;
        } else {
            logger.debug("Unknown FieldPermissionType: " + permissionType);
            permissionTypeInt = 0;
        }
        return permissionTypeInt;
    }
    
    /** Returns the string value associated with the internal integer reprsentation of a schema permission type. */
    public static String decodeSchemaPermissionType(int permissionType) {
        java.lang.String permissionTypeString;
        
        switch (permissionType) {
            case Constants.AR_PERMISSIONS_HIDDEN:
                permissionTypeString = "HIDDEN";
                break;
            case Constants.AR_PERMISSIONS_NONE:
                permissionTypeString = "NONE";
                break;
            case Constants.AR_PERMISSIONS_VISIBLE:
                permissionTypeString = "VISIBLE";
                break;
            default:
                logger.debug("Unknown SchemaPermissionType: " + permissionType);
                permissionTypeString = "UNKNOWN_TYPE";
                break;
        }
        
        return permissionTypeString;
    }
    
    /** Returns the internal integer reprsentation associated with the string value of a schema permission type. */
    public static int encodeSchemaPermissionType(String permissionType) {
        int permissionTypeInt;
        if (permissionType.equals("HIDDEN")) {
            permissionTypeInt = Constants.AR_RETURN_WARNING;
        } else if (permissionType.equals("NONE")) {
            permissionTypeInt = Constants.AR_RETURN_ERROR;
        } else if (permissionType.equals("VISIBLE")) {
            permissionTypeInt = Constants.AR_RETURN_BAD_STATUS;
        } else {
            logger.debug("Unknown SchemaPermissionType: " + permissionType);
            permissionTypeInt = 0;
        }
        return permissionTypeInt;
    }
    
    /** Returns the string value associated with the internal integer reprsentation of a message type. */
    public static String decodeMessageType(int messageType) {
        java.lang.String messageTypeString;
        
        switch (messageType) {
            case Constants.AR_RETURN_OK:
                messageTypeString = "OK";
                break;
            case Constants.AR_RETURN_WARNING: 
                messageTypeString = "WARNING";
                break;
            case Constants.AR_RETURN_ERROR:
                messageTypeString = "ERROR";
                break;
            case Constants.AR_RETURN_FATAL: 
                messageTypeString = "FATAL";
                break;
            case Constants.AR_RETURN_BAD_STATUS: 
                messageTypeString = "BAD_STATUS";
                break;
            case Constants.AR_RETURN_PROMPT:
                messageTypeString = "PROMPT";
                break;
            case Constants.AR_RETURN_ACCESSIBLE: 
                messageTypeString = "ACCESSIBLE";
                break;
            default:
                logger.debug("Unknown MessageType: " + messageType);
                messageTypeString = "UNKNOWN_TYPE";
                break;
        }

        return messageTypeString;        
    }
    
    /** Returns the internal integer reprsentation associated with the string value of a message type. */
    public static int encodeMessageType(String messageType) {
        int messageTypeInt;
        if (messageType.equals("OK")) {
            messageTypeInt = Constants.AR_RETURN_OK;
        } else if (messageType.equals("WARNING")) {
            messageTypeInt = Constants.AR_RETURN_WARNING;
        } else if (messageType.equals("ERROR")) {
            messageTypeInt = Constants.AR_RETURN_ERROR;
        } else if (messageType.equals("FATAL")) {
            messageTypeInt = Constants.AR_RETURN_FATAL;
        } else if (messageType.equals("BAD_STATUS")) {
            messageTypeInt = Constants.AR_RETURN_BAD_STATUS;
        } else if (messageType.equals("PROMPT")) {
            messageTypeInt = Constants.AR_RETURN_PROMPT;
        } else if (messageType.equals("ACCESSIBLE")) {
            messageTypeInt = Constants.AR_RETURN_ACCESSIBLE;
        } else {
            logger.debug("Unknown MessageType: " + messageType);
            messageTypeInt = 0;
        }
        return messageTypeInt;
    }
    
    public static String decodeFieldOption(int fieldOption) {
        java.lang.String fieldOptionString;
        
        switch (fieldOption) {
            case 1:
                fieldOptionString = "REQUIRED";
                break;
            case 2: 
                fieldOptionString = "OPTIONAL";
                break;
            case 3:
                fieldOptionString = "SYSTEM";
                break;
            case 4: 
                fieldOptionString = "DISPLAY_ONLY";
                break;
            default:
                logger.debug("Unknown FieldOption: " + fieldOption);
                fieldOptionString = "UNKNOWN_OPTION";
                break;
        }

        return fieldOptionString;        
    }
    
    public static int encodeFieldOption(String fieldOption) {
        int fieldOptionInt;
        if (fieldOption.equals("REQUIRED")) {
            fieldOptionInt = 1;
        } else if (fieldOption.equals("OPTIONAL")) {
            fieldOptionInt = 2;
        } else if (fieldOption.equals("SYSTEM")) {
            fieldOptionInt = 3;
        } else if (fieldOption.equals("DISPLAY_ONLY")) {
            fieldOptionInt = 4;
        } else {
            logger.debug("Unknown FieldOption: " + fieldOption);
            fieldOptionInt = 0;
        }
        return fieldOptionInt;
    }
    
    /**
     * Returns the string representation of a Schema type integer constant or 
     * "Unknown Type" if the type in integer form is unrecognized.
     *
     * 1 = BASE, 2 = JOIN, 3 = VIEW, 4 = DISPLAY-ONLY
     */
    public static String decodeSchemaType(int schemaType) {
        java.lang.String schemaTypeString;
        
        switch (schemaType) {
            case 1:
                schemaTypeString = "BASE";
                break;
            case 2: 
                schemaTypeString = "JOIN";
                break;
            case 3: 
                schemaTypeString = "VIEW";
                break;
            case 4:
                schemaTypeString = "DISPLAY_ONLY";
                break;
            default:
                logger.debug("Unknown SchemaType: " + schemaType);
                schemaTypeString = "UNKNOWN_TYPE";
                break;
        }

        return schemaTypeString;        
    }
    
    /**
     * Returns the integer representation of a Schema type string constant or 
     * 0 if the type in integer form is unrecognized.
     *
     * 1 = BASE, 2 = JOIN, 3 = VIEW, 4 = DISPLAY-ONLY, 0 = Unknown Type
     */
    public static int encodeSchemaType(String schemaType) {
        int schemaTypeInt;
        if (schemaType.equals("BASE")) {
            schemaTypeInt = 1;
        } else if (schemaType.equals("JOIN")) {
            schemaTypeInt = 2;
        } else if (schemaType.equals("VIEW")) {
            schemaTypeInt = 3;
        } else if (schemaType.equals("DISPLAY_ONLY")) {
            schemaTypeInt = 4;
        } else {
            logger.debug("Unknown SchemaType: " + schemaType);
            schemaTypeInt = 0;
        }
        return schemaTypeInt;
    }
    
    /** 
     * Returns the string representation of a field type integer constant (as
     * defined in com.remedy.arsys.api.Constants).  Valid strings include:
     * 'ATTACHMENT', 'ATTACHMENT_POOL', 'COLUMN', 'CONTROL', 'DATA', 'PAGE', 
     * 'PAGE_HOLDER', 'TABLE', and 'TRIM'.
     */
    public static String decodeFieldType(int fieldType) {
        java.lang.String fieldTypeString;
        switch (fieldType) {
            case Constants.AR_FIELD_TYPE_ATTACH:
                fieldTypeString = "ATTACHMENT";
                break;
            case Constants.AR_FIELD_TYPE_ATTACH_POOL:
                fieldTypeString = "ATTACHMENT_POOL";
                break;
            case Constants.AR_FIELD_TYPE_COLUMN:
                fieldTypeString = "COLUMN";
                break;
            case Constants.AR_FIELD_TYPE_CONTROL:
                fieldTypeString = "CONTROL";
                break;
            case Constants.AR_FIELD_TYPE_DATA:
                fieldTypeString = "DATA";
                break;
            case Constants.AR_FIELD_TYPE_PAGE:
                fieldTypeString = "PAGE";
                break;
            case Constants.AR_FIELD_TYPE_PAGE_HOLDER:
                fieldTypeString = "PAGE_HOLDER";
                break;
            case Constants.AR_FIELD_TYPE_TABLE:
                fieldTypeString = "TABLE";
                break;
            case Constants.AR_FIELD_TYPE_TRIM:
                fieldTypeString = "TRIM";
                break;
            case 0:
                fieldTypeString = "DATA_VISUALIZATION";
                break;
            default:
                logger.debug("Unknown FieldType: " + fieldType);
                fieldTypeString = "UNKNOWN_TYPE";
                break;
        }
        return fieldTypeString;
    }
    
    /** 
     * Returns the integer representation of a field type string constant (as
     * defined in com.remedy.arsys.api.Constants).  Valid strings include:
     * 'ATTACHMENT', 'ATTACHMENT_POOL', 'COLUMN', 'CONTROL', 'DATA', 'PAGE', 
     * 'PAGE_HOLDER', 'TABLE', and 'TRIM'.
     */
    public static int encodeFieldType(String fieldType) {
        int fieldTypeInt;
        if (fieldType.equals("ATTACHMENT")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_ATTACH;
        } else if (fieldType.equals("ATTACHMENT_POOL")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_ATTACH_POOL;
        } else if (fieldType.equals("COLUMN")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_COLUMN;
        } else if (fieldType.equals("CONTROL")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_CONTROL;
        } else if (fieldType.equals("DATA")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_DATA;
        } else if (fieldType.equals("DATA_VISUALIZATION")) {
            fieldTypeInt = 0;
        } else if (fieldType.equals("PAGE")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_PAGE;
        } else if (fieldType.equals("PAGE_HOLDER")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_PAGE_HOLDER;
        } else if (fieldType.equals("TABLE")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_TABLE;
        } else if (fieldType.equals("TRIM")) {
            fieldTypeInt = Constants.AR_FIELD_TYPE_TRIM;
        } else {
            logger.debug("Unknown FieldType: " + fieldType);
            fieldTypeInt = Constants.AR_DATA_TYPE_NULL;
        }
        return fieldTypeInt;
    }
    
    /** */
    public static String decodeDataType(int dataType) {
        java.lang.String dataTypeString;
        switch (dataType) {
            case Constants.AR_DATA_TYPE_ATTACH:
                dataTypeString = "ATTACHMENT";
                break;
            case Constants.AR_DATA_TYPE_ATTACH_POOL:
                dataTypeString = "ATTACHMENT_POOL";
                break;
            case Constants.AR_DATA_TYPE_BITMASK:
                dataTypeString = "BITMASK";
                break;
            case Constants.AR_DATA_TYPE_BYTES:
                dataTypeString = "BYTES";
                break;
            case Constants.AR_DATA_TYPE_CHAR:
                dataTypeString = "CHAR";
                break;
            case Constants.AR_DATA_TYPE_COLUMN:
                dataTypeString = "COLUMN";
                break;
            case Constants.AR_DATA_TYPE_CONTROL:
                dataTypeString = "CONTROL";
                break;
            case Constants.AR_DATA_TYPE_COORDS:
                dataTypeString = "COORDS";
                break;
            case Constants.AR_DATA_TYPE_CURRENCY:
                dataTypeString = "CURRENCY";
                break;
            case Constants.AR_DATA_TYPE_DATE:
                dataTypeString = "DATE";
                break;
            case Constants.AR_DATA_TYPE_DECIMAL:
                dataTypeString = "DECIMAL";
                break;
            case Constants.AR_DATA_TYPE_DIARY:
                dataTypeString = "DIARY";
                break;
            case Constants.AR_DATA_TYPE_DISPLAY:
                dataTypeString = "DISPLAY";
                break;
            case Constants.AR_DATA_TYPE_ENUM:
                dataTypeString = "ENUM";
                break;
            case Constants.AR_DATA_TYPE_INTEGER:
                dataTypeString = "INTEGER";
                break;
            case Constants.AR_DATA_TYPE_JOIN:
                dataTypeString = "JOIN";
                break;
            case Constants.AR_DATA_TYPE_KEYWORD:
                dataTypeString = "KEYWORD";
                break;
            case Constants.AR_DATA_TYPE_NULL:
                dataTypeString = "NULL";
                break;
            case Constants.AR_DATA_TYPE_PAGE:
                dataTypeString = "PAGE";
                break;
            case Constants.AR_DATA_TYPE_PAGE_HOLDER:
                dataTypeString = "PAGE_HOLDER";
                break;
            case Constants.AR_DATA_TYPE_REAL:
                dataTypeString = "REAL";
                break;
            case Constants.AR_DATA_TYPE_TABLE:
                dataTypeString = "TABLE";
                break;
            case Constants.AR_DATA_TYPE_TIME:
                dataTypeString = "TIME";
                break;
            case Constants.AR_DATA_TYPE_TIME_OF_DAY:
                dataTypeString = "TIME_OF_DAY";
                break;
            case Constants.AR_DATA_TYPE_TRIM:
                dataTypeString = "TRIM";
                break;
            case Constants.AR_DATA_TYPE_ULONG:
                dataTypeString = "ULONG";
                break;
            case Constants.AR_DATA_TYPE_VIEW:
                dataTypeString = "VIEW";
                break;
            default:
                logger.debug("Unknown DataType: " + dataType);
                dataTypeString = "UNKNOWN_TYPE";
                break;
        }
        return dataTypeString;
    }
    
    /** */
    public static int encodeDataType(String dataType) {
        int dataTypeInt;
        if (dataType.equals("ATTACHMENT")) {
            dataTypeInt = Constants.AR_DATA_TYPE_ATTACH;
        } else if (dataType.equals("ATTACHMENT_POOL")) {
            dataTypeInt = Constants.AR_DATA_TYPE_ATTACH_POOL;
        } else if (dataType.equals("BITMASK")) {
            dataTypeInt = Constants.AR_DATA_TYPE_BITMASK;
        } else if (dataType.equals("BYTES")) {
            dataTypeInt = Constants.AR_DATA_TYPE_BYTES;
        } else if (dataType.equals("CHAR")) {
            dataTypeInt = Constants.AR_DATA_TYPE_CHAR;
        } else if (dataType.equals("COLUMN")) {
            dataTypeInt = Constants.AR_DATA_TYPE_COLUMN;
        } else if (dataType.equals("CONTROL")) {
            dataTypeInt = Constants.AR_DATA_TYPE_CONTROL;
        } else if (dataType.equals("COORDS")) {
            dataTypeInt = Constants.AR_DATA_TYPE_COORDS;
        } else if (dataType.equals("CURRENCY")) {
            dataTypeInt = Constants.AR_DATA_TYPE_CURRENCY;
        } else if (dataType.equals("DATE")) {
            dataTypeInt = Constants.AR_DATA_TYPE_DATE;
        } else if (dataType.equals("DECIMAL")) {
            dataTypeInt = Constants.AR_DATA_TYPE_DECIMAL;
        } else if (dataType.equals("DIARY")) {
            dataTypeInt = Constants.AR_DATA_TYPE_DIARY;
        } else if (dataType.equals("DISPLAY")) {
            dataTypeInt = Constants.AR_DATA_TYPE_DISPLAY;
        } else if (dataType.equals("ENUM")) {
            dataTypeInt = Constants.AR_DATA_TYPE_ENUM;
        } else if (dataType.equals("INTEGER")) {
            dataTypeInt = Constants.AR_DATA_TYPE_INTEGER;
        } else if (dataType.equals("JOIN")) {
            dataTypeInt = Constants.AR_DATA_TYPE_JOIN;
        } else if (dataType.equals("KEYWORD")) {
            dataTypeInt = Constants.AR_DATA_TYPE_KEYWORD;
        } else if (dataType.equals("PAGE")) {
            dataTypeInt = Constants.AR_DATA_TYPE_PAGE;
        } else if (dataType.equals("PAGE_HOLDER")) {
            dataTypeInt = Constants.AR_DATA_TYPE_PAGE_HOLDER;
        } else if (dataType.equals("REAL")) {
            dataTypeInt = Constants.AR_DATA_TYPE_REAL;
        } else if (dataType.equals("TABLE")) {
            dataTypeInt = Constants.AR_DATA_TYPE_TABLE;
        } else if (dataType.equals("TIME")) {
            dataTypeInt = Constants.AR_DATA_TYPE_TIME;
        } else if (dataType.equals("TIME_OF_DAY")) {
            dataTypeInt = Constants.AR_DATA_TYPE_TIME_OF_DAY;
        } else if (dataType.equals("TRIM")) {
            dataTypeInt = Constants.AR_DATA_TYPE_TRIM;
        } else if (dataType.equals("ULONG")) {
            dataTypeInt = Constants.AR_DATA_TYPE_ULONG;
        } else if (dataType.equals("VIEW")) {
            dataTypeInt = Constants.AR_DATA_TYPE_VIEW;
        } else {
            logger.debug("Unknown DataType: " + dataType);
            dataTypeInt = Constants.AR_DATA_TYPE_NULL;
        }
        return dataTypeInt;
    }
}
