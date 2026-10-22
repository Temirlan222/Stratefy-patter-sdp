package finalization;

public class RefundService {
    public int calculateRefund(TicketAccess access) {
        if (!"ACTIVE".equals(access.getStatus())) {
            throw new IllegalArgumentException("Возврат возможен только для активного доступа");
        }
        return access.getAmount();
    }
}