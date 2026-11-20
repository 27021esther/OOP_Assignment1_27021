package com.auca.revenueofficer;

import com.auca.abstracts.TaxParty;
import com.auca.exceptions.ValidationException;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A tax-office employee who can approve refunds and clearances. 12 fields (6 inherited + 6 own). */
public class RevenueOfficer extends TaxParty {
    private String staffNumber;
    private String department;
    private String role;
    private LocalDate hireDate;
    private BigDecimal approvalLimit;
    private boolean active;

    public RevenueOfficer(String partyId, String firstName, String lastName, String email,
                          String phoneNumber, LocalDate dateOfBirth, String staffNumber,
                          String department, String role, LocalDate hireDate,
                          BigDecimal approvalLimit, boolean active) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.approvalLimit = approvalLimit;
        this.active = active;
    }

    public String getStaffNumber() { return staffNumber; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }
    public void setApprovalLimit(BigDecimal newLimit) { this.approvalLimit = newLimit; }
    public void setActive(boolean newActive) { this.active = newActive; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public String getPartyRole() { return "Revenue officer (" + role + ")"; }

    @Override
    public String getDisplayName() { return "Officer " + getFirstName() + " " + getLastName(); }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Validator.pattern(staffNumber, "Staff number", 3, 20, "[A-Z0-9-]+", "uppercase letters, digits and -");
        Validator.text(department, "Department", 2, 60);
        Validator.text(role, "Role", 2, 60);
        Validator.notNull(hireDate, "Hire date");
        if (hireDate.isAfter(LocalDate.now())) throw new ValidationException("Hire date cannot be in the future.");
        if (!hireDate.isAfter(getDateOfBirth())) throw new ValidationException("Hire date must be after the date of birth.");
        Validator.checkMoney(approvalLimit, "Approval limit", false);
    }

    @Override
    public void printDetails() {
        System.out.println("REVENUE OFFICER");
        super.printDetails();
        Input.show("Staff number", staffNumber);
        Input.show("Department", department);
        Input.show("Role", role);
        Input.show("Hire date", hireDate);
        Input.show("Approval limit", approvalLimit);
        Input.show("Active", active ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return "Officer " + getId() + " | " + getDisplayName() + " | Staff " + staffNumber
                + " | limit " + approvalLimit + " | " + (active ? "ACTIVE" : "INACTIVE");
    }
}
