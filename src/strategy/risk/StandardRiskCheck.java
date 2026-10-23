package strategy.risk;

import processing.FraudChecker;

public class StandardRiskCheck implements RiskCheckStrategy {
    private final FraudChecker fraudChecker;

    public StandardRiskCheck(FraudChecker fraudChecker) {
        this.fraudChecker = fraudChecker;
    }

    @Override
    public void check(int customerId, int amount) {
        fraudChecker.check(customerId);
    }
}