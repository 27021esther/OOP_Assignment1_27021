package com.auca.exceptions;

/** Thrown when an officer is not allowed to approve. */
public class UnauthorizedApprovalException extends ValidationException {
    public UnauthorizedApprovalException(String message) { super(message); }
}
