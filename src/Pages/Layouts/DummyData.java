package Pages.Layouts;

import java.time.LocalDate;
import java.util.*;

public class DummyData {

    // FIELDS

    private static final Map<LocalDate, List<SoldItem>> transactions = new LinkedHashMap<>();

    // STATIC INITIALIZER

    static {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 35; i++) {
            LocalDate date = today.minusDays(i);
            List<SoldItem> daySales = new ArrayList<>();

            // Simulate 2–3 items sold per day
            daySales.add(new SoldItem("Item " + (i + 1), "Category " + ((i % 3) + 1), (i % 5) + 1, 50 + (i * 5)));
            daySales.add(new SoldItem("Item " + (i + 2), "Category " + ((i % 2) + 1), ((i + 1) % 4) + 1, 60 + (i * 3)));

            if (i % 3 == 0) { // Occasionally add a third item
                daySales.add(new SoldItem("Item " + (i + 3), "Category 3", 1 + (i % 3), 80 + (i * 2)));
            }

            transactions.put(date, daySales);
        }
    }

    // GETTERS

    public static List<SoldItem> getTransactionsForDate(LocalDate date) {
        return transactions.getOrDefault(date, new ArrayList<>());
    }

    public static List<LocalDate> getAllTransactionDates() {
        return new ArrayList<>(transactions.keySet());
    }
}
