package strategy.risk;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatedPurchaseHistorySourceTest {
    @Test
    void returnsConfiguredCountsAndZeroForAnUnknownCustomer() {
        PurchaseHistorySource source = new SimulatedPurchaseHistorySource(Map.of(20, 2, 30, 3));

        assertEquals(2, source.countRecentPurchases(20));
        assertEquals(3, source.countRecentPurchases(30));
        assertEquals(0, source.countRecentPurchases(99));
    }
}
