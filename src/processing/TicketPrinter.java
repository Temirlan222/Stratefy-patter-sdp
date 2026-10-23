package processing;

import strategy.risk.PurchaseRiskChecker;
import strategy.risk.StandardRiskCheck;
import strategy.risk.RiskCheckSelector;

import java.sql.SQLException;

public class TicketPrinter {
    private final PurchaseRiskChecker riskChecker;
    private final TicketProcessingRepository repository;
    private final RiskCheckSelector selector;
    private final String riskMode;
    private final boolean highDemand;

    // Existing callers supply a ready context; no automatic selection is applied.
    public TicketPrinter(PurchaseRiskChecker riskChecker, TicketProcessingRepository repository) {
        this.riskChecker = riskChecker;
        this.repository = repository;
        this.selector = null;
        this.riskMode = "standard";
        this.highDemand = false;
    }

    // The menu supplies selection settings; the final amount arrives in printTicket().
    public TicketPrinter(RiskCheckSelector selector, String riskMode, boolean highDemand,
                         TicketProcessingRepository repository) {
        this.riskChecker = new PurchaseRiskChecker(selector.selectByName("standard"));
        this.repository = repository;
        this.selector = selector;
        this.riskMode = riskMode;
        this.highDemand = highDemand;
    }
    public TicketPrinter(FraudChecker fraudChecker, TicketProcessingRepository repository) {
        this(new PurchaseRiskChecker(new StandardRiskCheck(fraudChecker)), repository);
    }

    public TicketProcessing printTicket(int ticketId, int reservationId, int customerId)
            throws SQLException {
        return printTicket(ticketId, reservationId, customerId, 0);
    }

    public TicketProcessing printTicket(int ticketId, int reservationId, int customerId, int amount)
            throws SQLException {
        if (selector != null) {
            riskChecker.setStrategy(selector.select(riskMode, highDemand, amount));
        }
        riskChecker.check(customerId, amount);
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
