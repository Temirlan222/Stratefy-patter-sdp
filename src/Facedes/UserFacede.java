package Facedes;

import finalization.AccessControlGate;
import processing.TicketPrinter;
import reservation.SeatReservation;
import reservation.SeatingChart;

import java.sql.SQLException;

public class UserFacede {
    private final SeatingChart seatingChart;
    private final TicketPrinter ticketPrinter;
    private final AccessControlGate accessControlGate;
    private final PurchaseWorkflowRepository workflowRepository;
    private final AdminFacade adminFacade;

    public UserFacede(SeatingChart seatingChart, TicketPrinter ticketPrinter,
                      AccessControlGate accessControlGate,
                      PurchaseWorkflowRepository workflowRepository, AdminFacade adminFacade) {
        this.seatingChart = seatingChart;
        this.ticketPrinter = ticketPrinter;
        this.accessControlGate = accessControlGate;
        this.workflowRepository = workflowRepository;
        this.adminFacade = adminFacade;
    }

    public PurchaseWorkflow purchase(int purchaseId, int eventId, int seatId,
                                     int customerId, int basePrice) throws SQLException {
        if (workflowRepository.findById(purchaseId) != null) {
            throw new IllegalArgumentException("Покупка уже существует: " + purchaseId);
        }

        PurchaseWorkflow purchase = new PurchaseWorkflow(
                purchaseId, eventId, seatId, customerId, basePrice);
        workflowRepository.save(purchase);

        try {
            SeatReservation reservation = seatingChart.reserveSeat(
                    purchaseId, eventId, seatId, basePrice);
            purchase.markACompleted();
            workflowRepository.save(purchase);

            ticketPrinter.printTicket(purchaseId, purchaseId, customerId);
            purchase.markBCompleted();
            workflowRepository.save(purchase);

            accessControlGate.activateAccess(purchaseId, purchaseId, reservation.getPrice());
            purchase.markCCompleted();
            workflowRepository.save(purchase);

            return purchase;
        } catch (SQLException | RuntimeException failure) {
            try {
                adminFacade.compensate(purchaseId);
            } catch (SQLException | RuntimeException compensationFailure) {
                failure.addSuppressed(compensationFailure);
            }
            throw failure;
        }
    }
}