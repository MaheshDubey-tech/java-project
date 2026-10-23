package com.attendance.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility for polished console I/O, tabular printing, and color formatting.
 */
public class ConsoleUtils {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            printError("Value cannot be empty! Please try again.");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("Invalid integer number! Please try again.");
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) {
                return val;
            }
            printError(String.format("Please enter a number between %d and %d.", min, max));
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                printError("Invalid decimal number! Please try again.");
            }
        }
    }

    public static LocalDate readDate(String prompt, boolean allowTodayDefault) {
        while (true) {
            System.out.print(prompt + (allowTodayDefault ? " (Press Enter for today " + LocalDate.now() + "): " : ": "));
            String input = scanner.nextLine().trim();
            if (allowTodayDefault && input.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                printError("Invalid date format! Please enter in YYYY-MM-DD format (e.g., 2026-10-10).");
            }
        }
    }

    public static String readAttendanceStatus(String prompt) {
        while (true) {
            System.out.print(prompt + " [P]resent / [A]bsent / [L]ate: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("P") || input.equals("PRESENT")) {
                return "PRESENT";
            } else if (input.equals("A") || input.equals("ABSENT")) {
                return "ABSENT";
            } else if (input.equals("L") || input.equals("LATE")) {
                return "LATE";
            }
            printError("Invalid status! Enter P for Present, A for Absent, or L for Late.");
        }
    }

    public static void printHeader(String title) {
        int width = 75;
        String line = "═".repeat(width);
        System.out.println("\n" + CYAN + line + RESET);
        int pad = (width - title.length() - 2) / 2;
        System.out.println(CYAN + "║" + " ".repeat(Math.max(0, pad)) + BOLD + title + RESET + CYAN + " ".repeat(Math.max(0, width - pad - title.length() - 2)) + "║" + RESET);
        System.out.println(CYAN + line + RESET);
    }

    public static void printSubHeader(String title) {
        System.out.println("\n" + BOLD + "─── " + title + " ───" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "✔ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "✘ " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress [Enter] to return to menu...");
        scanner.nextLine();
    }
}
