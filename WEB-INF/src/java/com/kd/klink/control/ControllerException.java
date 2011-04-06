package com.kd.klink.control;

/**
 * This class represents an exception which occured in attempting to process
 * a Klink request.
 */
public class ControllerException extends Exception {
    public ControllerException(String msg) { super(msg); }
}
