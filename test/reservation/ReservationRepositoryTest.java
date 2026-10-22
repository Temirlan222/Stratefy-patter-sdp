package reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import java.nio.file.Path;
import java.sql.SQLException;


public class ReservationRepositoryTest {
    @Test
    void savedReservationSurvivesReopening(@TempDir Path tempDir)
            throws SQLException {
        Path databaseFile = tempDir.resolve("reservations.db");
        String url = "jdbc:sqlite:" + databaseFile;
        ReservationRepository repository1 = new ReservationRepository(url);
        SeatReservation reservation1 = new SeatReservation(11,22,33,4000);
        reservation1.reserve();
        repository1.save(reservation1);
        ReservationRepository repository2 = new ReservationRepository(url);
        SeatReservation reservation2 = repository2.findById(11);
        assertNotNull(reservation2);
        assertEquals(11,reservation2.getReservationId());
        assertEquals(22,reservation2.getEventId());
        assertEquals(33,reservation2.getSeatId());
        assertEquals(4000,reservation2.getPrice());
        assertEquals("RESERVED",reservation2.getStatus());

    }
}
