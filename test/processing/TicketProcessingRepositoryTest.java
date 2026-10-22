package processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class TicketProcessingRepositoryTest {
    @Test
    void savesAndUpdatesTicketAcrossReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("tickets.db");
        TicketProcessingRepository first = new TicketProcessingRepository(url);
        TicketProcessing ticket = new TicketProcessing(1, 10);
        ticket.print();
        first.save(ticket);

        TicketProcessingRepository second = new TicketProcessingRepository(url);
        TicketProcessing restored = second.findById(1);
        assertNotNull(restored);
        assertEquals(1, restored.getTicketId());
        assertEquals(10, restored.getReservationId());
        assertEquals("PRINTED", restored.getStatus());

        restored.cancel();
        second.save(restored);
        TicketProcessing saved = new TicketProcessingRepository(url).findById(1);
        assertNotNull(saved);
        assertEquals("CANCELLED", saved.getStatus());
    }

    @Test
    void returnsNullForMissingTicket(@TempDir Path tempDir) throws SQLException {
        TicketProcessingRepository repository = new TicketProcessingRepository(
                "jdbc:sqlite:" + tempDir.resolve("tickets.db"));

        assertNull(repository.findById(99));
    }
}