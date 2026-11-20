package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.*;
import com.auca.individualtaxpayer.IndividualTaxpayer;
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
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The "rule book" used by the form engine (Wizard).
 * Every form is described by a table: {label, Java type/range hint, default value, rule name}.
 * checkField() checks ONE field, crossCheck() checks the fields TOGETHER (and against saved records),
 * build() creates the real object.
 */
public class Rules {
    public static final int INDIVIDUAL = 0, CORPORATE = 1, OFFICER = 2, TAXTYPE = 3, OBLIGATION = 4,
            DECLARATION = 5, ASSESSMENT = 6, PAYMENT = 7, PENALTY = 8, REFUND = 9, CLEARANCE = 10;

    // ---- the field tables ----------------------------------------------------
    private static final String[][] PARTY = {
        {"Party ID", "String, unique, 4-30 letters/digits/-/_", "", "PARTYID"},
        {"First name", "String, 2-60 characters", "", "NAME"},
        {"Last name", "String, 2-60 characters", "", "NAME"},
        {"Email", "String, valid email, max 120", "", "EMAIL"},
        {"Phone", "String, 10-15 digits, optional +", "", "PHONE"},
        {"Date of birth", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"}};
    private static final String[][] REGISTERED = {
        {"TIN", "String, exactly 9 digits, unique", "", "TIN"},
        {"Registration date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Taxpayer status", "String: ACTIVE / SUSPENDED / CLOSED", "ACTIVE", "TPSTATUS"},
        {"District", "String, 2-60 characters", "", "TEXT"},
        {"Sector", "String, 2-60 characters", "", "TEXT"},
        {"Tax office", "String, 2-60 characters", "", "TEXT"}};
    private static final String[][] INDIVIDUAL_ROWS = {
        {"National ID", "String, exactly 16 digits, unique", "", "NATID"},
        {"Occupation", "String, 2-60 characters", "", "TEXT"},
        {"Business name", "String, 2-120 characters", "", "BUSNAME"},
        {"Business start date", "LocalDate, YYYY-MM-DD", "", "PASTDATE"},
        {"Annual turnover", "BigDecimal, >= 0, 2 decimals", "", "MONEY"},
        {"Resident", "boolean: YES / NO", "YES", "YESNO"}};
    private static final String[][] CORPORATE_ROWS = {
        {"Company reg. no.", "String, 5-30 A-Z 0-9 -, unique", "", "REGNO"},
        {"Legal name", "String, 3-120 characters", "", "LEGALNAME"},
        {"Incorporation date", "LocalDate, YYYY-MM-DD", "", "PASTDATE"},
        {"Business sector", "String, 2-60 characters", "", "TEXT"},
        {"Authorized rep.", "String, 2-60 characters", "", "TEXT"},
        {"Annual turnover", "BigDecimal, >= 0, 2 decimals", "", "MONEY"}};
    private static final String[][] OFFICER_ROWS = {
        {"Staff number", "String, 3-20 A-Z 0-9 -, unique", "", "STAFFNO"},
        {"Department", "String, 2-60 characters", "", "TEXT"},
        {"Role", "String, 2-60 characters", "", "TEXT"},
        {"Hire date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Approval limit", "BigDecimal, >= 0, 2 decimals", "", "MONEY"},
        {"Active", "boolean: YES / NO", "YES", "YESNO"}};
    private static final String[][] TAXTYPE_ROWS = {
        {"Tax type ID", "String, unique, 3-30", "", "UID"},
        {"Tax code", "String, unique, 2-10 A-Z 0-9", "", "TAXCODE"},
        {"Tax name", "String, 3-60 characters", "", "TAXNAME"},
        {"Rate (%)", "BigDecimal, 0 to 100", "", "RATE"},
        {"Filing frequency", "String: MONTHLY / QUARTERLY / ANNUAL", "", "FREQ"},
        {"Active", "boolean: YES / NO", "YES", "YESNO"}};
    private static final String[][] OBLIGATION_ROWS = {
        {"Obligation ID", "String, unique, 3-30", "", "UID"},
        {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "", "REF_TAXPAYER"},
        {"Tax code", "TaxType, existing tax code", "", "REF_TAXTYPE"},
        {"Period start", "LocalDate, YYYY-MM-DD", "", "DATE"},
        {"Period end", "LocalDate, YYYY-MM-DD, >= start", "", "DATE"},
        {"Due date", "LocalDate, YYYY-MM-DD, >= period end", "", "DATE"}};
    private static final String[][] DECLARATION_ROWS = {
        {"Declaration ID", "String, unique, 3-30", "", "UID"},
        {"Obligation ID", "TaxObligation, existing ID", "", "REF_OBLIGATION"},
        {"Filing date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Taxable amount", "BigDecimal, >= 0, 2 decimals", "", "MONEY"},
        {"Declared tax", "BigDecimal, = taxable x rate / 100", "", "MONEY"},
        {"Status", "String, calculated: SUBMITTED or LATE", "AUTO", "AUTO"}};
    private static final String[][] ASSESSMENT_ROWS = {
        {"Assessment ID", "String, unique, 3-30", "", "UID"},
        {"Declaration ID", "TaxDeclaration, existing ID", "", "REF_DECLARATION"},
        {"Assessed tax", "BigDecimal, >= 0, 2 decimals", "", "MONEY"},
        {"Adjustment amount", "BigDecimal, may be negative", "0", "ADJ"},
        {"Assessment date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Status", "String, starts as DRAFT", "DRAFT", "AUTO"}};
    private static final String[][] PAYMENT_ROWS = {
        {"Payment ID", "String, unique, 3-30", "", "UID"},
        {"Assessment ID", "TaxAssessment, existing CONFIRMED ID", "", "REF_ASSESSMENT"},
        {"Payment reference", "String, globally unique, 4-30", "", "PAYREF"},
        {"Amount", "BigDecimal, > 0, <= outstanding", "", "MONEYPOS"},
        {"Payment date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Payment method", "String: CASH/MOBILE_MONEY/BANK_TRANSFER/CARD", "", "METHOD"}};
    private static final String[][] PENALTY_ROWS = {
        {"Penalty ID", "String, unique, 3-30", "", "UID"},
        {"Obligation ID", "TaxObligation, existing ID", "", "REF_OBLIGATION"},
        {"Penalty type", "String: LATE_FILING", "LATE_FILING", "PENTYPE"},
        {"Base amount", "BigDecimal, >= 0 (usually declared tax)", "", "MONEY"},
        {"Penalty rate (%)", "BigDecimal, 0-100 (illustrative 5)", "5", "RATE"},
        {"Penalty amount", "BigDecimal, calculated", "AUTO", "AUTO"}};
    private static final String[][] REFUND_ROWS = {
        {"Refund ID", "String, unique, 3-30", "", "UID"},
        {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "", "REF_TAXPAYER"},
        {"Assessment ID", "TaxAssessment, existing ID", "", "REF_ASSESSMENT"},
        {"Requested amount", "BigDecimal, > 0, <= overpayment", "", "MONEYPOS"},
        {"Request date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Status", "String, starts as PENDING", "PENDING", "AUTO"}};
    private static final String[][] CLEARANCE_ROWS = {
        {"Clearance ID", "String, unique, 3-30", "", "UID"},
        {"Taxpayer TIN", "RegisteredTaxpayer, existing TIN", "", "REF_TAXPAYER"},
        {"Request date", "LocalDate, YYYY-MM-DD, not future", "", "PASTDATE"},
        {"Expiry date", "LocalDate, YYYY-MM-DD, after request", "", "DATE"},
        {"Status", "String: PENDING or APPROVED", "PENDING", "CLRSTATUS"},
        {"Approved by", "RevenueOfficer staff no. (NONE if pending)", "NONE", "REF_OFFICER_OPT"}};

    private static String[][] join(String[][] a, String[][] b) {
        String[][] r = new String[a.length + b.length][];
        for (int i = 0; i < a.length; i++) r[i] = a[i];
        for (int i = 0; i < b.length; i++) r[a.length + i] = b[i];
        return r;
    }

    public static String[][] spec(int kind) {
        switch (kind) {
            case INDIVIDUAL: return join(join(PARTY, REGISTERED), INDIVIDUAL_ROWS);
            case CORPORATE: return join(join(PARTY, REGISTERED), CORPORATE_ROWS);
            case OFFICER: return join(PARTY, OFFICER_ROWS);
            case TAXTYPE: return TAXTYPE_ROWS;
            case OBLIGATION: return OBLIGATION_ROWS;
            case DECLARATION: return DECLARATION_ROWS;
            case ASSESSMENT: return ASSESSMENT_ROWS;
            case PAYMENT: return PAYMENT_ROWS;
            case PENALTY: return PENALTY_ROWS;
            case REFUND: return REFUND_ROWS;
            default: return CLEARANCE_ROWS;
        }
    }

    /** First and last field number (0-based) of each INPUT step. */
    public static int[][] steps(int kind) {
        switch (kind) {
            case INDIVIDUAL: case CORPORATE: return new int[][] {{0, 5}, {6, 11}, {12, 17}};
            case OFFICER: return new int[][] {{0, 5}, {6, 11}};
            case TAXTYPE: case OBLIGATION: case REFUND: case CLEARANCE: return new int[][] {{0, 2}, {3, 5}};
            default: return new int[][] {{0, 1}, {2, 5}};
        }
    }

    public static String[] stepTitles(int kind) {
        switch (kind) {
            case INDIVIDUAL: return new String[] {"PERSONAL DETAILS", "TAX REGISTRATION DETAILS", "BUSINESS DETAILS"};
            case CORPORATE: return new String[] {"CONTACT PERSON DETAILS", "TAX REGISTRATION DETAILS", "COMPANY DETAILS"};
            case OFFICER: return new String[] {"PERSONAL DETAILS", "OFFICER DETAILS"};
            default: return new String[] {"IDENTIFICATION / PREREQUISITES", "DETAILS"};
        }
    }

    public static String title(int kind) {
        String[] t = {"REGISTER INDIVIDUAL TAXPAYER", "REGISTER CORPORATE TAXPAYER", "REGISTER REVENUE OFFICER",
            "REGISTER TAX TYPE", "CREATE TAX OBLIGATION", "FILE TAX DECLARATION", "ASSESS DECLARATION",
            "RECORD TAX PAYMENT", "RECORD PENALTY", "REQUEST TAX REFUND", "REQUEST TAX CLEARANCE"};
        return t[kind];
    }

    private static LocalDate d(String s) throws ValidationException { return Validator.date(s, "Date"); }
    private static BigDecimal m(String s) throws ValidationException { return Validator.money(s, "Amount", true); }

    // ---- check ONE field --------------------------------------------------------
    /** Returns the cleaned value or throws an exception that explains the problem. */
    public static String checkField(int kind, int idx, String value, String[] v, DataStore s) throws ValidationException {
        String label = spec(kind)[idx][0];
        String rule = spec(kind)[idx][3];
        String x = value == null ? "" : value.trim();
        switch (rule) {
            case "PARTYID":
                x = Validator.id(x, label, 4, 30);
                if (s.partyIdExists(x)) throw new DuplicateRecordException("Party ID " + x + " is already used.");
                return x;
            case "NAME": return Validator.text(x, label, 2, 60);
            case "EMAIL": return Validator.email(x);
            case "PHONE": return Validator.phone(x);
            case "TEXT": return Validator.text(x, label, 2, 60);
            case "BUSNAME": return Validator.text(x, label, 2, 120);
            case "LEGALNAME": return Validator.text(x, label, 3, 120);
            case "TAXNAME": return Validator.text(x, label, 3, 60);
            case "PASTDATE": Validator.pastDate(x, label); return x;
            case "DATE": Validator.date(x, label); return x;
            case "TIN":
                x = Validator.digits(x, label, 9);
                if (s.findTaxpayerOrNull(x) != null) throw new DuplicateRecordException("TIN " + x + " is already registered (TINs are unique across individuals and companies).");
                return x;
            case "NATID":
                x = Validator.digits(x, label, 16);
                if (s.nationalIdExists(x)) throw new DuplicateRecordException("National ID " + x + " is already registered.");
                return x;
            case "REGNO":
                x = Validator.pattern(x.toUpperCase(), label, 5, 30, "[A-Z0-9-]+", "uppercase letters, digits and -");
                if (s.regNoExists(x)) throw new DuplicateRecordException("Company registration number " + x + " is already registered.");
                return x;
            case "STAFFNO":
                x = Validator.pattern(x.toUpperCase(), label, 3, 20, "[A-Z0-9-]+", "uppercase letters, digits and -");
                if (s.staffExists(x)) throw new DuplicateRecordException("Staff number " + x + " is already used.");
                return x;
            case "TAXCODE":
                x = Validator.pattern(x.toUpperCase(), label, 2, 10, "[A-Z0-9]+", "uppercase letters and digits");
                if (s.taxCodeExists(x)) throw new DuplicateRecordException("Tax code " + x + " is already used.");
                return x;
            case "UID":
                x = Validator.id(x, label, 3, 30);
                if (s.idExists(kind, x)) throw new DuplicateRecordException(label + " " + x + " already exists.");
                return x;
            case "PAYREF":
                x = Validator.id(x, label, 4, 30).toUpperCase();
                if (s.paymentRefExists(x)) throw new DuplicateRecordException("Payment reference " + x + " was already used (references must be unique).");
                return x;
            case "TPSTATUS": return Validator.oneOf(x, label, "ACTIVE", "SUSPENDED", "CLOSED");
            case "FREQ": return Validator.oneOf(x, label, "MONTHLY", "QUARTERLY", "ANNUAL");
            case "METHOD": return Validator.oneOf(x, label, "CASH", "MOBILE_MONEY", "BANK_TRANSFER", "CARD");
            case "PENTYPE": return Validator.oneOf(x, label, "LATE_FILING");
            case "CLRSTATUS": return Validator.oneOf(x, label, "PENDING", "APPROVED");
            case "YESNO": return Validator.yesNo(x, label) ? "YES" : "NO";
            case "MONEY": return Validator.money(x, label, false).toPlainString();
            case "ADJ": return Validator.money(x, label, true).toPlainString();
            case "RATE": return Validator.rate(x, label).toPlainString();
            case "MONEYPOS": {
                BigDecimal amount = Validator.money(x, label, false);
                if (amount.signum() <= 0) throw new ValidationException(label + " must be greater than zero.");
                return amount.toPlainString();
            }
            case "REF_TAXPAYER": s.findTaxpayer(x); return x;
            case "REF_TAXTYPE": s.findTaxType(x); return x.toUpperCase();
            case "REF_OBLIGATION": s.findObligation(x); return x;
            case "REF_DECLARATION": s.findDeclaration(x); return x;
            case "REF_ASSESSMENT": s.findAssessment(x); return x;
            case "REF_OFFICER_OPT":
                if (x.equalsIgnoreCase("NONE")) return "NONE";
                s.findOfficer(x);
                return x.toUpperCase();
            default: return x; // AUTO fields
        }
    }

    // ---- check fields TOGETHER ------------------------------------------------------
    /** Re-checks EVERY field and then the cross-record rules. Used at step 3 and again when saving. */
    public static void revalidateAll(int kind, String[] v, DataStore s) throws ValidationException {
        String[][] spec = spec(kind);
        for (int i = 0; i < spec.length; i++) {
            if (v[i] == null) throw new ValidationException("Field " + (i + 1) + " (" + spec[i][0] + ") is still empty.");
            v[i] = checkField(kind, i, v[i], v, s);
        }
        crossCheck(kind, v, s);
    }

    /** Rules that involve several fields or other saved records. May fill calculated (AUTO) fields. */
    public static void crossCheck(int kind, String[] v, DataStore s) throws ValidationException {
        switch (kind) {
            case INDIVIDUAL: {
                LocalDate reg = d(v[7]);
                if (!d(v[5]).isBefore(reg)) throw new ValidationException("Date of birth must be before the registration date.");
                if (!d(v[15]).isBefore(reg)) throw new ValidationException("Business start date must be before the registration date.");
                break;
            }
            case CORPORATE: {
                LocalDate reg = d(v[7]);
                if (!d(v[5]).isBefore(reg)) throw new ValidationException("Contact person's date of birth must be before the registration date.");
                if (!d(v[14]).isBefore(reg)) throw new ValidationException("Incorporation date must be before the registration date.");
                break;
            }
            case OFFICER:
                if (!d(v[9]).isAfter(d(v[5]))) throw new ValidationException("Hire date must be after the date of birth.");
                break;
            case OBLIGATION: {
                RegisteredTaxpayer tp = s.findTaxpayer(v[1]);
                TaxType tt = s.findTaxType(v[2]);
                if (!tp.isActive()) throw new PrerequisiteMissingException("Taxpayer " + tp.getTin() + " is " + tp.getTaxpayerStatus() + " - an obligation needs an ACTIVE taxpayer.");
                if (!tt.isActive()) throw new PrerequisiteMissingException("Tax type " + tt.getTaxCode() + " is not active.");
                LocalDate start = d(v[3]), end = d(v[4]), due = d(v[5]);
                if (start.isAfter(end)) throw new InvalidTaxPeriodException("Period start " + start + " must be on or before period end " + end + ".");
                if (due.isBefore(end)) throw new InvalidTaxPeriodException("Due date " + due + " must not be before period end " + end + ".");
                if (s.overlapExists(tp, tt, start, end)) throw new DuplicateRecordException("This taxpayer already has an obligation for " + tt.getTaxCode() + " that overlaps " + start + " to " + end + ".");
                break;
            }
            case DECLARATION: {
                TaxObligation o = s.findObligation(v[1]);
                TaxDeclaration existing = s.activeDeclarationFor(o);
                if (existing != null) throw new DuplicateRecordException("Obligation " + o.getId() + " already has an active declaration (" + existing.getId() + ").");
                LocalDate filing = d(v[2]);
                if (filing.isBefore(o.getPeriodEnd())) throw new InvalidTaxPeriodException("Filing date " + filing + " cannot be before the period end " + o.getPeriodEnd() + ".");
                BigDecimal expected = o.getTaxType().calculateTax(m(v[3]));
                if (m(v[4]).subtract(expected).abs().compareTo(new BigDecimal("0.01")) > 0) {
                    throw new InvalidTaxCalculationException("Declared tax " + v[4] + " is wrong. Expected " + expected + " (" + v[3] + " x " + o.getTaxType().getRate() + " / 100).");
                }
                v[5] = filing.isAfter(o.getDueDate()) ? "LATE" : "SUBMITTED";
                break;
            }
            case ASSESSMENT: {
                TaxDeclaration decl = s.findDeclaration(v[1]);
                if (decl.getStatus().equals("REJECTED")) throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
                TaxAssessment live = s.liveAssessmentFor(decl);
                if (live != null) throw new DuplicateRecordException("Declaration " + decl.getId() + " already has assessment " + live.getId() + " (" + live.getStatus() + ").");
                if (m(v[2]).add(m(v[3])).signum() < 0) throw new ValidationException("Assessment total cannot be negative (" + v[2] + " + " + v[3] + ").");
                if (d(v[4]).isBefore(decl.getFilingDate())) throw new ValidationException("Assessment date cannot be before the filing date " + decl.getFilingDate() + ".");
                v[5] = "DRAFT";
                break;
            }
            case PAYMENT: {
                TaxAssessment a = s.findAssessment(v[1]);
                if (!a.getStatus().equals("CONFIRMED")) throw new PrerequisiteMissingException("Payments need a CONFIRMED assessment (this one is " + a.getStatus() + ").");
                BigDecimal outstanding = s.outstanding(a);
                if (m(v[3]).compareTo(outstanding) > 0) throw new OverpaymentException("Amount " + v[3] + " is more than the outstanding balance " + outstanding + ".");
                if (d(v[4]).isBefore(a.getAssessmentDate())) throw new ValidationException("Payment date cannot be before the assessment date " + a.getAssessmentDate() + ".");
                break;
            }
            case PENALTY: {
                TaxObligation o = s.findObligation(v[1]);
                if (s.penaltyExists(o, v[2])) throw new DuplicateRecordException("Obligation " + o.getId() + " already has a " + v[2] + " penalty.");
                TaxDeclaration decl = s.activeDeclarationFor(o);
                if (decl == null || !decl.isLate()) throw new PrerequisiteMissingException("A LATE_FILING penalty needs a late declaration for obligation " + o.getId() + ".");
                v[5] = Penalty.calculate(m(v[3]), Validator.rate(v[4], "Penalty rate")).toPlainString();
                break;
            }
            case REFUND: {
                RegisteredTaxpayer tp = s.findTaxpayer(v[1]);
                TaxAssessment a = s.findAssessment(v[2]);
                if (a.getDeclaration().getObligation().getTaxpayer() != tp) throw new ValidationException("Assessment " + a.getId() + " does not belong to taxpayer " + tp.getTin() + ".");
                if (!a.getStatus().equals("CONFIRMED")) throw new PrerequisiteMissingException("A refund needs a CONFIRMED assessment.");
                BigDecimal over = s.overpayment(a);
                if (over.signum() <= 0) throw new OverpaymentException("There is no verified overpayment on assessment " + a.getId() + ", so no refund can be requested.");
                if (m(v[3]).compareTo(over) > 0) throw new OverpaymentException("Requested " + v[3] + " is more than the verified overpayment " + over + ".");
                if (s.pendingRefundFor(a) != null) throw new DuplicateRecordException("A refund for assessment " + a.getId() + " is already pending.");
                v[5] = "PENDING";
                break;
            }
            case CLEARANCE: {
                RegisteredTaxpayer tp = s.findTaxpayer(v[1]);
                if (!d(v[3]).isAfter(d(v[2]))) throw new ValidationException("Expiry date must be after the request date.");
                if (v[4].equals("APPROVED")) {
                    if (v[5].equals("NONE")) throw new ValidationException("An APPROVED clearance needs an approving officer (enter a staff number).");
                    RevenueOfficer officer = s.findOfficer(v[5]);
                    if (!officer.isActive()) throw new UnauthorizedApprovalException("Officer " + officer.getStaffNumber() + " is not active.");
                    String problem = s.clearanceProblem(tp);
                    if (problem != null) throw new PrerequisiteMissingException("Clearance cannot be APPROVED: " + problem);
                } else {
                    v[5] = "NONE";
                }
                break;
            }
            default:
                break;
        }
    }

    /** Shows the calculations for step 3 (VALIDATION / CALCULATIONS). */
    public static void printCalculations(int kind, String[] v, DataStore s) throws ValidationException {
        switch (kind) {
            case DECLARATION: {
                TaxObligation o = s.findObligation(v[1]);
                System.out.println("  Rate from tax type   : " + o.getTaxType().getRate() + " %");
                System.out.println("  Expected tax         : " + v[3] + " x " + o.getTaxType().getRate() + " / 100 = " + o.getTaxType().calculateTax(m(v[3])));
                System.out.println("  Declared tax         : " + v[4] + " (difference allowed: 0.01)");
                System.out.println("  Due date " + o.getDueDate() + ", filing date " + v[2] + " -> status " + v[5]);
                break;
            }
            case ASSESSMENT:
                System.out.println("  Total = " + v[2] + " + " + v[3] + " = " + m(v[2]).add(m(v[3])) + "  (status will be DRAFT)");
                break;
            case PAYMENT: {
                TaxAssessment a = s.findAssessment(v[1]);
                BigDecimal before = s.outstanding(a);
                System.out.println("  Outstanding balance now   : " + before + "  (total " + a.getTotal() + " + penalties - payments)");
                System.out.println("  Balance after this payment: " + before.subtract(m(v[3])));
                break;
            }
            case PENALTY:
                System.out.println("  Penalty = " + v[3] + " x " + v[4] + " / 100 = " + v[5] + " (HALF_UP, 2 decimals)");
                break;
            case REFUND:
                System.out.println("  Verified overpayment: " + s.overpayment(s.findAssessment(v[2])) + "   Requested: " + v[3]);
                break;
            case CLEARANCE: {
                String problem = s.clearanceProblem(s.findTaxpayer(v[1]));
                System.out.println("  Clearance check: " + (problem == null ? "taxpayer is clean (active, no overdue unpaid obligations, nothing unresolved)." : problem));
                break;
            }
            case OBLIGATION:
                System.out.println("  Period " + v[3] + " to " + v[4] + ", due " + v[5] + ": no overlap with existing obligations.");
                break;
            default:
                System.out.println("  Every field and cross-record check passed.");
        }
    }

    // ---- create the real object ------------------------------------------------------
    public static Object build(int kind, String[] v, DataStore s) throws ValidationException {
        Object o;
        switch (kind) {
            case INDIVIDUAL:
                o = new IndividualTaxpayer(v[0], v[1], v[2], v[3], v[4], d(v[5]), v[6], d(v[7]), v[8], v[9], v[10], v[11],
                        v[12], v[13], v[14], d(v[15]), m(v[16]), v[17].equals("YES"));
                break;
            case CORPORATE:
                o = new CorporateTaxpayer(v[0], v[1], v[2], v[3], v[4], d(v[5]), v[6], d(v[7]), v[8], v[9], v[10], v[11],
                        v[12], v[13], d(v[14]), v[15], v[16], m(v[17]));
                break;
            case OFFICER:
                o = new RevenueOfficer(v[0], v[1], v[2], v[3], v[4], d(v[5]), v[6], v[7], v[8], d(v[9]), m(v[10]), v[11].equals("YES"));
                break;
            case TAXTYPE:
                o = new TaxType(v[0], v[1], v[2], Validator.rate(v[3], "Rate"), v[4], v[5].equals("YES"));
                break;
            case OBLIGATION:
                o = new TaxObligation(v[0], s.findTaxpayer(v[1]), s.findTaxType(v[2]), d(v[3]), d(v[4]), d(v[5]));
                break;
            case DECLARATION:
                o = new TaxDeclaration(v[0], s.findObligation(v[1]), d(v[2]), m(v[3]), m(v[4]), v[5]);
                break;
            case ASSESSMENT:
                o = new TaxAssessment(v[0], s.findDeclaration(v[1]), m(v[2]), m(v[3]), d(v[4]), v[5]);
                break;
            case PAYMENT:
                o = new TaxPayment(v[0], s.findAssessment(v[1]), v[2], m(v[3]), d(v[4]), v[5]);
                break;
            case PENALTY:
                o = new Penalty(v[0], s.findObligation(v[1]), v[2], m(v[3]), Validator.rate(v[4], "Penalty rate"), m(v[5]));
                break;
            case REFUND:
                o = new TaxRefund(v[0], s.findTaxpayer(v[1]), s.findAssessment(v[2]), m(v[3]), d(v[4]), v[5]);
                break;
            default: {
                RevenueOfficer officer = v[5].equals("NONE") ? null : s.findOfficer(v[5]);
                o = new TaxClearance(v[0], s.findTaxpayer(v[1]), d(v[2]), d(v[3]), v[4], officer);
            }
        }
        ((Validatable) o).validate();
        return o;
    }
}
