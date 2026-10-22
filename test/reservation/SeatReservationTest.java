package reservation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class SeatReservationTest {
    @Test
    void newReservationHasNewStatus(){
        SeatReservation seatTest = new SeatReservation(1,2,2,20);
        assertEquals("NEW",seatTest.getStatus());
    }

    @Test
    void reserveChangesStatusToReserved(){
        SeatReservation seatTest = new SeatReservation(1,2,2,299);
        seatTest.reserve();
        assertEquals("RESERVED",seatTest.getStatus());
    }

    @Test
    void reserveTwiceThrowsException(){
        SeatReservation seatTest = new SeatReservation(1,2,3,200);
        seatTest.reserve();
        assertThrows(IllegalArgumentException.class, () -> seatTest.reserve());
        assertEquals("RESERVED",seatTest.getStatus());
    }

    @Test
    void  releaseChangesStatusToReleased(){
        SeatReservation seatTest = new SeatReservation(1,2,3,200);
        seatTest.reserve();
        seatTest.release();
        assertEquals("RELEASED",seatTest.getStatus());
    }

    @Test
    void releaseWithoutReservationThrowsException(){
        SeatReservation seatTest = new SeatReservation(1,2,3,200);
        assertThrows(IllegalArgumentException.class, () -> seatTest.release());
        assertEquals("NEW",seatTest.getStatus());
    }

    @Test
    void Blabla() {
        SeatReservation seatTest = new SeatReservation(1,2,3,200);
        seatTest.reserve();
        seatTest.release();
        assertThrows(IllegalArgumentException.class, () -> seatTest.reserve());
        assertEquals("RELEASED",seatTest.getStatus());
    }

}
