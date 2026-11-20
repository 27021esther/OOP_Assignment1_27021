package com.auca.exceptions;

/** Thrown when a status change is not allowed. */
public class InvalidStatusTransitionException extends ValidationException {
    public InvalidStatusTransitionException(String message) { super(message); }
}
