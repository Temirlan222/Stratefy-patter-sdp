package finalization;

import java.sql.SQLException;

public class AccessControlGate {
    private final TicketAccessRepository repository;
    private final boolean available;

    public AccessControlGate(TicketAccessRepository repository, boolean available) {
        this.repository = repository;
        this.available = available;
    }

    public TicketAccess activateAccess(int accessId, int ticketId, int amount)
            throws SQLException {
        if (!available) {
            throw new IllegalStateException("Система прохода недоступна");
        }
        if (repository.findById(accessId) != null) {
            throw new IllegalArgumentException("Запись доступа уже существует: " + accessId);
        }
        TicketAccess access = new TicketAccess(accessId, ticketId, amount);
        access.activate();
        repository.save(access);
        return access;
    }

    public int refundAccess(int accessId) throws SQLException {
        TicketAccess access = repository.findById(accessId);
        if (access == null) {
            throw new IllegalArgumentException("Запись доступа не найдена: " + accessId);
        }
        RefundService refunds = new RefundService();
        int amount = refunds.calculateRefund(access);
        access.refund();
        repository.save(access);
        return amount;
    }
}