package app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FechaFormato {
    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2025, 12, 29);
        DateTimeFormatter yyyy = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("YYYY");

        System.out.println("yyyy: " + yyyy.format(date)); // 2022
        System.out.println("YYYY: " + YYYY.format(date));
    }
}