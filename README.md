# Tax Administration Management System (TAMS) - Java OOP practical assignment

Teaching simulation only: every rate, deadline and rule is FICTIONAL and is not real tax law.

## How to compile and run

You need a JDK (Java 11 or newer).

Linux / macOS:
    ./compile.sh        (compiles into the folder "out")
    ./run.sh            (starts the menu)

Windows (Command Prompt, inside this folder):
    compile.bat
    run.bat

By hand:
    javac -d out $(find src -name "*.java")      (Windows: dir /s /b src\*.java > sources.txt  then  javac -d out @sources.txt)
    java -cp out com.auca.main.Main

Automatic demo of the acceptance scenarios (types the answers for you):
    java -cp out com.auca.main.Main < demo_input.txt

## Package tree (src/com/auca/...)

    interfaces/          Identifiable, Validatable
    abstracts/           TaxParty (level 1), RegisteredTaxpayer (level 2)
    individualtaxpayer/  IndividualTaxpayer       corporatetaxpayer/  CorporateTaxpayer
    revenueofficer/      RevenueOfficer           taxtype/            TaxType
    taxobligation/       TaxObligation            taxdeclaration/     TaxDeclaration
    taxassessment/       TaxAssessment            taxpayment/         TaxPayment
    penalty/             Penalty                  taxrefund/          TaxRefund
    taxclearance/        TaxClearance
    exceptions/          the 9 custom exceptions (all extend ValidationException)
    main/                Main (menu) + helpers: DataStore (arrays), Wizard (4-step form), Rules (validation)
    util/                helpers: Input (the ONE Scanner), Validator (small reusable checks)

The 15 domain types are the 2 interfaces, 2 abstract classes and 11 concrete classes.
DataStore, Wizard, Rules, Input and Validator are helper classes, not domain types.

## How a form works (every "create" menu option)

Type each field. ENTER keeps the shown default/current value.
BACK = previous step, CANCEL = throw the form away (nothing is saved).
After a step you can type a FIELD NUMBER to change that field without losing the others.
Step 4 shows ALL fields; [1] SAVE re-validates everything before storing.
If a referenced record (taxpayer, tax type...) is missing you may register it right now, then your form continues.

## Quick scenario guide (used by demo_input.txt)

1. Option 1: first type a short TIN (rejected), then a 9-digit one. Option 2 with the same TIN is rejected.
2. Options 3, 4, 5: officer, tax type (rate 18), obligation. A period whose start is after its end is rejected.
3. Option 6: wrong declared tax is rejected; a second declaration for the same obligation is rejected;
   a filing after the due date becomes LATE. Option 13 then records the 5% late-filing penalty.
4. Options 7 and 8: assess and confirm. Option 12: two partial payments; a duplicate reference or an amount
   above the outstanding balance is rejected. Cancel a payment form and option 19 shows nothing changed.
5. Option 17 -> 9: a negative adjustment on a fully paid assessment creates a verified overpayment.
   Option 14: refund request, then approval (inactive officer or low approval limit is rejected).
6. Option 15: clearance is refused while a balance is unpaid; after settling it is APPROVED.
7. Options 16, 18, 19, 20, 21: search, statement, balances, reports, polymorphism.
