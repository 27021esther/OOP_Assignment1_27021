package com.auca.abstracts;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Level 2 abstract class: a taxpayer that has a TIN.
 * Domain extension (abstract): getAnnualTurnover() - individuals and companies each know their own.
 */
public abstract class RegisteredTaxpayer extends TaxParty {
    private String tin;
    private LocalDate registrationDate;
    private String taxpayerStatus;
    private String district;
    private String sector;
    private String taxOffice;

    public RegisteredTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth, String tin,
                              LocalDate registrationDate, String taxpayerStatus,
                              String district, String sector, String taxOffice) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth); // constructor chaining
        this.tin = tin;
        this.registrationDate = registrationDate;
        this.taxpayerStatus = taxpayerStatus;
        this.district = district;
        this.sector = sector;
        this.taxOffice = taxOffice;
    }

    public String getTin() { return tin; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getTaxpayerStatus() { return taxpayerStatus; }
    public String getDistrict() { return district; }

    public void setDistrict(String newDistrict) throws ValidationException {
        this.district = Validator.text(newDistrict, "District", 2, 60);
    }

    /** Allowed: ACTIVE <-> SUSPENDED, and either one -> CLOSED. A CLOSED taxpayer is final. */
    public void changeStatus(String newStatus) throws ValidationException {
        String target = Validator.oneOf(newStatus, "Taxpayer status", "ACTIVE", "SUSPENDED", "CLOSED");
        if (taxpayerStatus.equals("CLOSED")) {
            throw new InvalidStatusTransitionException("A CLOSED taxpayer can never change status again.");
        }
        if (target.equals(taxpayerStatus)) {
            throw new InvalidStatusTransitionException("Taxpayer is already " + target + ".");
        }
        this.taxpayerStatus = target;
    }

    public abstract BigDecimal getAnnualTurnover();

    @Override
    public boolean isActive() { return "ACTIVE".equals(taxpayerStatus); }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Validator.digits(tin, "TIN", 9);
        Validator.notNull(registrationDate, "Registration date");
        if (registrationDate.isAfter(LocalDate.now())) throw new ValidationException("Registration date cannot be in the future.");
        if (!getDateOfBirth().isBefore(registrationDate)) throw new ValidationException("Date of birth must be before the registration date.");
        Validator.oneOf(taxpayerStatus, "Taxpayer status", "ACTIVE", "SUSPENDED", "CLOSED");
        Validator.text(district, "District", 2, 60);
        Validator.text(sector, "Sector", 2, 60);
        Validator.text(taxOffice, "Tax office", 2, 60);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        Input.show("TIN", tin);
        Input.show("Registration date", registrationDate);
        Input.show("Taxpayer status", taxpayerStatus);
        Input.show("District", district);
        Input.show("Sector", sector);
        Input.show("Tax office", taxOffice);
    }
}
