package com.auca.taxobligation;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxtype.TaxType;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.time.LocalDate;

/** "This taxpayer must file this tax type for this period." Holds REAL object references. */
public class TaxObligation implements Identifiable, Validatable {
    private String obligationId;
    private RegisteredTaxpayer taxpayer;
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    public TaxObligation(String obligationId, RegisteredTaxpayer taxpayer, TaxType taxType,
                         LocalDate periodStart, LocalDate periodEnd, LocalDate dueDate) {
        this.obligationId = obligationId;
        this.taxpayer = taxpayer;
        this.taxType = taxType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
    }

    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxType getTaxType() { return taxType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getDueDate() { return dueDate; }

    @Override public String getId() { return obligationId; }
    @Override public String getDisplayName() { return "Obligation " + obligationId + " (" + taxType.getTaxCode() + ")"; }
    @Override public boolean isActive() { return taxpayer != null && taxpayer.isActive(); }

    @Override
    public void validate() throws ValidationException {
        Validator.id(obligationId, "Obligation ID", 3, 30);
        Validator.notNull(taxpayer, "Taxpayer");
        Validator.notNull(taxType, "Tax type");
        Validator.notNull(periodStart, "Period start");
        Validator.notNull(periodEnd, "Period end");
        Validator.notNull(dueDate, "Due date");
        if (!taxpayer.isActive()) throw new PrerequisiteMissingException("Taxpayer " + taxpayer.getTin() + " is not ACTIVE.");
        if (!taxType.isActive()) throw new PrerequisiteMissingException("Tax type " + taxType.getTaxCode() + " is not active.");
        if (periodStart.isAfter(periodEnd)) throw new InvalidTaxPeriodException("Period start must be on or before period end.");
        if (dueDate.isBefore(periodEnd)) throw new InvalidTaxPeriodException("Due date must not be before the period end.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX OBLIGATION");
        Input.show("Obligation ID", obligationId);
        Input.show("Taxpayer", taxpayer.getDisplayName() + " (TIN " + taxpayer.getTin() + ")");
        Input.show("Tax type", taxType.getDisplayName());
        Input.show("Period start", periodStart);
        Input.show("Period end", periodEnd);
        Input.show("Due date", dueDate);
    }

    @Override
    public String getSummary() {
        return "Obligation " + obligationId + " | TIN " + taxpayer.getTin() + " | " + taxType.getTaxCode()
                + " | " + periodStart + " to " + periodEnd + " | due " + dueDate;
    }
}
