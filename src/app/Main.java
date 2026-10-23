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
import strategy.pricing.PricingContext;
import strategy.pricing.PricingSelector;
import strategy.pricing.StandardPricing;
import strategy.risk.RiskCheckSelector;
import strategy.risk.SimulatedPurchaseHistorySource;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;

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
        PricingSelector selector = new PricingSelector();
        PricingContext pricingContext = new PricingContext(new StandardPricing());
        SeatingChart seatingChart = new SeatingChart(100, reservations, pricingContext);
        RiskCheckSelector riskSelector = new RiskCheckSelector(new FraudChecker(Set.of(7)),
                new SimulatedPurchaseHistorySource(Map.of(30, 3)));
        String riskMode = "auto";
        String pricingMode = "auto";
        boolean partnerBooking = false;
        while (true) {
            output.println("""

                    === Concert Ticketing ===
                    1. Купить билет
                    2. Посмотреть состояние покупки
                    3. Компенсировать незавершённую покупку
                    4. Покупка с отказом B: покупатель 7 заблокирован
                    5. Покупка с отказом C: система прохода недоступна
                    6. Настроить цену: auto / standard / demand / booking_fee
                    7. Настроить риск: auto / standard / large_purchase / frequent_purchase
                    8. Демо всех стратегий цены: один контекст
                    10. Демо всех стратегий риска: один контекст
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
                        int reservedSeats = reservations.countReservedSeats(eventId);
                        pricingContext.setStrategy(selector.select(
                                pricingMode, partnerBooking, reservedSeats, 100));
                        boolean highDemand = reservedSeats * 2 >= 100;
                        TicketPrinter printer = new TicketPrinter(riskSelector, riskMode, highDemand, tickets);
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
                    case 6 -> {
                        output.print("Режим цены (auto, standard, demand, booking_fee): ");
                        String nextMode = input.nextLine().trim();
                        // Validate the name before replacing the current settings.
                        selector.select(nextMode, false, 0, 100);
                        int partner = readInt(input, output, "Партнёрская покупка? 1 — да, 0 — нет: ");
                        if (partner != 0 && partner != 1) {
                            throw new IllegalArgumentException("Введите 0 или 1.");
                        }
                        pricingMode = nextMode;
                        partnerBooking = partner == 1;
                        output.println("Настройки цены сохранены: " + pricingMode
                                + ", партнёрская покупка: " + partnerBooking);
                    }
                    case 7 -> {
                        output.print("Режим риска (auto, standard, large_purchase, frequent_purchase): ");
                        String nextMode = input.nextLine().trim();
                        riskSelector.select(nextMode, false, 0);
                        riskMode = nextMode;
                        output.println("Режим риска сохранён: " + riskMode);
                    }
                    case 8 -> StrategyDemo.pricing(output);
                    case 10 -> StrategyDemo.risk(output);
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
