package com.kd.klink.ars.verification;

/**
 * This class represents the exception which occurs when trying to validate
 * a version of the java Ars api library that is not supported by the framework.
 */
public class UnsupportedVersionException extends Exception {
    public UnsupportedVersionException() { super(); }
    public UnsupportedVersionException(String msg) { super(msg); }
}
