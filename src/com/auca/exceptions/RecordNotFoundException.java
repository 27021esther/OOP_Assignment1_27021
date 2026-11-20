package com.auca.exceptions;

/** Thrown when a referenced record does not exist. */
public class RecordNotFoundException extends ValidationException {
    public RecordNotFoundException(String message) { super(message); }
}
