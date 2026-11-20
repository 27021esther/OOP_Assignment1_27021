package com.auca.individualtaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A person who pays tax. 18 fields in total (6 + 6 inherited + 6 own). */
public class IndividualTaxpayer extends RegisteredTaxpayer {
    private String nationalId;
    private String occupation;
    private String businessName;
    private LocalDate businessStartDate;
    private BigDecimal annualTurnover;
    private boolean resident;

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth, String tin,
                              LocalDate registrationDate, String taxpayerStatus, String district,
                              String sector, String taxOffice, String nationalId, String occupation,
                              String businessName, LocalDate businessStartDate,
                              BigDecimal annualTurnover, boolean resident) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin,
              registrationDate, taxpayerStatus, district, sector, taxOffice); // super(...) chaining
        this.nationalId = nationalId;
        this.occupation = occupation;
        this.businessName = businessName;
        this.businessStartDate = businessStartDate;
        this.annualTurnover = annualTurnover;
        this.resident = resident;
    }

    public String getNationalId() { return nationalId; }

    @Override
    public BigDecimal getAnnualTurnover() { return annualTurnover; }

    @Override
    public String getPartyRole() { return "Individual taxpayer"; }

    @Override
    public String getDisplayName() { return getFirstName() + " " + getLastName() + " (Individual)"; }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Validator.digits(nationalId, "National ID", 16);
        Validator.text(occupation, "Occupation", 2, 60);
        Validator.text(businessName, "Business name", 2, 120);
        Validator.notNull(businessStartDate, "Business start date");
        if (!businessStartDate.isBefore(getRegistrationDate())) throw new ValidationException("Business start date must be before the registration date.");
        Validator.checkMoney(annualTurnover, "Annual turnover", false);
    }

    @Override
    public void printDetails() {
        System.out.println("INDIVIDUAL TAXPAYER");
        super.printDetails();
        Input.show("National ID", nationalId);
        Input.show("Occupation", occupation);
        Input.show("Business name", businessName);
        Input.show("Business start date", businessStartDate);
        Input.show("Annual turnover", annualTurnover);
        Input.show("Resident", resident ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return "Individual " + getId() + " | " + getDisplayName() + " | TIN " + getTin()
                + " | NationalID " + nationalId + " | " + getTaxpayerStatus();
    }
}
