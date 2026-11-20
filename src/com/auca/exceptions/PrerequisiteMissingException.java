package com.auca.exceptions;

/** Thrown when something required first (active taxpayer, confirmed assessment...) is missing. */
public class PrerequisiteMissingException extends ValidationException {
    public PrerequisiteMissingException(String message) { super(message); }
}
