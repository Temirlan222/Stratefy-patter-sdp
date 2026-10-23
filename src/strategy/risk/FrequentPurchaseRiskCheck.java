package strategy.risk;

import processing.FraudChecker;

public class FrequentPurchaseRiskCheck implements RiskCheckStrategy{
    private final FraudChecker fraudChecker;
    private final PurchaseHistorySource purchaseHistorySource;
    private final int maximumPurchases;

    public FrequentPurchaseRiskCheck(FraudChecker fraudChecker,PurchaseHistorySource purchaseHistorySource,int maximumPurchases){
        this.fraudChecker = fraudChecker;
        this.purchaseHistorySource = purchaseHistorySource;
        this.maximumPurchases = maximumPurchases;
    }
    @Override
    public void check(int customerId,int amount){
        fraudChecker.check(customerId);
        if (
                purchaseHistorySource.countRecentPurchases(customerId)>= maximumPurchases
                )
        {
            throw new IllegalArgumentException("Слишком много покупок");
        }
    }
}
