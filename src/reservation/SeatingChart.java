package reservation;

import java.sql.SQLException;

public class SeatingChart {
    private final int seatCount;
    private final ReservationRepository reservationRepository;

    public SeatingChart(int seatCount, ReservationRepository reservationRepository){
        this.reservationRepository = reservationRepository;
        if (seatCount<=0){
            throw new IllegalArgumentException("Количество мест должно быть больше нуля");
        }
        this.seatCount = seatCount;
    }
    public void validateSeat(int seatId){
        if(seatId <= 0 ||seatCount < seatId  ){
            throw new IllegalArgumentException("Нету такого места");
        }
    }
    public void validateAvailableSeat(int eventId, int seatId) throws SQLException {
        validateSeat(seatId);
        if (reservationRepository.isSeatReserved(eventId, seatId)) {
            throw new IllegalArgumentException("Место уже зарезервировано");
        }
        
    }
    public SeatReservation reserveSeat(int reservationId, int eventId, int seatId, int basePrice) throws SQLException{
        validateAvailableSeat(eventId, seatId);
        DynamicPricingEngine dynamicPricingEngine = new DynamicPricingEngine();
        SeatReservation seatReservation = new SeatReservation(reservationId,eventId,seatId,dynamicPricingEngine.calculatePrice(basePrice,reservationRepository.countReservedSeats(eventId),seatCount));
        seatReservation.reserve();
        reservationRepository.save(seatReservation);
        return seatReservation;
    }



}
