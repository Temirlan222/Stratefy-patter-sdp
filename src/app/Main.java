package app;

import Facedes.AdminFacade;
import Facedes.PurchaseWorkflowRepository;
import Facedes.UserFacede;
import finalization.AccessControlGate;
import finalization.TicketAccessRepository;
import processing.FraudChecker;
import processing.TicketPrinter;
import processing.TicketProcessingRepository;
import reservation.ReservationRepository;
import reservation.SeatingChart;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Path database = Path.of(args.length == 0 ? "data/concert.db" : args[0]).toAbsolutePath();
        try {
            Files.createDirectories(database.getParent());
            System.out.println("База данных: " + database);
            try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
                run(input, System.out, "jdbc:sqlite:" + database);
            }
        } catch (IOException | SQLException failure) {
            System.err.println("Не удалось открыть приложение: " + failure.getMessage());
            System.exit(1);
        }
    }

    static void run(Scanner input, PrintStream output, String databaseUrl) throws SQLException {
        ReservationRepository reservations = new ReservationRepository(databaseUrl);
        TicketProcessingRepository tickets = new TicketProcessingRepository(databaseUrl);
        TicketAccessRepository access = new TicketAccessRepository(databaseUrl);
        PurchaseWorkflowRepository workflows = new PurchaseWorkflowRepository(databaseUrl);
        AdminFacade admin = new AdminFacade(workflows, reservations, tickets, access);
        SeatingChart seatingChart = new SeatingChart(100, reservations);
        TicketPrinter printer = new TicketPrinter(new FraudChecker(Set.of(7)), tickets);

        while (true) {
            output.println("""

                    === Concert Ticketing ===
                    1. Купить билет
                    2. Посмотреть состояние покупки
                    3. Компенсировать незавершённую покупку
                    4. Покупка с отказом B: покупатель 7 заблокирован
                    5. Покупка с отказом C: система прохода недоступна
                    0. Выход
                    """);
            if (!input.hasNextLine()) return;
            try {
                int choice = readInt(input, output, "Пункт меню: ");
                switch (choice) {
                    case 0 -> { return; }
                    case 1, 4, 5 -> {
                        int purchaseId = readPositiveInt(input, output, "ID покупки (новый): ");
                        int eventId = readPositiveInt(input, output, "ID концерта: ");
                        int seatId = readPositiveInt(input, output, "Место (1–100): ");
                        int customerId = choice == 4 ? 7
                                : readPositiveInt(input, output, "ID покупателя (7 заблокирован): ");
                        int basePrice = readPositiveInt(input, output, "Исходная цена: ");
                        UserFacede user = new UserFacede(seatingChart, printer,
                                new AccessControlGate(access, choice != 5), workflows, admin);
                        try {
                            user.purchase(purchaseId, eventId, seatId, customerId, basePrice);
                            output.println("Покупка завершена успешно.");
                        } catch (SQLException | RuntimeException failure) {
                            printFailure(output, failure);
                        }
                        if (workflows.findById(purchaseId) != null) {
                            output.println(admin.inspectPurchase(purchaseId));
                        }
                    }
                    case 2 -> output.println(admin.inspectPurchase(
                            readPositiveInt(input, output, "ID покупки: ")));
                    case 3 -> {
                        int purchaseId = readPositiveInt(input, output, "ID покупки: ");
                        admin.compensate(purchaseId);
                        output.println(admin.inspectPurchase(purchaseId));
                    }
                    default -> output.println("Нет такого пункта меню.");
                }
            } catch (NumberFormatException failure) {
                output.println("Введите целое число.");
            } catch (NoSuchElementException endOfInput) {
                return;
            } catch (SQLException | IllegalArgumentException | IllegalStateException failure) {
                printFailure(output, failure);
            }
        }
    }

    private static int readInt(Scanner input, PrintStream output, String prompt) {
        output.print(prompt);
        output.flush();
        return Integer.parseInt(input.nextLine().trim());
    }

    private static int readPositiveInt(Scanner input, PrintStream output, String prompt) {
        int value = readInt(input, output, prompt);
        if (value <= 0) throw new IllegalArgumentException("Значение должно быть больше нуля.");
        return value;
    }

    private static void printFailure(PrintStream output, Exception failure) {
        output.println("Ошибка: " + failure.getMessage());
        for (Throwable suppressed : failure.getSuppressed()) {
            output.println("Компенсация не завершена: " + suppressed.getMessage()
                    + ". Повторите её через пункт 3 после устранения причины.");
        }
    }
}