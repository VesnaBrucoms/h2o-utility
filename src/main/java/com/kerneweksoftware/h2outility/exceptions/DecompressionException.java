package com.kerneweksoftware.h2outility.exceptions;

/** Signals that data has failed to decompress. */
public class DecompressionException extends Exception {
    
    public DecompressionException(String message) {
        super(message);
    }
}
