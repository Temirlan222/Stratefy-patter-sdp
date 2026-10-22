package reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeatingChartTest {
    @Test
    void rejectsReservedSeat(@TempDir Path tempDir) throws SQLException {
        ReservationRepository repository = new ReservationRepository(
                "jdbc:sqlite:" + tempDir.resolve("reservations.db"));
        SeatingChart chart = new SeatingChart(100, repository);
        SeatReservation reservation = new SeatReservation(1, 10, 5, 4000);
        reservation.reserve();
        repository.save(reservation);

        assertThrows(IllegalArgumentException.class,
                () -> chart.validateAvailableSeat(10, 5));
        assertDoesNotThrow(() -> chart.validateAvailableSeat(10, 6));
        assertDoesNotThrow(() -> chart.validateAvailableSeat(20, 5));

        reservation.release();
        repository.save(reservation);
        assertDoesNotThrow(() -> chart.validateAvailableSeat(10, 5));
    }

    @Test
    void rejectsSeatOutsideChart(@TempDir Path tempDir) throws SQLException {
        ReservationRepository repository = new ReservationRepository(
                "jdbc:sqlite:" + tempDir.resolve("reservations.db"));
        SeatingChart chart = new SeatingChart(100, repository);

        assertThrows(IllegalArgumentException.class,
                () -> chart.validateAvailableSeat(10, 0));
        assertThrows(IllegalArgumentException.class,
                () -> chart.validateAvailableSeat(10, 101));
        assertDoesNotThrow(() -> chart.validateAvailableSeat(10, 1));
        assertDoesNotThrow(() -> chart.validateAvailableSeat(10, 100));
    }

    @Test
    void reservesSeatAndSavesCalculatedPrice(@TempDir Path tempDir)
            throws SQLException {
        ReservationRepository repository = new ReservationRepository(
                "jdbc:sqlite:" + tempDir.resolve("reservations.db"));
        SeatingChart chart = new SeatingChart(2, repository);

        SeatReservation first = chart.reserveSeat(1, 10, 1, 5000);
        SeatReservation second = chart.reserveSeat(2, 10, 2, 5000);

        assertEquals(5000, first.getPrice());
        assertEquals(6000, second.getPrice());
        assertEquals("RESERVED", second.getStatus());

        SeatReservation saved = repository.findById(2);
        assertNotNull(saved);
        assertEquals(6000, saved.getPrice());
        assertEquals("RESERVED", saved.getStatus());
    }
}