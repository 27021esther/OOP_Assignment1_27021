package com.auca.taxdeclaration;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** The taxpayer's filed return. Status: SUBMITTED, LATE, ASSESSED or REJECTED. */
public class TaxDeclaration implements Identifiable, Validatable {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private String declarationId;
    private TaxObligation obligation;
    private LocalDate filingDate;
    private BigDecimal taxableAmount;
    private BigDecimal declaredTax;
    private String status;

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate,
                          BigDecimal taxableAmount, BigDecimal declaredTax, String status) {
        this.declarationId = declarationId;
        this.obligation = obligation;
        this.filingDate = filingDate;
        this.taxableAmount = taxableAmount;
        this.declaredTax = declaredTax;
        this.status = status;
    }

    public TaxObligation getObligation() { return obligation; }
    public LocalDate getFilingDate() { return filingDate; }
    public BigDecimal getDeclaredTax() { return declaredTax; }
    public String getStatus() { return status; }

    /** The filing date is never changed; lateness is simply calculated from it. */
    public boolean isLate() { return filingDate.isAfter(obligation.getDueDate()); }

    public void markAssessed() throws ValidationException {
        if (status.equals("REJECTED") || status.equals("ASSESSED")) {
            throw new InvalidStatusTransitionException("A " + status + " declaration cannot become ASSESSED.");
        }
        status = "ASSESSED";
    }

    public void reject() throws ValidationException {
        if (!status.equals("SUBMITTED") && !status.equals("LATE")) {
            throw new InvalidStatusTransitionException("Only SUBMITTED or LATE declarations can be rejected (this one is " + status + ").");
        }
        status = "REJECTED";
    }

    @Override public String getId() { return declarationId; }
    @Override public String getDisplayName() { return "Declaration " + declarationId; }
    @Override public boolean isActive() { return !"REJECTED".equals(status); }

    @Override
    public void validate() throws ValidationException {
        Validator.id(declarationId, "Declaration ID", 3, 30);
        Validator.notNull(obligation, "Obligation");
        Validator.notNull(filingDate, "Filing date");
        if (filingDate.isAfter(LocalDate.now())) throw new ValidationException("Filing date cannot be in the future.");
        Validator.checkMoney(taxableAmount, "Taxable amount", false);
        Validator.checkMoney(declaredTax, "Declared tax", false);
        Validator.oneOf(status, "Declaration status", "SUBMITTED", "LATE", "ASSESSED", "REJECTED");
        BigDecimal expected = obligation.getTaxType().calculateTax(taxableAmount);
        if (declaredTax.subtract(expected).abs().compareTo(TOLERANCE) > 0) {
            throw new InvalidTaxCalculationException("Declared tax " + declaredTax + " is wrong. Expected " + expected
                    + " (" + taxableAmount + " x " + obligation.getTaxType().getRate() + " / 100).");
        }
    }

    @Override
    public void printDetails() {
        System.out.println("TAX DECLARATION");
        Input.show("Declaration ID", declarationId);
        Input.show("Obligation", obligation.getId() + " (TIN " + obligation.getTaxpayer().getTin() + ")");
        Input.show("Filing date", filingDate);
        Input.show("Taxable amount", taxableAmount);
        Input.show("Declared tax", declaredTax);
        Input.show("Status", status);
    }

    @Override
    public String getSummary() {
        return "Declaration " + declarationId + " | obligation " + obligation.getId() + " | filed " + filingDate
                + " | taxable " + taxableAmount + " | tax " + declaredTax + " | " + status;
    }
}
