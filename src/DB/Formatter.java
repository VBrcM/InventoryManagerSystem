package DB;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    private static final Locale PH_LOCALE = new Locale("en", "PH");
    private static final String PESO_SYMBOL = "₱";

    // Currency formatting (e.g., ₱1,234.56)
    public static String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(PH_LOCALE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return PESO_SYMBOL + formatter.format(value);
    }

    // 12-hour time with AM/PM from LocalDateTime
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    // 12-hour time with AM/PM from LocalTime
    public static String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    // Integer number formatting (e.g., 1,000)
    public static String formatNumber(int value) {
        return NumberFormat.getIntegerInstance(PH_LOCALE).format(value);
    }

    // Date only (e.g., July 24, 2025)
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    // Full datetime (e.g., July 24, 2025 02:15 PM)
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a"));
    }
}
