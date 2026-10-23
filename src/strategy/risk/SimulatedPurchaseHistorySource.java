package strategy.risk;

import java.util.Map;

// A fixed snapshot simulates recent purchases returned by an external service.
public class SimulatedPurchaseHistorySource implements PurchaseHistorySource {
    private final Map<Integer, Integer> purchases;

    public SimulatedPurchaseHistorySource(Map<Integer, Integer> purchases) {
        this.purchases = Map.copyOf(purchases);
    }

    @Override
    public int countRecentPurchases(int customerId) {
        return purchases.getOrDefault(customerId, 0);
    }
}
