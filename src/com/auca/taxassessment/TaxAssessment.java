package com.auca.taxassessment;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** The officer's decision about how much tax is due. Status: DRAFT, CONFIRMED or CANCELLED. */
public class TaxAssessment implements Identifiable, Validatable {
    private String assessmentId;
    private TaxDeclaration declaration;
    private BigDecimal assessedTax;
    private BigDecimal adjustmentAmount;
    private LocalDate assessmentDate;
    private String status;

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax,
                         BigDecimal adjustmentAmount, LocalDate assessmentDate, String status) {
        this.assessmentId = assessmentId;
        this.declaration = declaration;
        this.assessedTax = assessedTax;
        this.adjustmentAmount = adjustmentAmount;
        this.assessmentDate = assessmentDate;
        this.status = status;
    }

    public TaxDeclaration getDeclaration() { return declaration; }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public String getStatus() { return status; }

    /** Assessment total = assessedTax + adjustmentAmount. */
    public BigDecimal getTotal() { return assessedTax.add(adjustmentAmount); }

    public void confirm() throws ValidationException {
        if (!status.equals("DRAFT")) {
            throw new InvalidStatusTransitionException("Only a DRAFT assessment can be confirmed (this one is " + status + ").");
        }
        status = "CONFIRMED";
    }

    public void cancel() throws ValidationException {
        if (status.equals("CANCELLED")) throw new InvalidStatusTransitionException("Assessment is already CANCELLED.");
        status = "CANCELLED";
    }

    /** A documented adjustment. The new total may never be negative. */
    public void setAdjustmentAmount(BigDecimal newAdjustment) throws ValidationException {
        if (status.equals("CANCELLED")) throw new InvalidStatusTransitionException("A CANCELLED assessment cannot be adjusted.");
        if (assessedTax.add(newAdjustment).signum() < 0) {
            throw new ValidationException("Assessment total cannot be negative (assessed tax is " + assessedTax + ").");
        }
        this.adjustmentAmount = newAdjustment;
    }

    @Override public String getId() { return assessmentId; }
    @Override public String getDisplayName() { return "Assessment " + assessmentId; }
    @Override public boolean isActive() { return !"CANCELLED".equals(status); }

    @Override
    public void validate() throws ValidationException {
        Validator.id(assessmentId, "Assessment ID", 3, 30);
        Validator.notNull(declaration, "Declaration");
        if ("REJECTED".equals(declaration.getStatus())) {
            throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
        }
        Validator.checkMoney(assessedTax, "Assessed tax", false);
        Validator.checkMoney(adjustmentAmount, "Adjustment amount", true);
        Validator.notNull(assessmentDate, "Assessment date");
        if (assessmentDate.isAfter(LocalDate.now())) throw new ValidationException("Assessment date cannot be in the future.");
        Validator.oneOf(status, "Assessment status", "DRAFT", "CONFIRMED", "CANCELLED");
        if (getTotal().signum() < 0) throw new ValidationException("Assessment total cannot be negative.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX ASSESSMENT");
        Input.show("Assessment ID", assessmentId);
        Input.show("Declaration", declaration.getId());
        Input.show("Assessed tax", assessedTax);
        Input.show("Adjustment amount", adjustmentAmount);
        Input.show("Assessment date", assessmentDate);
        Input.show("Status", status);
        Input.show("Total (tax + adjustment)", getTotal());
    }

    @Override
    public String getSummary() {
        return "Assessment " + assessmentId + " | declaration " + declaration.getId() + " | tax " + assessedTax
                + " adj " + adjustmentAmount + " = total " + getTotal() + " | " + status;
    }
}
