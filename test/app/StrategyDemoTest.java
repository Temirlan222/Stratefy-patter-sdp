package app;

import Facedes.PurchaseWorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reservation.ReservationRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class StrategyDemoTest {
    @Test
    void pricingDemoShowsAllAlgorithmsWithIdenticalInput(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("pricing-demo.db");
        String output = run("8\n0\n", url);

        assertTrue(output.contains("base=5000, reserved=70, total=100"));
        assertTrue(output.contains("standard = 5000"));
        assertTrue(output.contains("demand = 6000"));
        assertTrue(output.contains("booking_fee = 5500"));
        assertTrue(output.contains("same context, runtime switching"));
        assertTrue(output.contains("Manual: booking_fee = 5500"));
        assertTrue(output.contains("Auto: DemandPricing = 6000"));
        assertEquals(0, new ReservationRepository(url).countReservedSeats(10));
    }

    @Test
    void riskDemoShowsAllChecksDespiteRejections(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("risk-demo.db");
        String output = run("10\n0\n", url);

        assertTrue(output.contains("customer=30, amount=60000, recent purchases=3"));
        assertTrue(output.contains("standard = ACCEPTED"));
        assertTrue(output.contains("large_purchase = REJECTED"));
        assertTrue(output.contains("frequent_purchase = REJECTED"));
        assertTrue(output.contains("Manual: standard = ACCEPTED"));
        assertTrue(output.contains("Auto: FrequentPurchaseRiskCheck = REJECTED"));
        assertTrue(output.contains("same context, runtime switching"));
        assertNull(new PurchaseWorkflowRepository(url).findById(1));
    }

    @Test
    void demosDoNotReplaceTheRealPurchaseSettings(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("independent-demo.db");
        run("6\nbooking_fee\n0\n7\nstandard\n8\n10\n"
                + "1\n1\n10\n1\n30\n60000\n0\n", url);

        assertEquals(60500, new ReservationRepository(url).findById(1).getPrice());
        assertEquals("COMPLETED", new PurchaseWorkflowRepository(url).findById(1).getStatus());
    }

    private String run(String commands, String url) throws SQLException {
        var bytes = new ByteArrayOutputStream();
        Main.run(new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8), url);
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
