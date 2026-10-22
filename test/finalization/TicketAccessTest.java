package finalization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketAccessTest {
    @Test
    void activatesAndRefundsAccess() {
        TicketAccess access = new TicketAccess(1, 10, 5000);
        assertEquals(1, access.getAccessId());
        assertEquals(10, access.getTicketId());
        assertEquals(5000, access.getAmount());
        assertEquals("NEW", access.getStatus());

        access.activate();
        assertEquals("ACTIVE", access.getStatus());
        access.refund();
        assertEquals("REFUNDED", access.getStatus());
    }

    @Test
    void rejectsIllegalTransitionsWithoutChangingStatus() {
        TicketAccess access = new TicketAccess(1, 10, 5000);
        assertThrows(IllegalArgumentException.class, () -> access.refund());
        assertEquals("NEW", access.getStatus());
        access.activate();
        assertThrows(IllegalArgumentException.class, () -> access.activate());
        assertEquals("ACTIVE", access.getStatus());
        access.refund();
        assertThrows(IllegalArgumentException.class, () -> access.refund());
        assertThrows(IllegalArgumentException.class, () -> access.activate());
        assertEquals("REFUNDED", access.getStatus());
    }

    @Test
    void rejectsUnknownRestoredStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> new TicketAccess(1, 10, 5000, "UNKNOWN"));
    }
}