package Facedes;

public class PurchaseWorkflow {
    private final int purchaseId;
    private final int eventId;
    private final int seatId;
    private final int customerId;
    private final int basePrice;
    private String status;

    public PurchaseWorkflow(int purchaseId, int eventId, int seatId,
                            int customerId, int basePrice) {
        this(purchaseId, eventId, seatId, customerId, basePrice, "PENDING_A");
    }

    public PurchaseWorkflow(int purchaseId, int eventId, int seatId,
                            int customerId, int basePrice, String status) {
        if (!"PENDING_A".equals(status) && !"PENDING_B".equals(status)
                && !"PENDING_C".equals(status) && !"COMPLETED".equals(status)
                && !"COMPENSATING".equals(status) && !"COMPENSATED".equals(status)) {
            throw new IllegalArgumentException("Неизвестный статус покупки: " + status);
        }
        this.purchaseId = purchaseId;
        this.eventId = eventId;
        this.seatId = seatId;
        this.customerId = customerId;
        this.basePrice = basePrice;
        this.status = status;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public int getEventId() {
        return eventId;
    }

    public int getSeatId() {
        return seatId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public String getStatus() {
        return status;
    }

    public void markACompleted() {
        if (!status.equals("PENDING_A")) {
            throw new IllegalArgumentException("Шаг A можно завершить только из PENDING_A");
        }
        status = "PENDING_B";
    }

    public void markBCompleted() {
        if (!status.equals("PENDING_B")) {
            throw new IllegalArgumentException("Шаг B можно завершить только из PENDING_B");
        }
        status = "PENDING_C";
    }

    public void markCCompleted() {
        if (!status.equals("PENDING_C")) {
            throw new IllegalArgumentException("Шаг C можно завершить только из PENDING_C");
        }
        status = "COMPLETED";
    }

    public void beginCompensation() {
        if ("COMPENSATING".equals(status)) {
            return;
        }
        if (!"PENDING_A".equals(status) && !"PENDING_B".equals(status)
                && !"PENDING_C".equals(status)) {
            throw new IllegalArgumentException("Компенсация разрешена только для незавершённой покупки");
        }
        status = "COMPENSATING";
    }

    public void markCompensated() {
        if (!"COMPENSATING".equals(status)) {
            throw new IllegalArgumentException("Сначала нужно начать компенсацию");
        }
        status = "COMPENSATED";
    }
}