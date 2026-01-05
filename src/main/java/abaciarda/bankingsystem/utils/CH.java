package abaciarda.bankingsystem.utils;

import java.time.Instant;
import java.time.ZoneId;

public class CH {


    public static void printDivider() {
        System.out.println("--------------------------------------------");
    }

    public static void printTitle(String title) {
        printDivider();
        System.out.println(title);
        printDivider();
    }

    public static void success(String msg) {
        System.out.println("[✓] " + msg);
    }

    public static void error(String msg) {
        System.out.println("[✗] " + msg);
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
