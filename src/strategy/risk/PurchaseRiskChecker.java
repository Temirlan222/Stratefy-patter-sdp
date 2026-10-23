package strategy.risk;

public class PurchaseRiskChecker {
    private RiskCheckStrategy strategy;
    public PurchaseRiskChecker(RiskCheckStrategy strategy){
        this.strategy = strategy;
    }

    public void setStrategy(RiskCheckStrategy strategy){
        this.strategy = strategy;
    }

    public void check(int customerId, int amount){
        strategy.check(customerId,amount);

    }
}
