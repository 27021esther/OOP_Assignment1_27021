package com.auca.taxrefund;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.OverpaymentException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A refund request. Status: PENDING, APPROVED or REJECTED. (approvedBy/approvedDate are extra fields.) */
public class TaxRefund implements Identifiable, Validatable {
    private String refundId;
    private RegisteredTaxpayer taxpayer;
    private TaxAssessment assessment;
    private BigDecimal requestedAmount;
    private LocalDate requestDate;
    private String status;
    private RevenueOfficer decidedBy;
    private LocalDate decisionDate;

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment,
                     BigDecimal requestedAmount, LocalDate requestDate, String status) {
        this.refundId = refundId;
        this.taxpayer = taxpayer;
        this.assessment = assessment;
        this.requestedAmount = requestedAmount;
        this.requestDate = requestDate;
        this.status = status;
    }

    public TaxAssessment getAssessment() { return assessment; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public String getStatus() { return status; }

    /** All checks happen BEFORE the status changes, so a failed approval changes nothing. */
    public void approve(RevenueOfficer officer, BigDecimal overpaymentAvailable, LocalDate date) throws ValidationException {
        if (!status.equals("PENDING")) throw new InvalidStatusTransitionException("Only a PENDING refund can be approved (this one is " + status + ").");
        if (officer == null || !officer.isActive()) throw new UnauthorizedApprovalException("The officer is not active and cannot approve refunds.");
        if (requestedAmount.compareTo(officer.getApprovalLimit()) > 0) {
            throw new UnauthorizedApprovalException("Refund " + requestedAmount + " is above the officer's approval limit " + officer.getApprovalLimit() + ".");
        }
        if (requestedAmount.compareTo(overpaymentAvailable) > 0) {
            throw new OverpaymentException("Refund " + requestedAmount + " is more than the verified overpayment " + overpaymentAvailable + ".");
        }
        status = "APPROVED";
        decidedBy = officer;
        decisionDate = date;
    }

    public void reject(RevenueOfficer officer, LocalDate date) throws ValidationException {
        if (!status.equals("PENDING")) throw new InvalidStatusTransitionException("Only a PENDING refund can be rejected (this one is " + status + ").");
        if (officer == null || !officer.isActive()) throw new UnauthorizedApprovalException("The officer is not active.");
        status = "REJECTED";
        decidedBy = officer;
        decisionDate = date;
    }

    @Override public String getId() { return refundId; }
    @Override public String getDisplayName() { return "Refund " + refundId; }
    @Override public boolean isActive() { return "PENDING".equals(status); }

    @Override
    public void validate() throws ValidationException {
        Validator.id(refundId, "Refund ID", 3, 30);
        Validator.notNull(taxpayer, "Taxpayer");
        Validator.notNull(assessment, "Assessment");
        Validator.checkMoney(requestedAmount, "Requested amount", false);
        if (requestedAmount.signum() <= 0) throw new ValidationException("Requested refund must be greater than zero.");
        Validator.notNull(requestDate, "Request date");
        if (requestDate.isAfter(LocalDate.now())) throw new ValidationException("Request date cannot be in the future.");
        Validator.oneOf(status, "Refund status", "PENDING", "APPROVED", "REJECTED");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX REFUND");
        Input.show("Refund ID", refundId);
        Input.show("Taxpayer", taxpayer.getDisplayName() + " (TIN " + taxpayer.getTin() + ")");
        Input.show("Assessment", assessment.getId());
        Input.show("Requested amount", requestedAmount);
        Input.show("Request date", requestDate);
        Input.show("Status", status);
        if (decidedBy != null) Input.show("Decided by", decidedBy.getStaffNumber() + " on " + decisionDate);
    }

    @Override
    public String getSummary() {
        return "Refund " + refundId + " | TIN " + taxpayer.getTin() + " | assessment " + assessment.getId()
                + " | " + requestedAmount + " | " + requestDate + " | " + status;
    }
}
