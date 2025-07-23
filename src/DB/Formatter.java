package DB;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {

    public static String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String result = "₱" + formatter.format(value);
        System.out.println("[DEBUG] Formatted currency: " + result);
        return result;
    }

    public static String formatTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String result = dateTime.format(formatter);
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
