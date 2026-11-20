package com.auca.exceptions;

/** Base exception: thrown whenever user data or a business rule is wrong. */
public class ValidationException extends Exception {
    public ValidationException(String message) { super(message); }
}
