package processing;

import java.util.Set;

public class FraudChecker {
    private final Set<Integer> blockedCustomerIds;

    public FraudChecker(Set<Integer> blockedCustomerIds) {
        this.blockedCustomerIds = blockedCustomerIds;
    }

    public void check(int customerId) {
        if (blockedCustomerIds.contains(customerId)) {
            throw new IllegalArgumentException("Покупатель заблокирован: " + customerId);
        }
    }
}