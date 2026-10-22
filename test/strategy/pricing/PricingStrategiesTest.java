package strategy.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingStrategiesTest {
    @Test
    void standardReturnsBasePrice() {
        PricingStrategy strategy = new StandardPricing();

        assertEquals(5000, strategy.calculatePrice(5000, 70, 100));
    }

    @Test
    void demandRaisesPriceStartingAtHalfCapacity() {
        PricingStrategy strategy = new DemandPricing();

        assertEquals(5000, strategy.calculatePrice(5000, 49, 100));
        assertEquals(6000, strategy.calculatePrice(5000, 50, 100));
    }

    @Test
    void bookingFeeAddsFixedCharge() {
        PricingStrategy strategy = new BookingFeePricing();

        assertEquals(5500, strategy.calculatePrice(5000, 70, 100));
    }

    @ParameterizedTest
    @MethodSource("allPricingStrategies")
    void priceIsNonNegativeForNonNegativeBasePrice(PricingStrategy strategy) {
        assertTrue(strategy.calculatePrice(0, 70, 100) >= 0);
        assertTrue(strategy.calculatePrice(5000, 70, 100) >= 0);
    }

    static Stream<PricingStrategy> allPricingStrategies() {
        return Stream.of(new StandardPricing(), new DemandPricing(), new BookingFeePricing());
    }
}