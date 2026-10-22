package processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketProcessingTest {
    @Test
    void newTicketHasNewStatus() {
        TicketProcessing ticket = new TicketProcessing(1, 10);

        assertEquals("NEW", ticket.getStatus());
    }

    @Test
    void printsAndCancelsTicket() {
        TicketProcessing ticket = new TicketProcessing(1, 10);

        ticket.print();
        assertEquals("PRINTED", ticket.getStatus());

        ticket.cancel();
        assertEquals("CANCELLED", ticket.getStatus());
    }

    @Test
    void cannotPrintTicketTwice() {
        TicketProcessing ticket = new TicketProcessing(1, 10);
        ticket.print();

        assertThrows(IllegalArgumentException.class, () -> ticket.print());
        assertEquals("PRINTED", ticket.getStatus());
    }
}