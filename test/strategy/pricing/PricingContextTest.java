package strategy.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingContextTest {
    @Test
    void delegatesAllArgumentsToStrategy() {
        PricingStrategy testStrategy = (basePrice, reservedSeats, totalSeats) ->
                basePrice + reservedSeats + totalSeats;
        PricingContext context = new PricingContext(testStrategy);

        assertEquals(5170, context.calculatePrice(5000, 70, 100));
    }

    @Test
    void switchesStrategiesOnSameContextAtRuntime() {
        PricingContext context = new PricingContext(new StandardPricing());
        assertEquals(5000, context.calculatePrice(5000, 70, 100));

        context.setStrategy(new DemandPricing());
        assertEquals(6000, context.calculatePrice(5000, 70, 100));

        context.setStrategy(new BookingFeePricing());
        assertEquals(5500, context.calculatePrice(5000, 70, 100));
    }
}