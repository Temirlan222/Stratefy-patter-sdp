package reservation;

public class SeatReservation {
    public int reservationId;
    public int eventId;
    public int seatId;
    public int  price;
    private String status;

    public SeatReservation(int reservationId,int eventId, int seatId, int price) {
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.seatId = seatId;
        this.price = price;
        this.status = "NEW";
    }
    public SeatReservation(int reservationId, int eventId, int seatId, int price, String status) {
        if (!"NEW".equals(status) &&
                !"RESERVED".equals(status) &&
                !"RELEASED".equals(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.seatId = seatId;
        this.price = price;
        this.status = status;
    }

    public void reserve(){
        if(status.equals("NEW")){
            this.status = "RESERVED";

        }
        else{
            throw new IllegalArgumentException("invalid transition");
        }
    }
    public void release(){
        if(status.equals("RESERVED")){
            this.status = "RELEASED";

        }
        else{
            throw new IllegalArgumentException("invalid transition");
        }
    }

    public int getReservationId() {
        return this.reservationId;
    }

    public int getEventId() {
        return this.eventId;
    }

    public int getSeatId() {
        return this.seatId;
    }

    public int getPrice() {
        return this.price;
    }

    public String getStatus(){
        return this.status;
    }

}
