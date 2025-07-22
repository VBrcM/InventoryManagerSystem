package DB;

import java.time.LocalDateTime;

public class Sale {
    private int id;
    private LocalDateTime dateTime;

    public Sale(int id, LocalDateTime dateTime) {
        this.id = id;
        this.dateTime = dateTime;
    }

    public Sale(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}

