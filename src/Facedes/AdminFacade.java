package Facedes;

import finalization.RefundService;
import finalization.TicketAccess;
import finalization.TicketAccessRepository;
import processing.TicketProcessing;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;
import reservation.SeatReservation;

import java.sql.SQLException;

public class AdminFacade {
    private final PurchaseWorkflowRepository workflows;
    private final ReservationRepository reservations;
    private final TicketProcessingRepository tickets;
    private final TicketAccessRepository accessRecords;

    public AdminFacade(PurchaseWorkflowRepository workflows, ReservationRepository reservations,
                       TicketProcessingRepository tickets, TicketAccessRepository accessRecords) {
        this.workflows = workflows;
        this.reservations = reservations;
        this.tickets = tickets;
        this.accessRecords = accessRecords;
    }

    public String inspectPurchase(int purchaseId) throws SQLException {
        PurchaseWorkflow purchase = requirePurchase(purchaseId);
        SeatReservation reservation = reservations.findById(purchaseId);
        TicketProcessing ticket = tickets.findById(purchaseId);
        TicketAccess access = accessRecords.findById(purchaseId);
        return """
                Purchase: %d
                Workflow: %s
                A reservation: %s
                B ticket: %s
                C access: %s
                """.formatted(purchaseId, purchase.getStatus(),
                reservation == null ? "NONE" : reservation.getStatus(),
                ticket == null ? "NONE" : ticket.getStatus(),
                access == null ? "NONE" : access.getStatus());
    }

    public PurchaseWorkflow compensate(int purchaseId) throws SQLException {
        PurchaseWorkflow purchase = requirePurchase(purchaseId);
        if ("COMPENSATED".equals(purchase.getStatus())) {
            return purchase;
        }
        purchase.beginCompensation();
        workflows.save(purchase);

        TicketAccess access = accessRecords.findById(purchaseId);
        if (access != null && "ACTIVE".equals(access.getStatus())) {
            new RefundService().calculateRefund(access);
            access.refund();
            accessRecords.save(access);
        }

        TicketProcessing ticket = tickets.findById(purchaseId);
        if (ticket != null && "PRINTED".equals(ticket.getStatus())) {
            ticket.cancel();
            tickets.save(ticket);
        }

        SeatReservation reservation = reservations.findById(purchaseId);
        if (reservation != null && "RESERVED".equals(reservation.getStatus())) {
            reservation.release();
            reservations.save(reservation);
        }

        purchase.markCompensated();
        workflows.save(purchase);
        return purchase;
    }

    private PurchaseWorkflow requirePurchase(int purchaseId) throws SQLException {
        PurchaseWorkflow purchase = workflows.findById(purchaseId);
        if (purchase == null) {
            throw new IllegalArgumentException("Покупка не найдена: " + purchaseId);
        }
        return purchase;
    }
}