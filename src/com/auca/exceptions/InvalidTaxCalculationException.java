package com.auca.exceptions;

/** Thrown when declared tax or penalty does not match the calculation. */
public class InvalidTaxCalculationException extends ValidationException {
    public InvalidTaxCalculationException(String message) { super(message); }
}
