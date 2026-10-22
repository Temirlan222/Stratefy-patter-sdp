package finalization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlGateTest {
    @Test
    void activatesAndSavesAccess(@TempDir Path tempDir) throws SQLException {
        TicketAccessRepository repository = new TicketAccessRepository(
                "jdbc:sqlite:" + tempDir.resolve("access.db"));
        AccessControlGate gate = new AccessControlGate(repository, true);

        TicketAccess access = gate.activateAccess(1, 10, 5000);

        assertEquals("ACTIVE", access.getStatus());
        TicketAccess saved = repository.findById(1);
        assertNotNull(saved);
        assertEquals(10, saved.getTicketId());
        assertEquals(5000, saved.getAmount());
        assertEquals("ACTIVE", saved.getStatus());
        assertThrows(IllegalArgumentException.class,
                () -> gate.activateAccess(1, 20, 6000));
        assertEquals(10, repository.findById(1).getTicketId());
    }

    @Test
    void unavailableGateDoesNotSaveAccess(@TempDir Path tempDir) throws SQLException {
        TicketAccessRepository repository = new TicketAccessRepository(
                "jdbc:sqlite:" + tempDir.resolve("access.db"));
        AccessControlGate gate = new AccessControlGate(repository, false);

        assertThrows(IllegalStateException.class,
                () -> gate.activateAccess(1, 10, 5000));
        assertNull(repository.findById(1));
    }

    @Test
    void refundsSavedAccessAfterReopening(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("access.db");
        AccessControlGate gate = new AccessControlGate(new TicketAccessRepository(url), true);
        gate.activateAccess(1, 10, 5000);
        AccessControlGate reopened = new AccessControlGate(new TicketAccessRepository(url), true);

        assertEquals(5000, reopened.refundAccess(1));

        TicketAccess saved = new TicketAccessRepository(url).findById(1);
        assertNotNull(saved);
        assertEquals("REFUNDED", saved.getStatus());
        assertThrows(IllegalArgumentException.class, () -> reopened.refundAccess(1));
    }

    @Test
    void rejectsRefundForMissingAccess(@TempDir Path tempDir) throws SQLException {
        AccessControlGate gate = new AccessControlGate(new TicketAccessRepository(
                "jdbc:sqlite:" + tempDir.resolve("access.db")), true);

        assertThrows(IllegalArgumentException.class, () -> gate.refundAccess(99));
    }
}