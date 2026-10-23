package app;

import processing.FraudChecker;
import strategy.pricing.PricingContext;
import strategy.pricing.PricingSelector;
import strategy.pricing.PricingStrategy;
import strategy.pricing.StandardPricing;
import strategy.risk.PurchaseRiskChecker;
import strategy.risk.RiskCheckSelector;
import strategy.risk.RiskCheckStrategy;
import strategy.risk.SimulatedPurchaseHistorySource;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

public class StrategyDemo {
    public static void pricing(PrintStream output) {
        int basePrice = 5000, reservedSeats = 70, totalSeats = 100;
        PricingSelector selector = new PricingSelector();
        PricingContext context = new PricingContext(new StandardPricing());
        output.println("Pricing: base=5000, reserved=70, total=100; same context, runtime switching");
        for (String name : new String[]{"standard", "demand", "booking_fee"}) {
            context.setStrategy(selector.selectByName(name));
            output.println(name + " = " + context.calculatePrice(basePrice, reservedSeats, totalSeats));
        }
        context.setStrategy(selector.selectByName("booking_fee"));
        output.println("Manual: booking_fee = " + context.calculatePrice(basePrice, reservedSeats, totalSeats));
        PricingStrategy automatic = selector.selectAutomatically(false, reservedSeats, totalSeats);
        context.setStrategy(automatic);
        output.println("Auto: " + automatic.getClass().getSimpleName() + " = "
                + context.calculatePrice(basePrice, reservedSeats, totalSeats));
    }

    public static void risk(PrintStream output) {
        int customerId = 30, amount = 60_000;
        RiskCheckSelector selector = new RiskCheckSelector(new FraudChecker(Set.of(7)),
                new SimulatedPurchaseHistorySource(Map.of(30, 3)));
        PurchaseRiskChecker context = new PurchaseRiskChecker(selector.selectByName("standard"));
        output.println("Risk: customer=30, amount=60000, recent purchases=3; same context, runtime switching");
        for (String name : new String[]{"standard", "large_purchase", "frequent_purchase"}) {
            context.setStrategy(selector.selectByName(name));
            output.println(name + " = " + riskResult(context, customerId, amount));
        }
        context.setStrategy(selector.selectByName("standard"));
        output.println("Manual: standard = " + riskResult(context, customerId, amount));
        RiskCheckStrategy automatic = selector.selectAutomatically(true, amount);
        context.setStrategy(automatic);
        output.println("Auto: " + automatic.getClass().getSimpleName() + " = "
                + riskResult(context, customerId, amount));
    }

    private static String riskResult(PurchaseRiskChecker context, int customerId, int amount) {
        try {
            context.check(customerId, amount);
            return "ACCEPTED";
        } catch (IllegalArgumentException failure) {
            return "REJECTED: " + failure.getMessage();
        }
    }
}
