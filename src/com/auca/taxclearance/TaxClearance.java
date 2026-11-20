package com.auca.taxclearance;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.time.LocalDate;

/** Tax-clearance decision. Status: PENDING, APPROVED or REJECTED. */
public class TaxClearance implements Identifiable, Validatable {
    private String clearanceId;
    private RegisteredTaxpayer taxpayer;
    private LocalDate requestDate;
    private LocalDate expiryDate;
    private String status;
    private RevenueOfficer approvedBy;

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate,
                        LocalDate expiryDate, String status, RevenueOfficer approvedBy) {
        this.clearanceId = clearanceId;
        this.taxpayer = taxpayer;
        this.requestDate = requestDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public String getStatus() { return status; }

    /** "problem" is the reason found by the data store (null means the taxpayer is clean). */
    public void approve(RevenueOfficer officer, String problem) throws ValidationException {
        if (!status.equals("PENDING")) throw new InvalidStatusTransitionException("Only a PENDING clearance can be approved (this one is " + status + ").");
        if (officer == null || !officer.isActive()) throw new UnauthorizedApprovalException("The officer is not active and cannot approve clearances.");
        if (problem != null) throw new PrerequisiteMissingException("Clearance cannot be APPROVED: " + problem);
        status = "APPROVED";
        approvedBy = officer;
    }

    public void reject(RevenueOfficer officer) throws ValidationException {
        if (!status.equals("PENDING")) throw new InvalidStatusTransitionException("Only a PENDING clearance can be rejected (this one is " + status + ").");
        if (officer == null || !officer.isActive()) throw new UnauthorizedApprovalException("The officer is not active.");
        status = "REJECTED";
        approvedBy = officer;
    }

    @Override public String getId() { return clearanceId; }
    @Override public String getDisplayName() { return "Clearance " + clearanceId; }
    @Override public boolean isActive() { return "APPROVED".equals(status) && expiryDate.isAfter(LocalDate.now()); }

    @Override
    public void validate() throws ValidationException {
        Validator.id(clearanceId, "Clearance ID", 3, 30);
        Validator.notNull(taxpayer, "Taxpayer");
        Validator.notNull(requestDate, "Request date");
        Validator.notNull(expiryDate, "Expiry date");
        if (requestDate.isAfter(LocalDate.now())) throw new ValidationException("Request date cannot be in the future.");
        if (!expiryDate.isAfter(requestDate)) throw new ValidationException("Expiry date must be after the request date.");
        Validator.oneOf(status, "Clearance status", "PENDING", "APPROVED", "REJECTED");
        if (status.equals("APPROVED") && approvedBy == null) throw new ValidationException("An APPROVED clearance must store the approving officer.");
    }

    @Override
    public void printDetails() {
        System.out.println("TAX CLEARANCE");
        Input.show("Clearance ID", clearanceId);
        Input.show("Taxpayer", taxpayer.getDisplayName() + " (TIN " + taxpayer.getTin() + ")");
        Input.show("Request date", requestDate);
        Input.show("Expiry date", expiryDate);
        Input.show("Status", status);
        Input.show("Approved by", approvedBy == null ? "NONE" : approvedBy.getStaffNumber() + " - " + approvedBy.getDisplayName());
    }

    @Override
    public String getSummary() {
        return "Clearance " + clearanceId + " | TIN " + taxpayer.getTin() + " | " + requestDate + " to " + expiryDate
                + " | " + status + " | by " + (approvedBy == null ? "NONE" : approvedBy.getStaffNumber());
    }
}
