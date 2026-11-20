package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.time.LocalDate;

/**
 * Level 1 abstract class: any person or organisation in the tax system.
 * It implements BOTH interfaces but leaves isActive() for the children.
 * Domain extension (abstract): getPartyRole() - every child must describe its role.
 */
public abstract class TaxParty implements Identifiable, Validatable {
    private String partyId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public TaxParty(String partyId, String firstName, String lastName, String email,
                    String phoneNumber, LocalDate dateOfBirth) {
        this.partyId = partyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public void setEmail(String newEmail) throws ValidationException { this.email = Validator.email(newEmail); }
    public void setPhoneNumber(String newPhone) throws ValidationException { this.phoneNumber = Validator.phone(newPhone); }

    /** Each child class gives its own role text (runtime polymorphism). */
    public abstract String getPartyRole();

    @Override
    public String getId() { return partyId; }

    @Override
    public String getDisplayName() { return firstName + " " + lastName; }

    @Override
    public void validate() throws ValidationException {
        Validator.id(partyId, "Party ID", 4, 30);
        Validator.text(firstName, "First name", 2, 60);
        Validator.text(lastName, "Last name", 2, 60);
        Validator.email(email);
        Validator.phone(phoneNumber);
        Validator.notNull(dateOfBirth, "Date of birth");
        if (dateOfBirth.isAfter(LocalDate.now())) throw new ValidationException("Date of birth cannot be in the future.");
    }

    @Override
    public void printDetails() {
        Input.show("Party ID", partyId);
        Input.show("First name", firstName);
        Input.show("Last name", lastName);
        Input.show("Email", email);
        Input.show("Phone number", phoneNumber);
        Input.show("Date of birth", dateOfBirth);
    }

    @Override
    public String getSummary() { return partyId + " | " + getDisplayName(); }
}
