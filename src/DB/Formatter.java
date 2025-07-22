package DB;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    // Formats a double value into Philippine Peso currency format (e.g., ₱1,234.56)
    public static String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String result = "₱" + formatter.format(value);
        System.out.println("[DEBUG] Formatted currency: " + result);
        return result;
    }

    // Formats a LocalDateTime into a readable string
    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String result = dateTime.format(timeFormatter);
        System.out.println("[DEBUG] Formatted time: " + result);
        return result;
    }

    public static String formatNumber(int value) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.US);
        String result = formatter.format(value);
        System.out.println("[DEBUG] Formatted number: " + result);
        return result;
    }
}

