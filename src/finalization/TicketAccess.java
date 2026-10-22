package finalization;

public class TicketAccess {
    private final int accessId;
    private final int ticketId;
    private final int amount;
    private String status;

    public TicketAccess(int accessId, int ticketId, int amount) {
        this(accessId, ticketId, amount, "NEW");
    }

    public TicketAccess(int accessId, int ticketId, int amount, String status) {
        if (!"NEW".equals(status) && !"ACTIVE".equals(status) && !"REFUNDED".equals(status)) {
            throw new IllegalArgumentException("Неизвестный статус доступа: " + status);
        }
        this.accessId = accessId;
        this.ticketId = ticketId;
        this.amount = amount;
        this.status = status;
    }

    public int getAccessId() {
        return accessId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void activate() {
        if (!status.equals("NEW")) {
            throw new IllegalArgumentException("Активировать можно только новый доступ");
        }
        status = "ACTIVE";
    }

    public void refund() {
        if (!status.equals("ACTIVE")) {
            throw new IllegalArgumentException("Возврат возможен только для активного доступа");
        }
        status = "REFUNDED";
    }
}