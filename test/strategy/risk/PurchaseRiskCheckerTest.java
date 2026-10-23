package strategy.risk;

import org.junit.jupiter.api.Test;
import processing.FraudChecker;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseRiskCheckerTest {
    @Test
    void delegatesBothArgumentsToTheStrategy() {
        int[] received = new int[2];
        PurchaseRiskChecker context = new PurchaseRiskChecker((customerId, amount) -> {
            received[0] = customerId;
            received[1] = amount;
        });

        context.check(20, 5000);

        assertArrayEquals(new int[]{20, 5000}, received);
    }

    @Test
    void changesTheAlgorithmOnTheSameContext() {
        FraudChecker fraudChecker = new FraudChecker(Set.of(7));
        PurchaseRiskChecker context = new PurchaseRiskChecker(new StandardRiskCheck(fraudChecker));

        assertDoesNotThrow(() -> context.check(20, 60000));

        context.setStrategy(new LargePurchaseRiskCheck(fraudChecker, 50000));
        assertThrows(IllegalArgumentException.class, () -> context.check(20, 60000));

        context.setStrategy(new FrequentPurchaseRiskCheck(fraudChecker, customerId -> 0, 3));
        assertDoesNotThrow(() -> context.check(20, 60000));
    }
}
