package processing;

public class TicketProcessing {
    private final int ticketId;
    private final int reservationId;
    private String status;

    public TicketProcessing(int ticketId, int reservationId) {
        this(ticketId, reservationId, "NEW");
    }

    public TicketProcessing(int ticketId, int reservationId, String status) {
        if (!"NEW".equals(status) && !"PRINTED".equals(status) && !"CANCELLED".equals(status)) {
            throw new IllegalArgumentException("Неизвестный статус билета: " + status);
        }
        this.ticketId = ticketId;
        this.reservationId = reservationId;
        this.status = status;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getStatus() {
        return status;
    }

    public void print() {
        if (!status.equals("NEW")) {
            throw new IllegalArgumentException("Билет можно выпустить только из статуса NEW");
        }
        status = "PRINTED";
    }

    public void cancel() {
        if (!status.equals("PRINTED")) {
            throw new IllegalArgumentException("Отменить можно только выпущенный билет");
        }
        status = "CANCELLED";
    }
}