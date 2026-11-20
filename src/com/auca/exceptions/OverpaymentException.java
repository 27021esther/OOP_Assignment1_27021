package com.auca.exceptions;

/** Thrown when a payment is too big or a refund has no (or too little) overpayment. */
public class OverpaymentException extends ValidationException {
    public OverpaymentException(String message) { super(message); }
}
