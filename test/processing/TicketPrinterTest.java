package processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketPrinterTest {
    @Test
    void printsAndSavesTicket(@TempDir Path tempDir) throws SQLException {
        TicketProcessingRepository repository = new TicketProcessingRepository(
                "jdbc:sqlite:" + tempDir.resolve("tickets.db"));
        TicketPrinter printer = new TicketPrinter(new FraudChecker(Set.of(7)), repository);

        TicketProcessing ticket = printer.printTicket(1, 10, 20);

        assertEquals("PRINTED", ticket.getStatus());
        TicketProcessing saved = repository.findById(1);
        assertNotNull(saved);
        assertEquals(10, saved.getReservationId());
        assertEquals("PRINTED", saved.getStatus());
        assertThrows(IllegalArgumentException.class,
                () -> printer.printTicket(1, 99, 20));
        assertEquals(10, repository.findById(1).getReservationId());
    }

    @Test
    void blockedCustomerDoesNotProduceTicket(@TempDir Path tempDir) throws SQLException {
        TicketProcessingRepository repository = new TicketProcessingRepository(
                "jdbc:sqlite:" + tempDir.resolve("tickets.db"));
        TicketPrinter printer = new TicketPrinter(new FraudChecker(Set.of(7)), repository);

        assertThrows(IllegalArgumentException.class,
                () -> printer.printTicket(1, 10, 7));
        assertNull(repository.findById(1));
    }

    @Test
    void cancelsPersistedTicketAfterReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("tickets.db");
        TicketPrinter first = new TicketPrinter(
                new FraudChecker(Set.of()), new TicketProcessingRepository(url));
        first.printTicket(1, 10, 20);

        TicketPrinter reopened = new TicketPrinter(
                new FraudChecker(Set.of()), new TicketProcessingRepository(url));
        reopened.cancelTicket(1);

        TicketProcessing saved = new TicketProcessingRepository(url).findById(1);
        assertNotNull(saved);
        assertEquals("CANCELLED", saved.getStatus());
        assertThrows(IllegalArgumentException.class, () -> reopened.cancelTicket(99));
    }
}