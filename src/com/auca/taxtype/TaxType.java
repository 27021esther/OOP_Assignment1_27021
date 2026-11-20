package com.auca.taxtype;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** A kind of tax (for example VAT). The rate is FICTIONAL and only used for this exercise. */
public class TaxType implements Identifiable, Validatable {
    private String taxTypeId;
    private String taxCode;
    private String taxName;
    private BigDecimal rate;
    private String filingFrequency;
    private boolean active;

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate,
                   String filingFrequency, boolean active) {
        this.taxTypeId = taxTypeId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.rate = rate;
        this.filingFrequency = filingFrequency;
        this.active = active;
    }

    public String getTaxCode() { return taxCode; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal newRate) { this.rate = newRate; }
    public void setActive(boolean newActive) { this.active = newActive; }

    /** expected tax = taxable x rate / 100, rounded HALF_UP to 2 decimals. */
    public BigDecimal calculateTax(BigDecimal taxableAmount) {
        return taxableAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override public String getId() { return taxTypeId; }
    @Override public String getDisplayName() { return taxCode + " - " + taxName; }
    @Override public boolean isActive() { return active; }

    @Override
    public void validate() throws ValidationException {
        Validator.id(taxTypeId, "Tax type ID", 3, 30);
        Validator.pattern(taxCode, "Tax code", 2, 10, "[A-Z0-9]+", "uppercase letters and digits");
        Validator.text(taxName, "Tax name", 3, 60);
        Validator.checkRate(rate, "Rate");
        Validator.oneOf(filingFrequency, "Filing frequency", "MONTHLY", "QUARTERLY", "ANNUAL");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX TYPE");
        Input.show("Tax type ID", taxTypeId);
        Input.show("Tax code", taxCode);
        Input.show("Tax name", taxName);
        Input.show("Rate (%)", rate);
        Input.show("Filing frequency", filingFrequency);
        Input.show("Active", active ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return "TaxType " + taxTypeId + " | " + getDisplayName() + " | rate " + rate + "% | "
                + filingFrequency + " | " + (active ? "ACTIVE" : "INACTIVE");
    }
}
