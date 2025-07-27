package DB;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class for consistent formatting of currency, date, time, and numbers.
 */
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class for consistent formatting of currency, numbers, and date/time
 * values using Philippine locale settings.
 *
 * All methods are static and stateless, designed for reusable formatting in the
 * application UI and logging.
 */
public class AppFormatter {

    private static final Locale LOCALE_PH = new Locale("en", "PH");
    private static final String PESO_SYMBOL = "₱";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a");

    /**
     * Formats a double value as Philippine currency (e.g. ₱1,234.56).
     */
    public static String formatCurrency(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(LOCALE_PH);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return PESO_SYMBOL + formatter.format(value);
    }

    /**
     * Formats a LocalDateTime as 12-hour time with AM/PM (e.g. 02:15 PM).
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(TIME_FORMAT);
    }

    /**
     * Formats an integer with digit grouping (e.g. 1,000).
     */
    public static String formatNumber(int value) {
        return NumberFormat.getIntegerInstance(LOCALE_PH).format(value);
    }

    /**
     * Formats a LocalDateTime to a readable date (e.g. July 24, 2025).
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMAT);
    }

}
