package com.auca.taxpayment;

import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One payment (full or partial) against a CONFIRMED assessment. */
public class TaxPayment implements Identifiable, Validatable {
    private String paymentId;
    private TaxAssessment assessment;
    private String paymentReference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    public TaxPayment(String paymentId, TaxAssessment assessment, String paymentReference,
                      BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.paymentId = paymentId;
        this.assessment = assessment;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public TaxAssessment getAssessment() { return assessment; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }

    @Override public String getId() { return paymentId; }
    @Override public String getDisplayName() { return "Payment " + paymentReference; }
    @Override public boolean isActive() { return true; }

    @Override
    public void validate() throws ValidationException {
        Validator.id(paymentId, "Payment ID", 3, 30);
        Validator.notNull(assessment, "Assessment");
        if (!"CONFIRMED".equals(assessment.getStatus())) {
            throw new PrerequisiteMissingException("Payments need a CONFIRMED assessment (this one is " + assessment.getStatus() + ").");
        }
        Validator.id(paymentReference, "Payment reference", 4, 30);
        Validator.checkMoney(amount, "Amount", false);
        if (amount.signum() <= 0) throw new ValidationException("Payment amount must be greater than zero.");
        Validator.notNull(paymentDate, "Payment date");
        if (paymentDate.isAfter(LocalDate.now())) throw new ValidationException("Payment date cannot be in the future.");
        Validator.oneOf(paymentMethod, "Payment method", "CASH", "MOBILE_MONEY", "BANK_TRANSFER", "CARD");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX PAYMENT");
        Input.show("Payment ID", paymentId);
        Input.show("Assessment", assessment.getId());
        Input.show("Payment reference", paymentReference);
        Input.show("Amount", amount);
        Input.show("Payment date", paymentDate);
        Input.show("Payment method", paymentMethod);
    }

    @Override
    public String getSummary() {
        return "Payment " + paymentId + " | ref " + paymentReference + " | assessment " + assessment.getId()
                + " | " + amount + " | " + paymentDate + " | " + paymentMethod;
    }
}
