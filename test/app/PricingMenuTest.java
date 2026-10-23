package app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reservation.ReservationRepository;
import reservation.SeatReservation;
import finalization.TicketAccessRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class PricingMenuTest {
    @Test
    void changesManualPricingBetweenPurchasesInTheSameSession(@TempDir Path dir)
            throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("manual.db");
        run("6\nbooking_fee\n0\n1\n1\n10\n1\n20\n5000\n"
                + "6\nstandard\n0\n1\n2\n10\n2\n20\n5000\n0\n", url);

        assertEquals(5500, new ReservationRepository(url).findById(1).getPrice());
        assertEquals(5500, new TicketAccessRepository(url).findById(1).getAmount());
        assertEquals(5000, new ReservationRepository(url).findById(2).getPrice());
    }

    @Test
    void automaticPricingUsesTheCurrentEventOccupancy(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("automatic.db");
        seedHalfFull(url);
        run("1\n1\n10\n51\n20\n5000\n1\n2\n11\n1\n20\n5000\n0\n", url);

        assertEquals(6000, new ReservationRepository(url).findById(1).getPrice());
        assertEquals(5000, new ReservationRepository(url).findById(2).getPrice());
    }

    @Test
    void automaticPartnerBookingHasPriorityOverDemand(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("partner.db");
        seedHalfFull(url);
        run("6\nauto\n1\n1\n1\n10\n51\n20\n5000\n0\n", url);

        assertEquals(5500, new ReservationRepository(url).findById(1).getPrice());
    }

    @Test
    void invalidPartnerFlagDoesNotReplacePreviousSettings(@TempDir Path dir) throws SQLException {
        String url = "jdbc:sqlite:" + dir.resolve("invalid.db");
        String text = run("6\nbooking_fee\n0\n6\nstandard\n2\n"
                + "1\n1\n10\n1\n20\n5000\n0\n", url);

        assertTrue(text.contains("Введите 0 или 1"));
        assertEquals(5500, new ReservationRepository(url).findById(1).getPrice());
    }

    private String run(String commands, String url) throws SQLException {
        var bytes = new ByteArrayOutputStream();
        Main.run(new Scanner(commands), new PrintStream(bytes, true, StandardCharsets.UTF_8), url);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void seedHalfFull(String url) throws SQLException {
        var repository = new ReservationRepository(url);
        for (int seat = 1; seat <= 50; seat++) {
            var reservation = new SeatReservation(1000 + seat, 10, seat, 5000);
            reservation.reserve();
            repository.save(reservation);
        }
    }
}
