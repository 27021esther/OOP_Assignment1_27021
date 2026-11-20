package com.auca.penalty;

import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import com.auca.util.Input;
import com.auca.util.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Late-filing penalty (illustrative 5% - NOT a legal rate). One per obligation and type. */
public class Penalty implements Identifiable, Validatable {
    private String penaltyId;
    private TaxObligation obligation;
    private String penaltyType;
    private BigDecimal baseAmount;
    private BigDecimal penaltyRate;
    private BigDecimal penaltyAmount;

    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType,
                   BigDecimal baseAmount, BigDecimal penaltyRate, BigDecimal penaltyAmount) {
        this.penaltyId = penaltyId;
        this.obligation = obligation;
        this.penaltyType = penaltyType;
        this.baseAmount = baseAmount;
        this.penaltyRate = penaltyRate;
        this.penaltyAmount = penaltyAmount;
    }

    /** penalty = baseAmount x rate / 100, rounded HALF_UP to 2 decimals. */
    public static BigDecimal calculate(BigDecimal baseAmount, BigDecimal rate) {
        return baseAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public TaxObligation getObligation() { return obligation; }
    public String getPenaltyType() { return penaltyType; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }

    @Override public String getId() { return penaltyId; }
    @Override public String getDisplayName() { return "Penalty " + penaltyId + " (" + penaltyType + ")"; }
    @Override public boolean isActive() { return true; }

    @Override
    public void validate() throws ValidationException {
        Validator.id(penaltyId, "Penalty ID", 3, 30);
        Validator.notNull(obligation, "Obligation");
        Validator.oneOf(penaltyType, "Penalty type", "LATE_FILING");
        Validator.checkMoney(baseAmount, "Base amount", false);
        Validator.checkRate(penaltyRate, "Penalty rate");
        Validator.checkMoney(penaltyAmount, "Penalty amount", false);
        BigDecimal expected = calculate(baseAmount, penaltyRate);
        if (penaltyAmount.compareTo(expected) != 0) {
            throw new InvalidTaxCalculationException("Penalty amount " + penaltyAmount + " is wrong. Expected " + expected + ".");
        }
    }

    @Override
    public void printDetails() {
        System.out.println("PENALTY");
        Input.show("Penalty ID", penaltyId);
        Input.show("Obligation", obligation.getId());
        Input.show("Penalty type", penaltyType);
        Input.show("Base amount", baseAmount);
        Input.show("Penalty rate (%)", penaltyRate);
        Input.show("Penalty amount", penaltyAmount);
    }

    @Override
    public String getSummary() {
        return "Penalty " + penaltyId + " | obligation " + obligation.getId() + " | " + penaltyType
                + " | " + baseAmount + " x " + penaltyRate + "% = " + penaltyAmount;
    }
}
