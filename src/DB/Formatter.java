package DB;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

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
    public static String formatDateTime(LocalDateTime dateTime) {
        String result = dateTime.format(dateTimeFormatter);
        System.out.println("[DEBUG] Formatted date-time: " + result);
        return result;
    }
}

