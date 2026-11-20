package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxtype.TaxType;
import com.auca.util.Input;

/**
 * The 4-step smart form used by every "create" option.
 * Step 1-2(3): type the fields.  Then VALIDATION/CALCULATIONS (if needed) and REVIEW/SAVE.
 * Typing BACK goes to the previous step, CANCEL throws the form away (nothing is saved).
 * While typing you may also type a field NUMBER to change that field.
 * The values live in ONE String[] until SAVE, so nothing is stored by a cancelled form.
 */
public class Wizard {

    /** Runs the form. Returns the saved object, or null if the user cancelled. */
    public static Object run(int kind, DataStore store) {
        String[][] spec = Rules.spec(kind);
        int[][] steps = Rules.steps(kind);
        String[] stepTitles = Rules.stepTitles(kind);
        String[] v = new String[spec.length];
        for (int i = 0; i < v.length; i++) {
            if (!spec[i][2].isEmpty()) v[i] = spec[i][2]; // default value
        }
        int inputSteps = steps.length;
        int step = 0; // 0,1,2,3 -> STEP 1..4

        while (true) {
            if (step < inputSteps) {
                header(kind, step + 1, stepTitles[step]);
                int r = inputStep(kind, steps[step], v, store);
                if (r == 2) return cancelled();
                if (r == 1) {
                    if (step > 0) step--; else System.out.println("  You are already at the first step.");
                } else {
                    step++;
                }
            } else if (step == 2) { // only for forms with 2 input steps: validation step
                header(kind, 3, "VALIDATION / CALCULATIONS");
                try {
                    Rules.revalidateAll(kind, v, store);
                    Rules.printCalculations(kind, v, store);
                    System.out.println("  All checks passed.");
                    String c = Input.ask("NEXT / BACK / CANCEL : ").toUpperCase();
                    if (c.equals("CANCEL")) return cancelled();
                    if (c.equals("BACK")) step = inputSteps - 1; else step = 3;
                } catch (ValidationException e) {
                    System.out.println("  ! PROBLEM: " + e.getMessage());
                    System.out.println("  Going back so you can fix it (type the field number to change it).");
                    step = inputSteps - 1;
                }
            } else { // step 4: review and save
                header(kind, 4, "REVIEW ALL " + spec.length + " FIELDS");
                try {
                    Rules.revalidateAll(kind, v, store);
                } catch (ValidationException e) {
                    System.out.println("  ! PROBLEM: " + e.getMessage() + "  (choose EDIT FIELD to fix it)");
                }
                for (int i = 0; i < spec.length; i++) printField(i, spec, v);
                String c = Input.ask("[1] SAVE  [2] EDIT FIELD  [0] CANCEL  (or BACK) : ").toUpperCase();
                if (c.equals("0") || c.equals("CANCEL")) return cancelled();
                if (c.equals("BACK")) {
                    step = inputSteps - 1;
                } else if (c.equals("2")) {
                    editByNumber(kind, v, store);
                } else if (c.equals("1")) {
                    try {
                        Rules.revalidateAll(kind, v, store);          // revalidate everything at SAVE time
                        Object saved = Rules.build(kind, v, store);
                        store.save(saved);
                        System.out.println("  SAVED successfully.");
                        return saved;
                    } catch (ValidationException e) {
                        System.out.println("  ! NOT SAVED: " + e.getMessage());
                    }
                } else {
                    System.out.println("  Please type 1, 2 or 0.");
                }
            }
        }
    }

    private static Object cancelled() {
        System.out.println("  Form cancelled. Nothing was saved and no record or balance changed.");
        return null;
    }

    private static void header(int kind, int step, String title) {
        System.out.println();
        System.out.println("+------------------ " + Rules.title(kind) + " ------------------+");
        System.out.println("| STEP " + step + " OF 4 - " + title);
        System.out.println("+------------------------------------------------------------+");
    }

    private static void printField(int i, String[][] spec, String[] v) {
        String shown = v[i] == null ? "(empty)" : v[i];
        System.out.printf("%02d %-20s [%s] : %s%n", i + 1, spec[i][0], spec[i][1], shown);
    }

    /** Asks the fields of one step, then lets the user NEXT / BACK / CANCEL / edit by number. */
    private static int inputStep(int kind, int[] range, String[] v, DataStore store) {
        String[][] spec = Rules.spec(kind);
        for (int i = range[0]; i <= range[1]; i++) {
            int r = askField(kind, i, v, store);
            if (r != 0) return r;
        }
        while (true) {
            System.out.println("  --- values of this step ---");
            for (int i = range[0]; i <= range[1]; i++) printField(i, spec, v);
            String cmd = Input.ask("NEXT / BACK / CANCEL (or a field number to change) : ").toUpperCase();
            if (cmd.equals("NEXT")) return 0;
            if (cmd.equals("BACK")) return 1;
            if (cmd.equals("CANCEL")) return 2;
            try {
                int idx = Integer.parseInt(cmd) - 1;
                if (idx < 0 || idx >= spec.length) {
                    System.out.println("  There is no field " + cmd + ".");
                } else if (askField(kind, idx, v, store) == 2) {
                    return 2;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Unknown command '" + cmd + "'. Type NEXT, BACK, CANCEL or a field number.");
            }
        }
    }

    private static void editByNumber(int kind, String[] v, DataStore store) {
        String c = Input.ask("Field number to edit : ");
        try {
            int idx = Integer.parseInt(c) - 1;
            if (idx < 0 || idx >= v.length) System.out.println("  There is no field " + c + ".");
            else askField(kind, idx, v, store);
        } catch (NumberFormatException e) {
            System.out.println("  '" + c + "' is not a number.");
        }
    }

    /**
     * Asks ONE field until it is valid. Returns 0 = ok, 1 = BACK typed, 2 = CANCEL typed.
     * Pressing ENTER keeps the current/default value. Only the invalid field is asked again.
     */
    private static int askField(int kind, int idx, String[] v, DataStore store) {
        String[] row = Rules.spec(kind)[idx];
        if (row[3].equals("AUTO")) {
            System.out.printf("%02d %-20s [%s] : (calculated automatically)%n", idx + 1, row[0], row[1]);
            return 0;
        }
        while (true) {
            String current = v[idx] == null ? "" : "  (current: " + v[idx] + ", ENTER keeps it)";
            String text = Input.ask(String.format("%02d %-20s [%s]%s : ", idx + 1, row[0], row[1], current));
            if (text.equalsIgnoreCase("CANCEL")) return 2;
            if (text.equalsIgnoreCase("BACK")) return 1;
            if (text.isEmpty()) {
                if (v[idx] != null) return 0;
                System.out.println("  ! " + row[0] + " cannot be blank.");
                continue;
            }
            try {
                v[idx] = Rules.checkField(kind, idx, text, v, store);
                return 0;
            } catch (RecordNotFoundException e) {
                System.out.println("  ! " + e.getMessage());
                int c = offerPrerequisite(row[3], idx, v, store);
                if (c == 2) return 2;
                if (c == 1) return 0;
            } catch (ValidationException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    /** A referenced record is missing. Returns 0 = ask again, 1 = prerequisite saved and filled in, 2 = cancel. */
    private static int offerPrerequisite(String rule, int idx, String[] v, DataStore store) {
        System.out.println("  Prerequisite missing:  1 = Register prerequisite now   2 = Re-enter value   0 = Cancel this form");
        String c = Input.ask("  Choose : ");
        if (c.equals("0")) return 2;
        if (!c.equals("1")) return 0;
        Object saved = null;
        if (rule.equals("REF_TAXPAYER")) {
            String t = Input.ask("  1 = Individual   2 = Corporate : ");
            if (t.equals("1")) saved = run(Rules.INDIVIDUAL, store);
            else if (t.equals("2")) saved = run(Rules.CORPORATE, store);
        } else if (rule.equals("REF_TAXTYPE")) {
            saved = run(Rules.TAXTYPE, store);
        } else if (rule.equals("REF_OBLIGATION")) {
            saved = run(Rules.OBLIGATION, store);
        } else if (rule.equals("REF_DECLARATION")) {
            saved = run(Rules.DECLARATION, store);
        } else if (rule.equals("REF_ASSESSMENT")) {
            saved = run(Rules.ASSESSMENT, store);
        } else if (rule.equals("REF_OFFICER_OPT")) {
            saved = run(Rules.OFFICER, store);
        }
        if (saved == null) {
            System.out.println("  Prerequisite was not saved. Back to your original form (your values are kept).");
            return 0;
        }
        v[idx] = keyOf(saved);
        System.out.println("  Prerequisite saved. Back to your original form with " + v[idx] + " filled in.");
        return 1;
    }

    /** The value that other forms use to point at a record (TIN, tax code, staff number or ID). */
    private static String keyOf(Object saved) {
        if (saved instanceof RegisteredTaxpayer) return ((RegisteredTaxpayer) saved).getTin();
        if (saved instanceof TaxType) return ((TaxType) saved).getTaxCode();
        if (saved instanceof RevenueOfficer) return ((RevenueOfficer) saved).getStaffNumber();
        return ((Identifiable) saved).getId();
    }
}
