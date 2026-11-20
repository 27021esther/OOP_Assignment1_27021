package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Tax Administration Management System (TEACHING SIMULATION - all rates and rules are fictional).
 * Main only shows the menu and calls the other classes.
 */
public class 1Main {
    private static final DataStore store = new DataStore();

    public static void main(String[] args) {
        System.out.println("TAX ADMINISTRATION MANAGEMENT SYSTEM - teaching simulation, NOT real tax law.");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = Input.ask("Select option: ");
            try {
                switch (choice) {
                    case "1": Wizard.run(Rules.INDIVIDUAL, store); break;
                    case "2": Wizard.run(Rules.CORPORATE, store); break;
                    case "3": Wizard.run(Rules.OFFICER, store); break;
                    case "4": Wizard.run(Rules.TAXTYPE, store); break;
                    case "5": Wizard.run(Rules.OBLIGATION, store); break;
                    case "6": Wizard.run(Rules.DECLARATION, store); break;
                    case "7": assessDeclaration(); break;
                    case "8": confirmAssessment(); break;
                    case "9": viewAll(); break;
                    case "10": declarationHistory(); break;
                    case "11": assessmentHistory(); break;
                    case "12": Wizard.run(Rules.PAYMENT, store); break;
                    case "13": Wizard.run(Rules.PENALTY, store); break;
                    case "14": refundMenu(); break;
                    case "15": clearanceMenu(); break;
                    case "16": searchRecord(); break;
                    case "17": updateRecord(); break;
                    case "18": taxpayerStatement(); break;
                    case "19": outstandingBalances(); break;
                    case "20": taxReports(); break;
                    case "21": demonstratePolymorphism(); break;
                    case "0": running = false; break;
                    default: System.out.println("Unknown option '" + choice + "'. Please type a number from the menu.");
                }
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            } catch (NumberFormatException | DateTimeParseException e) {
                System.out.println("  ! Invalid number or date. Nothing was changed.");
            } catch (RuntimeException e) {
                System.out.println("  ! Unexpected problem (" + e.getClass().getSimpleName() + "). Nothing was changed; the menu continues.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("================ TAX ADMINISTRATION MANAGEMENT ================");
        System.out.println(" 1 Register Individual Taxpayer     12 Record Tax Payment");
        System.out.println(" 2 Register Corporate Taxpayer      13 Calculate / Record Penalty");
        System.out.println(" 3 Register Revenue Officer         14 Request / Approve Refund");
        System.out.println(" 4 Register Tax Type                15 Request / Issue Tax Clearance");
        System.out.println(" 5 Create Tax Obligation            16 Search Record by ID / TIN");
        System.out.println(" 6 File Tax Declaration             17 Update Eligible Record");
        System.out.println(" 7 Assess Declaration               18 View Taxpayer Statement");
        System.out.println(" 8 Confirm Assessment               19 View Outstanding Balances");
        System.out.println(" 9 View All Records                 20 View Tax Reports");
        System.out.println("10 View Declaration History         21 Demonstrate Polymorphism");
        System.out.println("11 View Assessment History           0 Exit");
    }

    // ------------------------------------------------------------ 7 and 8
    private static void assessDeclaration() throws ValidationException {
        String c = Input.ask("1 = Create assessment   2 = Reject a declaration   0 = Back : ");
        if (c.equals("1")) {
            Wizard.run(Rules.ASSESSMENT, store);
        } else if (c.equals("2")) {
            TaxDeclaration d = store.findDeclaration(Input.ask("Declaration ID : "));
            d.printDetails();
            if (store.liveAssessmentFor(d) != null) throw new InvalidStatusTransitionException("This declaration already has an assessment; it cannot be rejected now.");
            if (Input.ask("Reject this declaration? (YES/NO) : ").equalsIgnoreCase("YES")) {
                d.reject();
                System.out.println("  Declaration " + d.getId() + " is now REJECTED (it can no longer be assessed).");
            } else {
                System.out.println("  Cancelled. Nothing changed.");
            }
        } else if (!c.equals("0")) {
            System.out.println("  Unknown choice.");
        }
    }

    private static void confirmAssessment() throws ValidationException {
        TaxAssessment a = store.findAssessment(Input.ask("Assessment ID to confirm : "));
        a.printDetails();
        if (!Input.ask("Confirm this assessment? (YES/NO) : ").equalsIgnoreCase("YES")) {
            System.out.println("  Cancelled. Nothing changed.");
            return;
        }
        a.confirm();                       // throws if it is not DRAFT
        a.getDeclaration().markAssessed();
        System.out.println("  Assessment " + a.getId() + " is CONFIRMED. Total due: " + a.getTotal());
    }

    // ------------------------------------------------------------ 9, 10, 11
    private static void printGroup(String title, Validatable[] arr, int count) {
        System.out.println("--- " + title + " (" + count + ") ---");
        for (int i = 0; i < count; i++) System.out.println("  " + arr[i].getSummary());
    }

    private static void viewAll() {
        printGroup("Individual taxpayers", store.individuals, store.individualCount);
        printGroup("Corporate taxpayers", store.corporates, store.corporateCount);
        printGroup("Revenue officers", store.officers, store.officerCount);
        printGroup("Tax types", store.taxTypes, store.taxTypeCount);
        printGroup("Tax obligations", store.obligations, store.obligationCount);
        printGroup("Tax declarations", store.declarations, store.declarationCount);
        printGroup("Tax assessments", store.assessments, store.assessmentCount);
        printGroup("Tax payments", store.payments, store.paymentCount);
        printGroup("Penalties", store.penalties, store.penaltyCount);
        printGroup("Tax refunds", store.refunds, store.refundCount);
        printGroup("Tax clearances", store.clearances, store.clearanceCount);
    }

    private static void declarationHistory() throws ValidationException {
        String tin = Input.ask("Taxpayer TIN (blank = all taxpayers) : ");
        RegisteredTaxpayer tp = tin.isEmpty() ? null : store.findTaxpayer(tin);
        int found = 0;
        for (int i = 0; i < store.declarationCount; i++) {
            TaxDeclaration d = store.declarations[i];
            if (tp == null || d.getObligation().getTaxpayer() == tp) {
                System.out.println("  " + d.getSummary());
                found++;
            }
        }
        if (found == 0) System.out.println("  No declarations found.");
    }

    private static void assessmentHistory() throws ValidationException {
        String tin = Input.ask("Taxpayer TIN (blank = all taxpayers) : ");
        RegisteredTaxpayer tp = tin.isEmpty() ? null : store.findTaxpayer(tin);
        int found = 0;
        for (int i = 0; i < store.assessmentCount; i++) {
            TaxAssessment a = store.assessments[i];
            if (tp == null || a.getDeclaration().getObligation().getTaxpayer() == tp) {
                System.out.println("  " + a.getSummary());
                found++;
            }
        }
        if (found == 0) System.out.println("  No assessments found.");
    }

    // ------------------------------------------------------------ 14 and 15
    private static void refundMenu() throws ValidationException {
        String c = Input.ask("1 = Request a refund   2 = Approve / reject a pending refund   0 = Back : ");
        if (c.equals("1")) {
            Wizard.run(Rules.REFUND, store);
        } else if (c.equals("2")) {
            TaxRefund r = store.findRefund(Input.ask("Refund ID : "));
            r.printDetails();
            RevenueOfficer officer = store.findOfficer(Input.ask("Deciding officer staff number : "));
            String d = Input.ask("[1] Approve  [2] Reject  [0] Cancel : ");
            if (d.equals("1")) {
                r.approve(officer, store.overpayment(r.getAssessment()), LocalDate.now());
                System.out.println("  Refund " + r.getId() + " APPROVED by " + officer.getStaffNumber() + ".");
            } else if (d.equals("2")) {
                r.reject(officer, LocalDate.now());
                System.out.println("  Refund " + r.getId() + " REJECTED.");
            } else {
                System.out.println("  Cancelled. Nothing changed.");
            }
        } else if (!c.equals("0")) {
            System.out.println("  Unknown choice.");
        }
    }

    private static void clearanceMenu() throws ValidationException {
        String c = Input.ask("1 = Request / issue a clearance   2 = Decide a pending clearance   0 = Back : ");
        if (c.equals("1")) {
            Wizard.run(Rules.CLEARANCE, store);
        } else if (c.equals("2")) {
            TaxClearance cl = store.findClearance(Input.ask("Clearance ID : "));
            cl.printDetails();
            RevenueOfficer officer = store.findOfficer(Input.ask("Deciding officer staff number : "));
            String d = Input.ask("[1] Approve  [2] Reject  [0] Cancel : ");
            if (d.equals("1")) {
                cl.approve(officer, store.clearanceProblem(cl.getTaxpayer()));
                System.out.println("  Clearance " + cl.getId() + " APPROVED by " + officer.getStaffNumber() + ".");
            } else if (d.equals("2")) {
                cl.reject(officer);
                System.out.println("  Clearance " + cl.getId() + " REJECTED.");
            } else {
                System.out.println("  Cancelled. Nothing changed.");
            }
        } else if (!c.equals("0")) {
            System.out.println("  Unknown choice.");
        }
    }

    // ------------------------------------------------------------ 16
    private static void searchRecord() {
        String q = Input.ask("Type an ID, TIN, national ID, staff number, tax code or payment reference : ");
        if (q.isEmpty()) {
            System.out.println("  Nothing typed.");
            return;
        }
        Identifiable[] all = store.everything();
        int found = 0;
        for (int i = 0; i < all.length; i++) {
            String summary = ((Validatable) all[i]).getSummary().toUpperCase();
            if (all[i].getId().equalsIgnoreCase(q) || summary.contains(q.toUpperCase())) {
                ((Validatable) all[i]).printDetails();
                System.out.println();
                found++;
            }
        }
        if (found == 0) System.out.println("  No record matches '" + q + "'.");
        else System.out.println("  " + found + " record(s) found.");
    }

    // ------------------------------------------------------------ 17
    private static void updateRecord() throws ValidationException {
        System.out.println("1 Taxpayer email   2 Taxpayer phone   3 Taxpayer district   4 Taxpayer status");
        System.out.println("5 Tax type rate    6 Tax type active  7 Officer approval limit  8 Officer active");
        System.out.println("9 Assessment adjustment   10 Cancel an assessment   0 Back");
        String c = Input.ask("Update what? : ");
        switch (c) {
            case "1": updateField(store.findTaxpayer(Input.ask("Taxpayer TIN : ")), "EMAIL", "New email"); break;
            case "2": updateField(store.findTaxpayer(Input.ask("Taxpayer TIN : ")), "PHONE", "New phone"); break;
            case "3": updateField(store.findTaxpayer(Input.ask("Taxpayer TIN : ")), "DISTRICT", "New district"); break;
            case "4": updateField(store.findTaxpayer(Input.ask("Taxpayer TIN : ")), "STATUS", "New status (ACTIVE/SUSPENDED/CLOSED)"); break;
            case "5": updateField(store.findTaxType(Input.ask("Tax code : ")), "RATE", "New rate 0-100"); break;
            case "6": updateField(store.findTaxType(Input.ask("Tax code : ")), "TTACTIVE", "Active? YES/NO"); break;
            case "7": updateField(store.findOfficer(Input.ask("Staff number : ")), "LIMIT", "New approval limit"); break;
            case "8": updateField(store.findOfficer(Input.ask("Staff number : ")), "OACTIVE", "Active? YES/NO"); break;
            case "9": updateField(store.findAssessment(Input.ask("Assessment ID : ")), "ADJ", "New adjustment amount (may be negative)"); break;
            case "10": {
                TaxAssessment a = store.findAssessment(Input.ask("Assessment ID : "));
                if (store.paid(a).signum() > 0) throw new InvalidStatusTransitionException("This assessment already has payments and cannot be cancelled.");
                a.cancel();
                System.out.println("  Assessment " + a.getId() + " is now CANCELLED.");
                break;
            }
            case "0": break;
            default: System.out.println("  Unknown choice.");
        }
    }

    /** Asks for the new value until it is valid (or CANCEL). The record changes only when the value is valid. */
    private static void updateField(Object target, String field, String prompt) {
        while (true) {
            String x = Input.ask(prompt + " (or CANCEL) : ");
            if (x.equalsIgnoreCase("CANCEL")) {
                System.out.println("  Cancelled. Nothing changed.");
                return;
            }
            try {
                applyUpdate(target, field, x);
                System.out.println("  Updated successfully.");
                return;
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    private static void applyUpdate(Object t, String field, String x) throws ValidationException {
        if (field.equals("EMAIL")) ((RegisteredTaxpayer) t).setEmail(x);
        else if (field.equals("PHONE")) ((RegisteredTaxpayer) t).setPhoneNumber(x);
        else if (field.equals("DISTRICT")) ((RegisteredTaxpayer) t).setDistrict(x);
        else if (field.equals("STATUS")) ((RegisteredTaxpayer) t).changeStatus(x);
        else if (field.equals("RATE")) ((TaxType) t).setRate(Validator.rate(x, "Rate"));
        else if (field.equals("TTACTIVE")) ((TaxType) t).setActive(Validator.yesNo(x, "Active"));
        else if (field.equals("LIMIT")) ((RevenueOfficer) t).setApprovalLimit(Validator.money(x, "Approval limit", false));
        else if (field.equals("OACTIVE")) ((RevenueOfficer) t).setActive(Validator.yesNo(x, "Active"));
        else if (field.equals("ADJ")) ((TaxAssessment) t).setAdjustmentAmount(Validator.money(x, "Adjustment", true));
    }

    // ------------------------------------------------------------ 18
    private static void taxpayerStatement() throws ValidationException {
        RegisteredTaxpayer tp = store.findTaxpayer(Input.ask("Taxpayer TIN : "));
        System.out.println("=== STATEMENT: " + tp.getSummary());
        BigDecimal assessed = BigDecimal.ZERO, penalties = BigDecimal.ZERO, paid = BigDecimal.ZERO, refunded = BigDecimal.ZERO;
        for (int i = 0; i < store.obligationCount; i++) {
            TaxObligation o = store.obligations[i];
            if (o.getTaxpayer() != tp) continue;
            System.out.println("Obligation : " + o.getSummary());
            for (int j = 0; j < store.declarationCount; j++) {
                TaxDeclaration d = store.declarations[j];
                if (d.getObligation() != o) continue;
                System.out.println("  Declaration : " + d.getSummary());
                for (int k = 0; k < store.assessmentCount; k++) {
                    TaxAssessment a = store.assessments[k];
                    if (a.getDeclaration() != d) continue;
                    System.out.println("    Assessment : " + a.getSummary());
                    if (a.getStatus().equals("CONFIRMED")) {
                        assessed = assessed.add(a.getTotal());
                        paid = paid.add(store.paid(a));
                        refunded = refunded.add(store.refundedFor(a));
                    }
                    for (int p = 0; p < store.paymentCount; p++) {
                        if (store.payments[p].getAssessment() == a) System.out.println("      Payment : " + store.payments[p].getSummary());
                    }
                }
            }
            for (int p = 0; p < store.penaltyCount; p++) {
                if (store.penalties[p].getObligation() == o) {
                    System.out.println("  Penalty : " + store.penalties[p].getSummary());
                    penalties = penalties.add(store.penalties[p].getPenaltyAmount());
                }
            }
        }
        for (int i = 0; i < store.refundCount; i++) {
            if (store.refunds[i].getAssessment().getDeclaration().getObligation().getTaxpayer() == tp) System.out.println("Refund : " + store.refunds[i].getSummary());
        }
        for (int i = 0; i < store.clearanceCount; i++) {
            if (store.clearances[i].getTaxpayer() == tp) System.out.println("Clearance : " + store.clearances[i].getSummary());
        }
        BigDecimal balance = assessed.add(penalties).subtract(paid.subtract(refunded));
        System.out.println("TOTAL confirmed assessments : " + assessed);
        System.out.println("TOTAL penalties             : " + penalties);
        System.out.println("TOTAL payments              : " + paid);
        System.out.println("TOTAL refunds paid back     : " + refunded);
        System.out.println("BALANCE (assessed + penalties - payments + refunds) : " + balance);
    }

    // ------------------------------------------------------------ 19 and 20
    private static void outstandingBalances() {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (int i = 0; i < store.assessmentCount; i++) {
            TaxAssessment a = store.assessments[i];
            if (!a.getStatus().equals("CONFIRMED")) continue;
            BigDecimal out = store.outstanding(a);
            if (out.signum() > 0) {
                RegisteredTaxpayer tp = a.getDeclaration().getObligation().getTaxpayer();
                System.out.println("  TIN " + tp.getTin() + " | " + a.getId() + " | total " + a.getTotal() + " | penalties "
                        + store.penaltiesFor(a.getDeclaration().getObligation()) + " | paid " + store.paid(a) + " | OUTSTANDING " + out);
                total = total.add(out);
                count++;
            }
        }
        if (count == 0) System.out.println("  No outstanding balances.");
        else System.out.println("  " + count + " assessment(s) with a balance. TOTAL OUTSTANDING: " + total);
    }

    private static void taxReports() {
        System.out.println("=== TAX REPORTS ===");
        System.out.println("Taxpayers: " + store.individualCount + " individual, " + store.corporateCount + " corporate | Officers: " + store.officerCount
                + " | Tax types: " + store.taxTypeCount + " | Obligations: " + store.obligationCount);
        int late = 0, rejected = 0;
        BigDecimal declared = BigDecimal.ZERO;
        for (int i = 0; i < store.declarationCount; i++) {
            TaxDeclaration d = store.declarations[i];
            declared = declared.add(d.getDeclaredTax());
            if (d.isLate()) late++;
            if (d.getStatus().equals("REJECTED")) rejected++;
        }
        System.out.println("Declarations: " + store.declarationCount + " (late " + late + ", rejected " + rejected + ") | total declared tax " + declared);
        BigDecimal assessed = BigDecimal.ZERO, outstanding = BigDecimal.ZERO, payments = BigDecimal.ZERO, penalties = BigDecimal.ZERO, refunds = BigDecimal.ZERO;
        for (int i = 0; i < store.assessmentCount; i++) {
            if (store.assessments[i].getStatus().equals("CONFIRMED")) {
                assessed = assessed.add(store.assessments[i].getTotal());
                outstanding = outstanding.add(store.outstanding(store.assessments[i]));
            }
        }
        for (int i = 0; i < store.paymentCount; i++) payments = payments.add(store.payments[i].getAmount());
        for (int i = 0; i < store.penaltyCount; i++) penalties = penalties.add(store.penalties[i].getPenaltyAmount());
        int pending = 0;
        for (int i = 0; i < store.refundCount; i++) {
            if (store.refunds[i].getStatus().equals("APPROVED")) refunds = refunds.add(store.refunds[i].getRequestedAmount());
            if (store.refunds[i].getStatus().equals("PENDING")) pending++;
        }
        System.out.println("Assessments: " + store.assessmentCount + " | confirmed total " + assessed);
        System.out.println("Payments: " + store.paymentCount + " | total received " + payments);
        System.out.println("Penalties: " + store.penaltyCount + " | total " + penalties);
        System.out.println("Refunds: " + store.refundCount + " (pending " + pending + ") | approved total " + refunds);
        System.out.println("Clearances: " + store.clearanceCount);
        System.out.println("TOTAL OUTSTANDING: " + outstanding);
        System.out.println("--- per tax type ---");
        for (int t = 0; t < store.taxTypeCount; t++) {
            int obl = 0;
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < store.obligationCount; i++) {
                if (store.obligations[i].getTaxType() == store.taxTypes[t]) {
                    obl++;
                    TaxDeclaration d = store.activeDeclarationFor(store.obligations[i]);
                    if (d != null) sum = sum.add(d.getDeclaredTax());
                }
            }
            System.out.println("  " + store.taxTypes[t].getDisplayName() + " : " + obl + " obligation(s), declared tax " + sum);
        }
    }

    // ------------------------------------------------------------ 21
    private static void demonstratePolymorphism() {
        System.out.println("Abstract classes TaxParty and RegisteredTaxpayer can NOT be created with 'new'.");
        System.out.println("One TaxParty[] array holds three DIFFERENT concrete types (stored records are used when they exist).");
        boolean sampleUsed = false;
        TaxParty[] parties = new TaxParty[3];
        if (store.individualCount > 0) parties[0] = store.individuals[0];
        else {
            parties[0] = new IndividualTaxpayer("DEMO-I1", "Alice", "Uwase", "alice@example.com", "0788000001", LocalDate.of(1990, 1, 1), "123456789",
                    LocalDate.of(2020, 1, 1), "ACTIVE", "Gasabo", "Kimironko", "Kigali Office", "1199080012345678", "Trader", "Alice Shop",
                    LocalDate.of(2019, 1, 1), new BigDecimal("1000000.00"), true);
            sampleUsed = true;
        }
        if (store.corporateCount > 0) parties[1] = store.corporates[0];
        else {
            parties[1] = new CorporateTaxpayer("DEMO-C1", "Bob", "Mugabo", "bob@example.com", "0788000002", LocalDate.of(1985, 5, 5), "987654321",
                    LocalDate.of(2020, 1, 1), "ACTIVE", "Nyarugenge", "Nyarugenge", "Kigali Office", "COMP-0001", "Demo Trading Ltd",
                    LocalDate.of(2018, 1, 1), "Retail", "Bob Mugabo", new BigDecimal("50000000.00"));
            sampleUsed = true;
        }
        if (store.officerCount > 0) parties[2] = store.officers[0];
        else {
            parties[2] = new RevenueOfficer("DEMO-O1", "Carine", "Ingabire", "carine@example.com", "0788000003", LocalDate.of(1988, 3, 3), "STAFF-001",
                    "Audit", "Senior Officer", LocalDate.of(2015, 1, 1), new BigDecimal("500000.00"), true);
            sampleUsed = true;
        }
        if (sampleUsed) System.out.println("(Sample objects marked DEMO-... are created in memory only and are NOT saved.)");
        for (int i = 0; i < parties.length; i++) {
            TaxParty p = parties[i];
            System.out.println("- " + p.getClass().getSimpleName() + " | role: " + p.getPartyRole() + " | display: " + p.getDisplayName() + " | active: " + p.isActive());
            System.out.println("  summary : " + p.getSummary());
            if (p instanceof RegisteredTaxpayer) System.out.println("  turnover: " + ((RegisteredTaxpayer) p).getAnnualTurnover());
            else if (p instanceof RevenueOfficer) System.out.println("  approval limit: " + ((RevenueOfficer) p).getApprovalLimit());
            try {
                p.validate();
                System.out.println("  validate(): OK");
            } catch (ValidationException e) {
                System.out.println("  validate(): " + e.getMessage());
            }
        }
        System.out.println("Now an Identifiable[] array mixing parties and other record types:");
        Identifiable[] ids = new Identifiable[11];
        int n = 0;
        ids[n++] = parties[0];
        ids[n++] = parties[1];
        ids[n++] = parties[2];
        if (store.taxTypeCount > 0) ids[n++] = store.taxTypes[0];
        if (store.obligationCount > 0) ids[n++] = store.obligations[0];
        if (store.declarationCount > 0) ids[n++] = store.declarations[0];
        if (store.assessmentCount > 0) ids[n++] = store.assessments[0];
        if (store.paymentCount > 0) ids[n++] = store.payments[0];
        if (store.penaltyCount > 0) ids[n++] = store.penalties[0];
        if (store.refundCount > 0) ids[n++] = store.refunds[0];
        if (store.clearanceCount > 0) ids[n++] = store.clearances[0];
        for (int i = 0; i < n; i++) {
            System.out.println("- " + ids[i].getClass().getSimpleName() + " | id " + ids[i].getId() + " | " + ids[i].getDisplayName() + " | active " + ids[i].isActive());
        }
    }
}
