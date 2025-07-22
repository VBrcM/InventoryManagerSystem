package DB;

import java.text.NumberFormat;
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
}

