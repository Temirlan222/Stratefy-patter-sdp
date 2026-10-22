package Facedes;

import java.sql.*;

public class PurchaseWorkflowRepository {
    private final String databaseUrl;

    public PurchaseWorkflowRepository(String databaseUrl) throws SQLException {
        this.databaseUrl = databaseUrl;
        String sql = """
                CREATE TABLE IF NOT EXISTS purchase_workflows (
                    purchase_id INTEGER PRIMARY KEY,
                    event_id INTEGER NOT NULL,
                    seat_id INTEGER NOT NULL,
                    customer_id INTEGER NOT NULL,
                    base_price INTEGER NOT NULL,
                    status TEXT NOT NULL
                );
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void save(PurchaseWorkflow purchase) throws SQLException {
        String sql = """
                INSERT INTO purchase_workflows
                    (purchase_id, event_id, seat_id, customer_id, base_price, status)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(purchase_id) DO UPDATE SET
                    event_id = excluded.event_id,
                    seat_id = excluded.seat_id,
                    customer_id = excluded.customer_id,
                    base_price = excluded.base_price,
                    status = excluded.status;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchase.getPurchaseId());
            statement.setInt(2, purchase.getEventId());
            statement.setInt(3, purchase.getSeatId());
            statement.setInt(4, purchase.getCustomerId());
            statement.setInt(5, purchase.getBasePrice());
            statement.setString(6, purchase.getStatus());
            statement.executeUpdate();
        }
    }

    public PurchaseWorkflow findById(int purchaseId) throws SQLException {
        String sql = """
                SELECT purchase_id, event_id, seat_id, customer_id, base_price, status
                FROM purchase_workflows
                WHERE purchase_id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new PurchaseWorkflow(
                            resultSet.getInt("purchase_id"),
                            resultSet.getInt("event_id"),
                            resultSet.getInt("seat_id"),
                            resultSet.getInt("customer_id"),
                            resultSet.getInt("base_price"),
                            resultSet.getString("status"));
                }
                return null;
            }
        }
    }
}