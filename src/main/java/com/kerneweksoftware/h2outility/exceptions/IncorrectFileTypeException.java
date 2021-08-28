package com.kerneweksoftware.h2outility.exceptions;

/** Signals that the file being read is not the expected type. */
public class IncorrectFileTypeException extends Exception {
    
    public IncorrectFileTypeException(String message) {
        super(message);
    }
}
