package reservation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicPricingEngineTest {
    @Test
    void calculatesPriceDependingOnOccupancy() {
        DynamicPricingEngine engine = new DynamicPricingEngine();

        assertEquals(5000, engine.calculatePrice(5000, 30, 100));
        assertEquals(6000, engine.calculatePrice(5000, 50, 100));
        assertEquals(6000, engine.calculatePrice(5000, 60, 100));
    }
}