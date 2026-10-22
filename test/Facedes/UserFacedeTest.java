package Facedes;

import finalization.AccessControlGate;
import finalization.TicketAccess;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserFacedeTest {
    @Test
    void completesPurchaseAndPersistsAllSteps(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        UserFacede facade = createTicketOffice(url, 100);

        PurchaseWorkflow result = facade.purchase(1, 10, 5, 20, 5000);

        assertEquals("COMPLETED", result.getStatus());
        PurchaseWorkflow workflow = new PurchaseWorkflowRepository(url).findById(1);
        assertNotNull(workflow);
        assertEquals("COMPLETED", workflow.getStatus());

        SeatReservation reservation = new ReservationRepository(url).findById(1);
        assertNotNull(reservation);
        assertEquals(10, reservation.getEventId());
        assertEquals(5, reservation.getSeatId());
        assertEquals(5000, reservation.getPrice());
        assertEquals("RESERVED", reservation.getStatus());

        TicketProcessing ticket = new TicketProcessingRepository(url).findById(1);
        assertNotNull(ticket);
        assertEquals(1, ticket.getReservationId());
        assertEquals("PRINTED", ticket.getStatus());

        TicketAccess access = new TicketAccessRepository(url).findById(1);
        assertNotNull(access);
        assertEquals(1, access.getTicketId());
        assertEquals(5000, access.getAmount());
        assertEquals("ACTIVE", access.getStatus());
    }

    @Test
    void passesCalculatedPriceToFinalization(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        UserFacede facade = createTicketOffice(url, 2);
        facade.purchase(1, 10, 1, 20, 5000);

        facade.purchase(2, 10, 2, 21, 5000);

        SeatReservation reservation = new ReservationRepository(url).findById(2);
        TicketAccess access = new TicketAccessRepository(url).findById(2);
        assertNotNull(reservation);
        assertNotNull(access);
        assertEquals(6000, reservation.getPrice());
        assertEquals(6000, access.getAmount());
    }

    @Test
    void rejectsRepeatedPurchaseIdWithoutChangingSavedPurchase(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        UserFacede facade = createTicketOffice(url, 100);
        facade.purchase(1, 10, 5, 20, 5000);

        assertThrows(IllegalArgumentException.class,
                () -> facade.purchase(1, 99, 6, 21, 7000));

        PurchaseWorkflow saved = new PurchaseWorkflowRepository(url).findById(1);
        assertNotNull(saved);
        assertEquals(10, saved.getEventId());
        assertEquals(5, saved.getSeatId());
        assertEquals(20, saved.getCustomerId());
        assertEquals(5000, saved.getBasePrice());
        assertEquals("COMPLETED", saved.getStatus());
        assertEquals(1, new ReservationRepository(url).countReservedSeats(10));
        assertEquals(0, new ReservationRepository(url).countReservedSeats(99));
    }

    private UserFacede createTicketOffice(String url, int seatCount) throws SQLException {
        SeatingChart seatingChart = new SeatingChart(seatCount, new ReservationRepository(url));
        TicketPrinter printer = new TicketPrinter(
                new FraudChecker(Set.of(7)), new TicketProcessingRepository(url));
        AccessControlGate gate = new AccessControlGate(new TicketAccessRepository(url), true);
        AdminFacade admin = new AdminFacade(new PurchaseWorkflowRepository(url),
                new ReservationRepository(url), new TicketProcessingRepository(url),
                new TicketAccessRepository(url));
        return new UserFacede(seatingChart, printer, gate, new PurchaseWorkflowRepository(url), admin);
    }
}