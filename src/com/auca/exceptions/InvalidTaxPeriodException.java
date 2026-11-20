package com.auca.exceptions;

/** Thrown when a tax period or due date is wrong. */
public class InvalidTaxPeriodException extends ValidationException {
    public InvalidTaxPeriodException(String message) { super(message); }
}
