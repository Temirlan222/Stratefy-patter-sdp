package strategy.risk;

public interface PurchaseHistorySource {
    int countRecentPurchases(int customerId);
}
