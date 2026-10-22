package processing;

import java.sql.SQLException;

public class TicketPrinter {
    private final FraudChecker fraudChecker;
    private final TicketProcessingRepository repository;

    public TicketPrinter(FraudChecker fraudChecker, TicketProcessingRepository repository) {
        this.fraudChecker = fraudChecker;
        this.repository = repository;
    }

    public TicketProcessing printTicket(int ticketId, int reservationId, int customerId)
            throws SQLException {
        fraudChecker.check(customerId);
        if (repository.findById(ticketId) != null) {
            throw new IllegalArgumentException("Билет уже существует: " + ticketId);
        }
        TicketProcessing ticket = new TicketProcessing(ticketId, reservationId);
        ticket.print();
        repository.save(ticket);
        return ticket;
    }

    public void cancelTicket(int ticketId) throws SQLException {
        TicketProcessing ticket = repository.findById(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Билет не найден: " + ticketId);
        }
        ticket.cancel();
        repository.save(ticket);
    }
}