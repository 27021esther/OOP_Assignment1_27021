package com.auca.exceptions;

/** Thrown when a unique value (TIN, ID, reference...) is already used. */
public class DuplicateRecordException extends ValidationException {
    public DuplicateRecordException(String message) { super(message); }
}
