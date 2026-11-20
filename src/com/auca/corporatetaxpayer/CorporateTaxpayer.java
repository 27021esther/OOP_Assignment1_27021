package com.auca.corporatetaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A company that pays tax. The inherited first/last name and date of birth
 * describe the company CONTACT PERSON.
 */
public class CorporateTaxpayer extends RegisteredTaxpayer {
    private String companyRegistrationNo;
    private String legalName;
    private LocalDate incorporationDate;
    private String businessSector;
    private String authorizedRepresentative;
    private BigDecimal annualTurnover;

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email,
                             String phoneNumber, LocalDate dateOfBirth, String tin,
                             LocalDate registrationDate, String taxpayerStatus, String district,
                             String sector, String taxOffice, String companyRegistrationNo,
                             String legalName, LocalDate incorporationDate, String businessSector,
                             String authorizedRepresentative, BigDecimal annualTurnover) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin,
              registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.companyRegistrationNo = companyRegistrationNo;
        this.legalName = legalName;
        this.incorporationDate = incorporationDate;
        this.businessSector = businessSector;
        this.authorizedRepresentative = authorizedRepresentative;
        this.annualTurnover = annualTurnover;
    }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }

    @Override
    public BigDecimal getAnnualTurnover() { return annualTurnover; }

    @Override
    public String getPartyRole() { return "Corporate taxpayer"; }

    @Override
    public String getDisplayName() { return legalName + " (Company)"; }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Validator.pattern(companyRegistrationNo, "Company registration no.", 5, 30, "[A-Z0-9-]+", "uppercase letters, digits and -");
        Validator.text(legalName, "Legal name", 3, 120);
        Validator.notNull(incorporationDate, "Incorporation date");
        if (!incorporationDate.isBefore(getRegistrationDate())) throw new ValidationException("Incorporation date must be before the registration date.");
        Validator.text(businessSector, "Business sector", 2, 60);
        Validator.text(authorizedRepresentative, "Authorized representative", 2, 60);
        Validator.checkMoney(annualTurnover, "Annual turnover", false);
    }

    @Override
    public void printDetails() {
        System.out.println("CORPORATE TAXPAYER");
        super.printDetails();
        Input.show("Company reg. no.", companyRegistrationNo);
        Input.show("Legal name", legalName);
        Input.show("Incorporation date", incorporationDate);
        Input.show("Business sector", businessSector);
        Input.show("Authorized rep.", authorizedRepresentative);
        Input.show("Annual turnover", annualTurnover);
    }

    @Override
    public String getSummary() {
        return "Company " + getId() + " | " + legalName + " | TIN " + getTin()
                + " | RegNo " + companyRegistrationNo + " | " + getTaxpayerStatus();
    }
}
