package processing;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FraudCheckerTest {
    @Test
    void rejectsBlockedCustomer() {
        FraudChecker checker = new FraudChecker(Set.of(7, 15));

        assertThrows(IllegalArgumentException.class, () -> checker.check(7));
        assertThrows(IllegalArgumentException.class, () -> checker.check(15));
    }

    @Test
    void allowsCustomerOutsideBlocklist() {
        FraudChecker checker = new FraudChecker(Set.of(7, 15));

        assertDoesNotThrow(() -> checker.check(20));
    }
}