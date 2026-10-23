package reservation;

import strategy.pricing.DemandPricing;
import strategy.pricing.PricingContext;

import java.sql.SQLException;

public class SeatingChart {
    private final int seatCount;
    private final ReservationRepository reservationRepository;
    private final PricingContext pricingContext;



    public SeatingChart(int seatCount, ReservationRepository reservationRepository){
        this(seatCount, reservationRepository, new PricingContext(new DemandPricing()));
    }
    public SeatingChart(int seatCount, ReservationRepository reservationRepository, PricingContext pricingContext){
        this.seatCount = seatCount;
        this.reservationRepository = reservationRepository;
        this.pricingContext = pricingContext;
        if (seatCount<=0){
            throw new IllegalArgumentException("Количество мест должно быть больше нуля");
        }
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
        SeatReservation seatReservation = new SeatReservation(reservationId,
                eventId,seatId,pricingContext.calculatePrice(basePrice,
                reservationRepository.countReservedSeats(eventId),seatCount));


        seatReservation.reserve();
        reservationRepository.save(seatReservation);
        return seatReservation;
    }



}
