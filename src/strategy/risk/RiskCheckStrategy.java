package strategy.risk;

public interface RiskCheckStrategy {
    void check(int customerId, int amount);
}