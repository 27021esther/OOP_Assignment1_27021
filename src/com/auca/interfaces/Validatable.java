package com.auca.interfaces;

import com.auca.exceptions.ValidationException;

/** Anything that can check itself, print itself and give a one-line summary. */
public interface Validatable {
    void validate() throws ValidationException;
    void printDetails();
    String getSummary();
}
