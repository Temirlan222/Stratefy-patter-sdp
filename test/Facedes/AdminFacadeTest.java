package Facedes;

import finalization.AccessControlGate;
import finalization.TicketAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import processing.FraudChecker;
import processing.TicketPrinter;
import processing.TicketProcessing;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;
import reservation.SeatReservation;
import reservation.SeatingChart;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdminFacadeTest {
    @Test
    void blockedCustomerCompensatesOnlyReservation(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        UserFacede facade = createTicketOffice(url, true);

        assertThrows(IllegalArgumentException.class,
                () -> facade.purchase(1, 10, 5, 7, 5000));

        assertEquals("RELEASED", new ReservationRepository(url).findById(1).getStatus());
        assertNull(new TicketProcessingRepository(url).findById(1));
        assertNull(new TicketAccessRepository(url).findById(1));
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    @Test
    void unavailableGateCompensatesTicketBeforeReservation(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        List<String> order = new ArrayList<>();
        ReservationRepository reservations = new ReservationRepository(url) {
            @Override
            public void save(SeatReservation reservation) throws SQLException {
                super.save(reservation);
                if ("RELEASED".equals(reservation.getStatus())) order.add("A");
            }
        };
        TicketProcessingRepository tickets = new TicketProcessingRepository(url) {
            @Override
            public void save(TicketProcessing ticket) throws SQLException {
                super.save(ticket);
                if ("CANCELLED".equals(ticket.getStatus())) order.add("B");
            }
        };
        TicketAccessRepository access = new TicketAccessRepository(url);
        PurchaseWorkflowRepository workflows = new PurchaseWorkflowRepository(url);
        AdminFacade admin = new AdminFacade(workflows, reservations, tickets, access);
        UserFacede facade = new UserFacede(new SeatingChart(100, reservations),
                new TicketPrinter(new FraudChecker(Set.of(7)), tickets),
                new AccessControlGate(access, false), workflows, admin);

        assertThrows(IllegalStateException.class,
                () -> facade.purchase(1, 10, 5, 20, 5000));

        assertEquals(List.of("B", "A"), order);
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(1).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(1).getStatus());
        assertNull(new TicketAccessRepository(url).findById(1));
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    @Test
    void invalidSeatStopsPurchaseWithoutSubsystemRecords(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        UserFacede facade = createTicketOffice(url, true);

        assertThrows(IllegalArgumentException.class,
                () -> facade.purchase(1, 10, 0, 20, 5000));

        assertNull(new ReservationRepository(url).findById(1));
        assertNull(new TicketProcessingRepository(url).findById(1));
        assertNull(new TicketAccessRepository(url).findById(1));
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    @Test
    void compensatesActualRecordsWhenWorkflowLagsBehind(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        new PurchaseWorkflowRepository(url).save(new PurchaseWorkflow(1, 10, 5, 20, 5000));
        ReservationRepository reservations = new ReservationRepository(url);
        new SeatingChart(100, reservations).reserveSeat(1, 10, 5, 5000);
        new TicketPrinter(new FraudChecker(Set.of()), new TicketProcessingRepository(url))
                .printTicket(1, 1, 20);

        AdminFacade reopened = createAdmin(url);
        String before = reopened.inspectPurchase(1);
        assertTrue(before.contains("PENDING_A"));
        assertTrue(before.contains("RESERVED"));
        assertTrue(before.contains("PRINTED"));
        assertTrue(before.contains("NONE"));

        assertEquals("COMPENSATED", reopened.compensate(1).getStatus());
        assertEquals("COMPENSATED", reopened.compensate(1).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(1).getStatus());
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(1).getStatus());
    }

    @Test
    void resumesInterruptedCompensationAfterReopening(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        ReservationRepository failingReservations = new ReservationRepository(url) {
            @Override
            public void save(SeatReservation reservation) throws SQLException {
                if ("RELEASED".equals(reservation.getStatus())) {
                    throw new SQLException("Simulated failure while saving release");
                }
                super.save(reservation);
            }
        };
        TicketProcessingRepository tickets = new TicketProcessingRepository(url);
        TicketAccessRepository access = new TicketAccessRepository(url);
        PurchaseWorkflowRepository workflows = new PurchaseWorkflowRepository(url);
        AdminFacade admin = new AdminFacade(workflows, failingReservations, tickets, access);
        UserFacede facade = new UserFacede(new SeatingChart(100, failingReservations),
                new TicketPrinter(new FraudChecker(Set.of()), tickets),
                new AccessControlGate(access, false), workflows, admin);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> facade.purchase(1, 10, 5, 20, 5000));
        assertEquals(1, failure.getSuppressed().length);
        assertInstanceOf(SQLException.class, failure.getSuppressed()[0]);
        assertEquals("COMPENSATING", new PurchaseWorkflowRepository(url).findById(1).getStatus());
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(1).getStatus());
        assertEquals("RESERVED", new ReservationRepository(url).findById(1).getStatus());

        PurchaseWorkflow result = createAdmin(url).compensate(1);

        assertEquals("COMPENSATED", result.getStatus());
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(1).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(1).getStatus());
    }

    @Test
    void refundsAccessIfCrashHappenedAfterFinalizationSaved(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        PurchaseWorkflow purchase = new PurchaseWorkflow(1, 10, 5, 20, 5000);
        new SeatingChart(100, new ReservationRepository(url)).reserveSeat(1, 10, 5, 5000);
        purchase.markACompleted();
        new TicketPrinter(new FraudChecker(Set.of()), new TicketProcessingRepository(url))
                .printTicket(1, 1, 20);
        purchase.markBCompleted();
        new PurchaseWorkflowRepository(url).save(purchase);
        new AccessControlGate(new TicketAccessRepository(url), true).activateAccess(1, 1, 5000);

        createAdmin(url).compensate(1);

        assertEquals("REFUNDED", new TicketAccessRepository(url).findById(1).getStatus());
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(1).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(1).getStatus());
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    @Test
    void inspectsCompletedPurchaseAndRejectsInvalidAdminRequests(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        createTicketOffice(url, true).purchase(1, 10, 5, 20, 5000);
        AdminFacade admin = createAdmin(url);

        String report = admin.inspectPurchase(1);
        assertTrue(report.contains("COMPLETED"));
        assertTrue(report.contains("RESERVED"));
        assertTrue(report.contains("PRINTED"));
        assertTrue(report.contains("ACTIVE"));
        assertThrows(IllegalArgumentException.class, () -> admin.compensate(1));
        assertThrows(IllegalArgumentException.class, () -> admin.compensate(99));
        assertThrows(IllegalArgumentException.class, () -> admin.inspectPurchase(99));
        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
        assertEquals("ACTIVE", new TicketAccessRepository(url).findById(1).getStatus());
    }

    private AdminFacade createAdmin(String url) throws SQLException {
        return new AdminFacade(new PurchaseWorkflowRepository(url), new ReservationRepository(url),
                new TicketProcessingRepository(url), new TicketAccessRepository(url));
    }

    private UserFacede createTicketOffice(String url, boolean gateAvailable) throws SQLException {
        return new UserFacede(new SeatingChart(100, new ReservationRepository(url)),
                new TicketPrinter(new FraudChecker(Set.of(7)), new TicketProcessingRepository(url)),
                new AccessControlGate(new TicketAccessRepository(url), gateAvailable),
                new PurchaseWorkflowRepository(url), createAdmin(url));
    }
}