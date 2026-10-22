package strategy.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {
    @Test
    void standardReturnsBasePrice() {
        PricingService service = new PricingService();

        assertEquals(5000, service.calculatePrice("standard", 5000, 70, 100));
    }

    @Test
    void demandKeepsPriceBelowHalfCapacity() {
        PricingService service = new PricingService();

        assertEquals(5000, service.calculatePrice("demand", 5000, 30, 100));
    }

    @Test
    void demandRaisesPriceAtHalfCapacity() {
        PricingService service = new PricingService();

        assertEquals(6000, service.calculatePrice("demand", 5000, 50, 100));
    }

    @Test
    void bookingFeeAddsFixedCharge() {
        PricingService service = new PricingService();

        assertEquals(5500, service.calculatePrice("booking_fee", 5000, 30, 100));
    }

    @Test
    void unknownModeThrowsException() {
        PricingService service = new PricingService();

        assertThrows(IllegalArgumentException.class,
                () -> service.calculatePrice("unknown", 5000, 30, 100));
    }
}