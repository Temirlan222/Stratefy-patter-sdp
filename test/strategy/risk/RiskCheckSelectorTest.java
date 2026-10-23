package strategy.risk;

import org.junit.jupiter.api.Test;
import processing.FraudChecker;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RiskCheckSelectorTest {
    private final RiskCheckSelector selector = new RiskCheckSelector(
            new FraudChecker(Set.of(7)), customerId -> customerId == 30 ? 3 : 2);

    @Test
    void manualSelectionCreatesConfiguredStrategies() {
        RiskCheckStrategy standard = selector.selectByName("standard");
        assertInstanceOf(StandardRiskCheck.class, standard);
        assertDoesNotThrow(() -> standard.check(20, 100000));
        assertThrows(IllegalArgumentException.class, () -> standard.check(7, 1000));

        RiskCheckStrategy large = selector.selectByName("large_purchase");
        assertInstanceOf(LargePurchaseRiskCheck.class, large);
        assertDoesNotThrow(() -> large.check(20, 50000));
        assertThrows(IllegalArgumentException.class, () -> large.check(20, 50001));

        RiskCheckStrategy frequent = selector.selectByName("frequent_purchase");
        assertInstanceOf(FrequentPurchaseRiskCheck.class, frequent);
        assertDoesNotThrow(() -> frequent.check(20, 1000));
        assertThrows(IllegalArgumentException.class, () -> frequent.check(30, 1000));
    }

    @Test
    void unknownNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> selector.selectByName("unknown"));
    }

    @Test
    void highDemandHasPriorityOverTheAmount() {
        assertInstanceOf(FrequentPurchaseRiskCheck.class, selector.selectAutomatically(true, 60000));
        assertInstanceOf(FrequentPurchaseRiskCheck.class, selector.selectAutomatically(true, 1000));
    }

    @Test
    void largeAmountSelectsTheAmountCheckAtTheBoundary() {
        assertInstanceOf(LargePurchaseRiskCheck.class, selector.selectAutomatically(false, 50000));
        assertInstanceOf(LargePurchaseRiskCheck.class, selector.selectAutomatically(false, 60000));
    }

    @Test
    void ordinaryPurchaseSelectsTheStandardCheck() {
        assertInstanceOf(StandardRiskCheck.class, selector.selectAutomatically(false, 49999));
    }
}
