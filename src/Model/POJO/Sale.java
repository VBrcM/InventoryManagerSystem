package Model.POJO;

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

    public void setId(int id) {
        this.id = id;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return String.format("Sale{id=%d, dateTime=%s}", id, dateTime);
    }
}

