package finalization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefundServiceTest {
    @Test
    void calculatesFullRefundWithoutChangingAccess() {
        TicketAccess access = new TicketAccess(1, 10, 5000);
        access.activate();
        RefundService refunds = new RefundService();

        assertEquals(5000, refunds.calculateRefund(access));
        assertEquals("ACTIVE", access.getStatus());
    }

    @Test
    void rejectsRefundForInactiveAccess() {
        TicketAccess access = new TicketAccess(1, 10, 5000);
        RefundService refunds = new RefundService();

        assertThrows(IllegalArgumentException.class, () -> refunds.calculateRefund(access));
        assertEquals("NEW", access.getStatus());

        access.activate();
        access.refund();
        assertThrows(IllegalArgumentException.class, () -> refunds.calculateRefund(access));
        assertEquals("REFUNDED", access.getStatus());
    }
}