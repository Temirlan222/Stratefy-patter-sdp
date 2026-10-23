package Facedes;

import finalization.AccessControlGate;
import finalization.TicketAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import processing.FraudChecker;
import processing.TicketPrinter;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;
import reservation.SeatingChart;
import strategy.pricing.BookingFeePricing;
import strategy.pricing.PricingContext;
import strategy.risk.LargePurchaseRiskCheck;
import strategy.risk.PurchaseRiskChecker;
import strategy.risk.RiskCheckStrategy;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseRiskIntegrationTest {
    @Test
    void passesCustomerAndFinalPriceToRiskCheck(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("success.db");
        int[] received = new int[2];
        UserFacede facade = createOffice(url, (customerId, amount) -> {
            received[0] = customerId;
            received[1] = amount;
        });

        facade.purchase(1, 10, 1, 20, 5000);

        assertArrayEquals(new int[]{20, 5500}, received);
        assertEquals("PRINTED", new TicketProcessingRepository(url).findById(1).getStatus());
        assertEquals(5500, new TicketAccessRepository(url).findById(1).getAmount());
        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    @Test
    void riskRejectionCompensatesReservationWhenTheFeeExceedsTheLimit(@TempDir Path dir)
            throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("rejection.db");
        UserFacede facade = createOffice(url,
                new LargePurchaseRiskCheck(new FraudChecker(Set.of(7)), 50000));

        assertThrows(IllegalArgumentException.class,
                () -> facade.purchase(1, 10, 1, 20, 49800));

        var reservation = new ReservationRepository(url).findById(1);
        assertEquals(50300, reservation.getPrice());
        assertEquals("RELEASED", reservation.getStatus());
        assertNull(new TicketProcessingRepository(url).findById(1));
        assertNull(new TicketAccessRepository(url).findById(1));
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    private UserFacede createOffice(String url, RiskCheckStrategy strategy) throws SQLException {
        var reservations = new ReservationRepository(url);
        var tickets = new TicketProcessingRepository(url);
        var access = new TicketAccessRepository(url);
        var workflows = new PurchaseWorkflowRepository(url);
        var chart = new SeatingChart(100, reservations, new PricingContext(new BookingFeePricing()));
        var printer = new TicketPrinter(new PurchaseRiskChecker(strategy), tickets);
        var admin = new AdminFacade(workflows, reservations, tickets, access);
        return new UserFacede(chart, printer, new AccessControlGate(access, true), workflows, admin);
    }
}
