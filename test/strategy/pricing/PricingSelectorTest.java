package strategy.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingSelectorTest {
    @Test
    void selectsEachStrategyByName() {
        PricingSelector selector = new PricingSelector();

        assertInstanceOf(StandardPricing.class, selector.selectByName("standard"));
        assertInstanceOf(DemandPricing.class, selector.selectByName("demand"));
        assertInstanceOf(BookingFeePricing.class, selector.selectByName("booking_fee"));
    }

    @Test
    void rejectsUnknownName() {
        PricingSelector selector = new PricingSelector();

        assertThrows(IllegalArgumentException.class, () -> selector.selectByName("unknown"));
    }

    @Test
    void partnerBookingHasPriorityOverDemand() {
        PricingSelector selector = new PricingSelector();

        assertInstanceOf(BookingFeePricing.class, selector.selectAutomatically(true, 20, 100));
        assertInstanceOf(BookingFeePricing.class, selector.selectAutomatically(true, 80, 100));
    }

    @Test
    void selectsDemandAtHalfCapacityForNonPartnerBooking() {
        PricingSelector selector = new PricingSelector();

        assertInstanceOf(DemandPricing.class, selector.selectAutomatically(false, 50, 100));
    }

    @Test
    void selectsStandardBelowHalfCapacityForNonPartnerBooking() {
        PricingSelector selector = new PricingSelector();

        assertInstanceOf(StandardPricing.class, selector.selectAutomatically(false, 49, 100));
    }
}