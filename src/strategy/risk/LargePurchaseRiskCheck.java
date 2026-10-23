package strategy.risk;

import processing.FraudChecker;

public class LargePurchaseRiskCheck implements RiskCheckStrategy {
    private final FraudChecker fraudChecker;
    private final int maximumAmount;
    public LargePurchaseRiskCheck(FraudChecker fraudChecker, int maximumAmount){
        this.fraudChecker = fraudChecker;
        this.maximumAmount = maximumAmount;
    }
    @Override
    public void check(int customerId, int amount){
        fraudChecker.check(customerId);
        if(amount > maximumAmount){
            throw new IllegalArgumentException("Сумма больше максимальной суммы");
        }
    }

}
