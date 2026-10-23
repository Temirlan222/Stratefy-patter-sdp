package strategy.risk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import processing.FraudChecker;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class RiskStrategiesTest {
    private final FraudChecker fraudChecker = new FraudChecker(Set.of(7));

    // Fake external service: always returns a known purchase count.
    private static class FixedPurchaseHistory implements PurchaseHistorySource {
        private final int count;

        FixedPurchaseHistory(int count) {
            this.count = count;
        }

        @Override
        public int countRecentPurchases(int customerId) {
            return count;
        }
    }

    @Test
    void standardCheckDoesNotApplyAnAmountLimit() {
        RiskCheckStrategy strategy = new StandardRiskCheck(fraudChecker);
        assertDoesNotThrow(() -> strategy.check(20, 100000));
    }

    @Test
    void largePurchaseAllowsTheLimitButRejectsAnAmountAboveIt() {
        RiskCheckStrategy strategy = new LargePurchaseRiskCheck(fraudChecker, 50000);
        assertDoesNotThrow(() -> strategy.check(20, 50000));
        assertThrows(IllegalArgumentException.class, () -> strategy.check(20, 50001));
    }

    @Test
    void frequentPurchaseUsesTheExternalCountAtTheBoundary() {
        RiskCheckStrategy belowLimit = new FrequentPurchaseRiskCheck(
                fraudChecker, new FixedPurchaseHistory(2), 3);
        RiskCheckStrategy atLimit = new FrequentPurchaseRiskCheck(
                fraudChecker, new FixedPurchaseHistory(3), 3);

        assertDoesNotThrow(() -> belowLimit.check(20, 1000));
        assertThrows(IllegalArgumentException.class, () -> atLimit.check(20, 1000));
    }

    @Test
    void frequentPurchaseRequestsHistoryForTheActualCustomer() {
        PurchaseHistorySource history = customerId -> customerId == 30 ? 3 : 0;
        RiskCheckStrategy strategy = new FrequentPurchaseRiskCheck(fraudChecker, history, 3);

        assertDoesNotThrow(() -> strategy.check(20, 1000));
        assertThrows(IllegalArgumentException.class, () -> strategy.check(30, 1000));
    }

    static Stream<RiskCheckStrategy> allRiskStrategies() {
        FraudChecker fraudChecker = new FraudChecker(Set.of(7));
        return Stream.of(
                new StandardRiskCheck(fraudChecker),
                new LargePurchaseRiskCheck(fraudChecker, 50000),
                new FrequentPurchaseRiskCheck(fraudChecker, new FixedPurchaseHistory(0), 3));
    }

    @ParameterizedTest
    @MethodSource("allRiskStrategies")
    void everyStrategyRejectsABlockedCustomer(RiskCheckStrategy strategy) {
        assertThrows(IllegalArgumentException.class, () -> strategy.check(7, 1000));
    }

    @ParameterizedTest
    @MethodSource("allRiskStrategies")
    void everyStrategyAcceptsAnOrdinaryPurchase(RiskCheckStrategy strategy) {
        assertDoesNotThrow(() -> strategy.check(20, 1000));
    }
}
