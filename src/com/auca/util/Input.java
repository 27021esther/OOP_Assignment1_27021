package com.auca.util;

import java.util.Scanner;

/** Keeps the ONE shared Scanner of the whole program. Always reads with nextLine(). */
public class Input {
    private static final Scanner SCANNER = new Scanner(System.in);

    /** Shows a prompt and returns the trimmed line typed by the user. */
    public static String ask(String prompt) {
        System.out.print(prompt);
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println("No more input. Program ends.");
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }

    /** Prints one "label : value" line (used by printDetails). */
    public static void show(String label, Object value) {
        System.out.printf("  %-26s: %s%n", label, value);
    }
}
