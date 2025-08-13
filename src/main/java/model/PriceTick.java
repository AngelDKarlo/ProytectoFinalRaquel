package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceTick {
    private LocalDateTime timestamp;
    private BigDecimal price;

    public PriceTick(LocalDateTime timestamp, BigDecimal price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
