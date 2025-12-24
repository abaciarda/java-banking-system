package abaciarda.bankingsystem.utils;

import java.time.Instant;
import java.time.ZoneId;

public class CH {
    public static String RED = "\u001B[31m";
    public static String GREEN = "\u001B[32m";
    public static String RESET = "\u001B[0m";

    public static void printDivider() {
        System.out.println("--------------------------------------------");
    }

    public static void printTitle(String title) {
        printDivider();
        System.out.println(GREEN + title + RESET);
        printDivider();
    }

    public static void success(String msg) {
        System.out.println(GREEN + "[✓] " + msg + RESET);
    }

    public static void error(String msg) {
        System.out.println(RED + "[✗] " + msg + RESET);
    }

    public static void info(String msg) {
        System.out.println("[i] " + msg);
    }

    public static void singleSpace() {
        System.out.println();
    }

    public static void multiSpace() {
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public static String formatDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace("T", " ");
    }
}
