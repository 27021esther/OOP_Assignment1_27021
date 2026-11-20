package com.auca.util;

import com.auca.exceptions.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Small reusable checks. Every method returns the clean value or throws ValidationException. */
public class Validator {
    public static final BigDecimal MAX_MONEY = new BigDecimal("999999999999.99");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public static String required(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " cannot be blank.");
        }
        return value.trim();
    }

    public static void notNull(Object value, String field) throws ValidationException {
        if (value == null) throw new ValidationException(field + " is missing (null reference).");
    }

    public static String text(String value, String field, int min, int max) throws ValidationException {
        String t = required(value, field);
        if (t.length() < min || t.length() > max) {
            throw new ValidationException(field + " must be " + min + "-" + max + " characters long.");
        }
        return t;
    }

    public static String digits(String value, String field, int exact) throws ValidationException {
        String t = required(value, field);
        if (!t.matches("\\d{" + exact + "}")) {
            throw new ValidationException(field + " must be exactly " + exact + " digits (numbers only).");
        }
        return t;
    }

    /** Checks length and a regular expression (for IDs, codes...). */
    public static String pattern(String value, String field, int min, int max, String regex, String help)
            throws ValidationException {
        String t = text(value, field, min, max);
        if (!t.matches(regex)) throw new ValidationException(field + " may only contain " + help + ".");
        return t;
    }

    public static String id(String value, String field, int min, int max) throws ValidationException {
        return pattern(value, field, min, max, "[A-Za-z0-9_-]+", "letters, digits, - and _");
    }

    public static String email(String value) throws ValidationException {
        String t = required(value, "Email");
        if (t.length() > 120) throw new ValidationException("Email must be at most 120 characters.");
        if (!t.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            throw new ValidationException("Email is not valid (example: name@example.com).");
        }
        return t;
    }

    public static String phone(String value) throws ValidationException {
        String t = required(value, "Phone");
        if (!t.matches("\\+?\\d{10,15}")) {
            throw new ValidationException("Phone must be 10-15 digits with an optional leading +.");
        }
        return t;
    }

    /** The value must be one of the allowed words (case does not matter). Returns it in UPPERCASE. */
    public static String oneOf(String value, String field, String... allowed) throws ValidationException {
        String t = required(value, field).toUpperCase();
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i].equals(t)) return t;
        }
        String list = "";
        for (int i = 0; i < allowed.length; i++) list += (i == 0 ? "" : ", ") + allowed[i];
        throw new ValidationException(field + " must be one of: " + list + ".");
    }

    public static boolean yesNo(String value, String field) throws ValidationException {
        String t = required(value, field).toUpperCase();
        if (t.equals("YES") || t.equals("Y")) return true;
        if (t.equals("NO") || t.equals("N")) return false;
        throw new ValidationException(field + " must be YES or NO.");
    }

    /** Strict YYYY-MM-DD. Impossible dates such as 2025-02-30 are rejected. */
    public static LocalDate date(String value, String field) throws ValidationException {
        String t = required(value, field);
        if (!t.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidationException(field + " must use the format YYYY-MM-DD.");
        }
        try {
            return LocalDate.parse(t, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " is not a real calendar date: " + t);
        }
    }

    public static LocalDate pastDate(String value, String field) throws ValidationException {
        LocalDate d = date(value, field);
        if (d.isAfter(LocalDate.now())) throw new ValidationException(field + " cannot be in the future.");
        return d;
    }

    /** Money: BigDecimal, max 2 decimals, up to 999999999999.99. */
    public static BigDecimal money(String value, String field, boolean allowNegative) throws ValidationException {
        String t = required(value, field);
        if (!t.matches("-?\\d+(\\.\\d+)?")) {
            throw new ValidationException(field + " must be a number such as 1500 or 1500.75.");
        }
        BigDecimal m;
        try {
            m = new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " is not a valid number.");
        }
        return checkMoney(m, field, allowNegative);
    }

    /** Same checks, but for a BigDecimal that already exists inside an object. */
    public static BigDecimal checkMoney(BigDecimal m, String field, boolean allowNegative)
            throws ValidationException {
        notNull(m, field);
        if (m.stripTrailingZeros().scale() > 2) throw new ValidationException(field + " allows at most 2 decimal places.");
        if (!allowNegative && m.signum() < 0) throw new ValidationException(field + " cannot be negative.");
        if (m.abs().compareTo(MAX_MONEY) > 0) throw new ValidationException(field + " is too large (max 999999999999.99).");
        return m.setScale(2);
    }

    /** A rate between 0 and 100 inclusive. */
    public static BigDecimal rate(String value, String field) throws ValidationException {
        return checkRate(money(value, field, false), field);
    }

    public static BigDecimal checkRate(BigDecimal r, String field) throws ValidationException {
        notNull(r, field);
        if (r.signum() < 0 || r.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException(field + " must be between 0 and 100.");
        }
        return r;
    }
}
