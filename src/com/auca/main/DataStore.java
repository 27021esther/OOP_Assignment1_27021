package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.penalty.Penalty;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Keeps ALL records in fixed-size arrays (no List/Map/Set).
 * Each array has a counter: only positions 0..count-1 are used.
 */
public class DataStore {
    public IndividualTaxpayer[] individuals = new IndividualTaxpayer[100];
    public int individualCount = 0;
    public CorporateTaxpayer[] corporates = new CorporateTaxpayer[100];
    public int corporateCount = 0;
    public RevenueOfficer[] officers = new RevenueOfficer[50];
    public int officerCount = 0;
    public TaxType[] taxTypes = new TaxType[30];
    public int taxTypeCount = 0;
    public TaxObligation[] obligations = new TaxObligation[200];
    public int obligationCount = 0;
    public TaxDeclaration[] declarations = new TaxDeclaration[200];
    public int declarationCount = 0;
    public TaxAssessment[] assessments = new TaxAssessment[200];
    public int assessmentCount = 0;
    public TaxPayment[] payments = new TaxPayment[300];
    public int paymentCount = 0;
    public Penalty[] penalties = new Penalty[200];
    public int penaltyCount = 0;
    public TaxRefund[] refunds = new TaxRefund[100];
    public int refundCount = 0;
    public TaxClearance[] clearances = new TaxClearance[100];
    public int clearanceCount = 0;

    // ------------------------------------------------------------ saving
    private ValidationException full(String what, int capacity) {
        return new ValidationException("Storage for " + what + " is full (" + capacity + " records). Nothing was saved.");
    }

    /** Validates the object one last time, then stores it. Nothing is stored if anything fails. */
    public void save(Object obj) throws ValidationException {
        ((Validatable) obj).validate();
        if (obj instanceof IndividualTaxpayer) {
            if (individualCount >= individuals.length) throw full("individual taxpayers", individuals.length);
            individuals[individualCount++] = (IndividualTaxpayer) obj;
        } else if (obj instanceof CorporateTaxpayer) {
            if (corporateCount >= corporates.length) throw full("corporate taxpayers", corporates.length);
            corporates[corporateCount++] = (CorporateTaxpayer) obj;
        } else if (obj instanceof RevenueOfficer) {
            if (officerCount >= officers.length) throw full("officers", officers.length);
            officers[officerCount++] = (RevenueOfficer) obj;
        } else if (obj instanceof TaxType) {
            if (taxTypeCount >= taxTypes.length) throw full("tax types", taxTypes.length);
            taxTypes[taxTypeCount++] = (TaxType) obj;
        } else if (obj instanceof TaxObligation) {
            if (obligationCount >= obligations.length) throw full("obligations", obligations.length);
            obligations[obligationCount++] = (TaxObligation) obj;
        } else if (obj instanceof TaxDeclaration) {
            if (declarationCount >= declarations.length) throw full("declarations", declarations.length);
            declarations[declarationCount++] = (TaxDeclaration) obj;
        } else if (obj instanceof TaxAssessment) {
            if (assessmentCount >= assessments.length) throw full("assessments", assessments.length);
            assessments[assessmentCount++] = (TaxAssessment) obj;
        } else if (obj instanceof TaxPayment) {
            if (paymentCount >= payments.length) throw full("payments", payments.length);
            payments[paymentCount++] = (TaxPayment) obj;
        } else if (obj instanceof Penalty) {
            if (penaltyCount >= penalties.length) throw full("penalties", penalties.length);
            penalties[penaltyCount++] = (Penalty) obj;
        } else if (obj instanceof TaxRefund) {
            if (refundCount >= refunds.length) throw full("refunds", refunds.length);
            refunds[refundCount++] = (TaxRefund) obj;
        } else if (obj instanceof TaxClearance) {
            if (clearanceCount >= clearances.length) throw full("clearances", clearances.length);
            clearances[clearanceCount++] = (TaxClearance) obj;
        } else {
            throw new ValidationException("Unknown record type.");
        }
    }

    // ------------------------------------------------------------ searching helpers
    private Identifiable findById(Identifiable[] arr, int count, String id) {
        for (int i = 0; i < count; i++) {
            if (arr[i].getId().equalsIgnoreCase(id)) return arr[i];
        }
        return null;
    }

    public boolean partyIdExists(String id) {
        return findById(individuals, individualCount, id) != null
                || findById(corporates, corporateCount, id) != null
                || findById(officers, officerCount, id) != null;
    }

    /** True if a record of this kind (see Rules constants) already uses this ID. */
    public boolean idExists(int kind, String id) {
        switch (kind) {
            case Rules.TAXTYPE: return findById(taxTypes, taxTypeCount, id) != null;
            case Rules.OBLIGATION: return findById(obligations, obligationCount, id) != null;
            case Rules.DECLARATION: return findById(declarations, declarationCount, id) != null;
            case Rules.ASSESSMENT: return findById(assessments, assessmentCount, id) != null;
            case Rules.PAYMENT: return findById(payments, paymentCount, id) != null;
            case Rules.PENALTY: return findById(penalties, penaltyCount, id) != null;
            case Rules.REFUND: return findById(refunds, refundCount, id) != null;
            case Rules.CLEARANCE: return findById(clearances, clearanceCount, id) != null;
            default: return false;
        }
    }

    public RegisteredTaxpayer findTaxpayerOrNull(String tin) {
        for (int i = 0; i < individualCount; i++) if (individuals[i].getTin().equals(tin)) return individuals[i];
        for (int i = 0; i < corporateCount; i++) if (corporates[i].getTin().equals(tin)) return corporates[i];
        return null;
    }

    public boolean nationalIdExists(String nid) {
        for (int i = 0; i < individualCount; i++) if (individuals[i].getNationalId().equals(nid)) return true;
        return false;
    }

    public boolean regNoExists(String regNo) {
        for (int i = 0; i < corporateCount; i++) if (corporates[i].getCompanyRegistrationNo().equalsIgnoreCase(regNo)) return true;
        return false;
    }

    public boolean taxCodeExists(String code) {
        for (int i = 0; i < taxTypeCount; i++) if (taxTypes[i].getTaxCode().equalsIgnoreCase(code)) return true;
        return false;
    }

    public boolean paymentRefExists(String ref) {
        for (int i = 0; i < paymentCount; i++) if (payments[i].getPaymentReference().equalsIgnoreCase(ref)) return true;
        return false;
    }

    public boolean staffExists(String staff) {
        for (int i = 0; i < officerCount; i++) if (officers[i].getStaffNumber().equalsIgnoreCase(staff)) return true;
        return false;
    }

    // ------------------------------------------------------------ finders that throw
    public RegisteredTaxpayer findTaxpayer(String tin) throws RecordNotFoundException {
        RegisteredTaxpayer t = findTaxpayerOrNull(tin);
        if (t == null) throw new RecordNotFoundException("No taxpayer with TIN " + tin + " exists.");
        return t;
    }

    public RevenueOfficer findOfficer(String staff) throws RecordNotFoundException {
        for (int i = 0; i < officerCount; i++) if (officers[i].getStaffNumber().equalsIgnoreCase(staff)) return officers[i];
        throw new RecordNotFoundException("No officer with staff number " + staff + " exists.");
    }

    public TaxType findTaxType(String code) throws RecordNotFoundException {
        for (int i = 0; i < taxTypeCount; i++) if (taxTypes[i].getTaxCode().equalsIgnoreCase(code)) return taxTypes[i];
        throw new RecordNotFoundException("No tax type with code " + code + " exists.");
    }

    public TaxObligation findObligation(String id) throws RecordNotFoundException {
        Identifiable x = findById(obligations, obligationCount, id);
        if (x == null) throw new RecordNotFoundException("Obligation " + id + " was not found.");
        return (TaxObligation) x;
    }

    public TaxDeclaration findDeclaration(String id) throws RecordNotFoundException {
        Identifiable x = findById(declarations, declarationCount, id);
        if (x == null) throw new RecordNotFoundException("Declaration " + id + " was not found.");
        return (TaxDeclaration) x;
    }

    public TaxAssessment findAssessment(String id) throws RecordNotFoundException {
        Identifiable x = findById(assessments, assessmentCount, id);
        if (x == null) throw new RecordNotFoundException("Assessment " + id + " was not found.");
        return (TaxAssessment) x;
    }

    public TaxRefund findRefund(String id) throws RecordNotFoundException {
        Identifiable x = findById(refunds, refundCount, id);
        if (x == null) throw new RecordNotFoundException("Refund " + id + " was not found.");
        return (TaxRefund) x;
    }

    public TaxClearance findClearance(String id) throws RecordNotFoundException {
        Identifiable x = findById(clearances, clearanceCount, id);
        if (x == null) throw new RecordNotFoundException("Clearance " + id + " was not found.");
        return (TaxClearance) x;
    }

    // ------------------------------------------------------------ business-rule lookups
    public boolean overlapExists(RegisteredTaxpayer tp, TaxType tt, LocalDate start, LocalDate end) {
        for (int i = 0; i < obligationCount; i++) {
            TaxObligation o = obligations[i];
            if (o.getTaxpayer() == tp && o.getTaxType() == tt
                    && !start.isAfter(o.getPeriodEnd()) && !end.isBefore(o.getPeriodStart())) return true;
        }
        return false;
    }

    /** The one non-rejected declaration of an obligation (or null). */
    public TaxDeclaration activeDeclarationFor(TaxObligation o) {
        for (int i = 0; i < declarationCount; i++) {
            if (declarations[i].getObligation() == o && declarations[i].isActive()) return declarations[i];
        }
        return null;
    }

    /** An assessment of this declaration that is not CANCELLED (or null). */
    public TaxAssessment liveAssessmentFor(TaxDeclaration d) {
        for (int i = 0; i < assessmentCount; i++) {
            if (assessments[i].getDeclaration() == d && assessments[i].isActive()) return assessments[i];
        }
        return null;
    }

    public TaxAssessment confirmedAssessmentFor(TaxDeclaration d) {
        for (int i = 0; i < assessmentCount; i++) {
            if (assessments[i].getDeclaration() == d && assessments[i].getStatus().equals("CONFIRMED")) return assessments[i];
        }
        return null;
    }

    public boolean penaltyExists(TaxObligation o, String type) {
        for (int i = 0; i < penaltyCount; i++) {
            if (penalties[i].getObligation() == o && penalties[i].getPenaltyType().equals(type)) return true;
        }
        return false;
    }

    public TaxRefund pendingRefundFor(TaxAssessment a) {
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i].getAssessment() == a && refunds[i].getStatus().equals("PENDING")) return refunds[i];
        }
        return null;
    }

    // ------------------------------------------------------------ money
    public BigDecimal paid(TaxAssessment a) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < paymentCount; i++) if (payments[i].getAssessment() == a) sum = sum.add(payments[i].getAmount());
        return sum;
    }

    public BigDecimal penaltiesFor(TaxObligation o) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < penaltyCount; i++) if (penalties[i].getObligation() == o) sum = sum.add(penalties[i].getPenaltyAmount());
        return sum;
    }

    public BigDecimal refundedFor(TaxAssessment a) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i].getAssessment() == a && refunds[i].getStatus().equals("APPROVED")) sum = sum.add(refunds[i].getRequestedAmount());
        }
        return sum;
    }

    /** What the taxpayer owes for this assessment: total + penalties (before payments). */
    public BigDecimal dueFor(TaxAssessment a) {
        return a.getTotal().add(penaltiesFor(a.getDeclaration().getObligation()));
    }

    /** Net payments = payments - approved refunds. */
    private BigDecimal netPaid(TaxAssessment a) { return paid(a).subtract(refundedFor(a)); }

    /** Balance = assessment total + penalties - confirmed payments (never below zero). */
    public BigDecimal outstanding(TaxAssessment a) {
        BigDecimal b = dueFor(a).subtract(netPaid(a));
        return b.signum() > 0 ? b : BigDecimal.ZERO;
    }

    /** Verified overpayment = what was paid beyond what is due (never below zero). */
    public BigDecimal overpayment(TaxAssessment a) {
        BigDecimal o = netPaid(a).subtract(dueFor(a));
        return o.signum() > 0 ? o : BigDecimal.ZERO;
    }

    /** Returns null if the taxpayer may get a clearance, otherwise the reason (text). */
    public String clearanceProblem(RegisteredTaxpayer tp) {
        if (!tp.isActive()) return "taxpayer " + tp.getTin() + " is not ACTIVE.";
        for (int i = 0; i < obligationCount; i++) {
            TaxObligation o = obligations[i];
            if (o.getTaxpayer() != tp) continue;
            boolean overdue = o.getDueDate().isBefore(LocalDate.now());
            TaxDeclaration d = activeDeclarationFor(o);
            if (d == null) {
                if (overdue) return "obligation " + o.getId() + " is overdue and has no declaration.";
                continue;
            }
            TaxAssessment a = confirmedAssessmentFor(d);
            if (a == null) return "declaration " + d.getId() + " is unresolved (no CONFIRMED assessment yet).";
            if (overdue && outstanding(a).signum() > 0) {
                return "obligation " + o.getId() + " is overdue with an unpaid balance of " + outstanding(a) + ".";
            }
        }
        return null;
    }

    // ------------------------------------------------------------ everything as one Identifiable[]
    private int copy(Identifiable[] target, int pos, Identifiable[] source, int count) {
        for (int i = 0; i < count; i++) target[pos++] = source[i];
        return pos;
    }

    public Identifiable[] everything() {
        int total = individualCount + corporateCount + officerCount + taxTypeCount + obligationCount + declarationCount
                + assessmentCount + paymentCount + penaltyCount + refundCount + clearanceCount;
        Identifiable[] all = new Identifiable[total];
        int p = 0;
        p = copy(all, p, individuals, individualCount);
        p = copy(all, p, corporates, corporateCount);
        p = copy(all, p, officers, officerCount);
        p = copy(all, p, taxTypes, taxTypeCount);
        p = copy(all, p, obligations, obligationCount);
        p = copy(all, p, declarations, declarationCount);
        p = copy(all, p, assessments, assessmentCount);
        p = copy(all, p, payments, paymentCount);
        p = copy(all, p, penalties, penaltyCount);
        p = copy(all, p, refunds, refundCount);
        p = copy(all, p, clearances, clearanceCount);
        return all;
    }
}
