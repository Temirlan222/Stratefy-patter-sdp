package app;

import Facedes.PurchaseWorkflowRepository;
import finalization.TicketAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void menuRunsPurchaseBothFailuresAndInspectionAfterRestart(@TempDir Path tempDir)
            throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        String input = """
                1
                1
                10
                1
                20
                5000
                4
                2
                10
                2
                5000
                5
                3
                10
                3
                20
                5000
                0
                """;
        String output = runMenu(input, url);
        assertTrue(output.contains("COMPLETED"));
        assertTrue(output.contains("COMPENSATED"));
        assertEquals("RESERVED", new ReservationRepository(url).findById(1).getStatus());
        assertEquals("ACTIVE", new TicketAccessRepository(url).findById(1).getStatus());
        assertEquals("RELEASED", new ReservationRepository(url).findById(2).getStatus());
        assertNull(new TicketProcessingRepository(url).findById(2));
        assertEquals("RELEASED", new ReservationRepository(url).findById(3).getStatus());
        assertEquals("CANCELLED", new TicketProcessingRepository(url).findById(3).getStatus());
        assertEquals("COMPENSATED", new PurchaseWorkflowRepository(url).findById(3).getStatus());

        String reopened = runMenu("2\n1\n3\n2\n0\n", url);
        assertTrue(reopened.contains("COMPLETED"));
        assertTrue(reopened.contains("COMPENSATED"));
    }

    @Test
    void handlesInvalidInputAndMissingPurchase(@TempDir Path tempDir) throws SQLException {
        String url = "jdbc:sqlite:" + tempDir.resolve("concert.db");
        String output = runMenu("abc\n9\n2\n99\n0\n", url);
        assertTrue(output.contains("Введите целое число"));
        assertTrue(output.contains("Нет такого пункта"));
        assertTrue(output.contains("Покупка не найдена"));
        assertNull(new PurchaseWorkflowRepository(url).findById(99));
    }

    private String runMenu(String commands, String url) throws SQLException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Main.run(new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8), url);
        return bytes.toString(StandardCharsets.UTF_8);
    }
}