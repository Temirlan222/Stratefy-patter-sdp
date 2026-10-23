package strategy.risk;

import processing.FraudChecker;

public class RiskCheckSelector {
    private final FraudChecker fraudChecker;
    private final PurchaseHistorySource purchaseHistorySource;

    public RiskCheckSelector(FraudChecker fraudChecker, PurchaseHistorySource purchaseHistorySource) {
        this.fraudChecker = fraudChecker;
        this.purchaseHistorySource = purchaseHistorySource;
    }

    public RiskCheckStrategy selectByName(String mode) {
        return switch (mode) {
            case "standard" -> new StandardRiskCheck(fraudChecker);
            case "large_purchase" -> new LargePurchaseRiskCheck(fraudChecker, 50000);
            case "frequent_purchase" -> new FrequentPurchaseRiskCheck(
                    fraudChecker, purchaseHistorySource, 3);
            default -> throw new IllegalArgumentException("Неизвестная стратегия риска: " + mode);
        };
    }

    public RiskCheckStrategy selectAutomatically(boolean highDemand, int amount) {
        if (highDemand) {
            return selectByName("frequent_purchase");
        }
        if (amount >= 50000) {
            return selectByName("large_purchase");
        }
        return selectByName("standard");
    }
    public RiskCheckStrategy select(String mode, boolean highDemand, int amount) {
        if ("auto".equals(mode)) {
            return selectAutomatically(highDemand, amount);
        }
        return selectByName(mode);
    }
}
