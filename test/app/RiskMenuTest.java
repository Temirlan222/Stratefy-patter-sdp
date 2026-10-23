package app;

import Facedes.PurchaseWorkflowRepository;
import finalization.TicketAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;
import reservation.SeatReservation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class RiskMenuTest {
    @Test
    void switchesFromManualStandardToAutomaticAmountCheck(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("modes.db");
        run("7\nstandard\n1\n1\n10\n1\n20\n60000\n"
                + "7\nauto\n1\n2\n10\n2\n20\n60000\n0\n", url);

        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
        assertCompensatedWithoutTicket(url, 2);
    }

    @Test
    void manualFrequencyCheckUsesSimulatedExternalHistory(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("history.db");
        run("7\nfrequent_purchase\n1\n1\n10\n1\n30\n5000\n0\n", url);

        assertCompensatedWithoutTicket(url, 1);
    }

    @Test
    void automaticHighDemandUsesHistoryBeforeTheAmountRule(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("demand.db");
        var reservations = new ReservationRepository(url);
        for (int seat = 1; seat <= 50; seat++) {
            var reservation = new SeatReservation(1000 + seat, 10, seat, 5000);
            reservation.reserve();
            reservations.save(reservation);
        }
        run("1\n1\n10\n51\n30\n5000\n1\n2\n10\n52\n20\n60000\n0\n", url);

        assertCompensatedWithoutTicket(url, 1);
        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(2).getStatus());
        assertEquals(72000, reservations.findById(2).getPrice());
    }

    @Test
    void automaticAmountCheckReceivesPriceIncludingTheBookingFee(@TempDir Path dir)
            throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("final-price.db");
        run("6\nbooking_fee\n0\n1\n1\n10\n1\n20\n49800\n0\n", url);

        assertEquals(50300, new ReservationRepository(url).findById(1).getPrice());
        assertCompensatedWithoutTicket(url, 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"standard", "large_purchase", "frequent_purchase"})
    void manualModesCannotBypassTheBlocklist(String mode, @TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("blocked.db");
        run("7\n" + mode + "\n1\n1\n10\n1\n7\n5000\n0\n", url);

        assertCompensatedWithoutTicket(url, 1);
    }

    @Test
    void unknownModeKeepsThePreviousSelection(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("invalid.db");
        String output = run("7\nstandard\n7\nunknown\n"
                + "1\n1\n10\n1\n20\n60000\n0\n", url);

        assertTrue(output.contains("Неизвестная стратегия риска"));
        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    private String run(String commands, String url) throws SQLException {
        var bytes = new ByteArrayOutputStream();
        Main.run(new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8), url);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void assertCompensatedWithoutTicket(String url, int id) throws SQLException {
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(id).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(id).getStatus());
        assertNull(new TicketProcessingRepository(url).findById(id));
        assertNull(new TicketAccessRepository(url).findById(id));
    }
}
