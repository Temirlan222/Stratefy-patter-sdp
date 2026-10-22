package finalization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class TicketAccessRepositoryTest {
    @Test
    void savesAndUpdatesAccessAcrossReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("access.db");
        TicketAccessRepository repository = new TicketAccessRepository(url);
        TicketAccess access = new TicketAccess(1, 10, 5000);
        access.activate();
        repository.save(access);

        TicketAccessRepository reopened = new TicketAccessRepository(url);
        TicketAccess saved = reopened.findById(1);
        assertNotNull(saved);
        assertEquals(1, saved.getAccessId());
        assertEquals(10, saved.getTicketId());
        assertEquals(5000, saved.getAmount());
        assertEquals("ACTIVE", saved.getStatus());

        saved.refund();
        reopened.save(saved);
        TicketAccess refunded = new TicketAccessRepository(url).findById(1);
        assertNotNull(refunded);
        assertEquals("REFUNDED", refunded.getStatus());
    }

    @Test
    void returnsNullForMissingAccess(@TempDir Path tempDir) throws SQLException {
        TicketAccessRepository repository = new TicketAccessRepository(
                "jdbc:sqlite:" + tempDir.resolve("access.db"));
        assertNull(repository.findById(99));
    }
}