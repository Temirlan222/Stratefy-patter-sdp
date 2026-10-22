package finalization;

import java.sql.*;

public class TicketAccessRepository {
    private final String databaseUrl;

    public TicketAccessRepository(String databaseUrl) throws SQLException {
        this.databaseUrl = databaseUrl;
        String sql = """
                CREATE TABLE IF NOT EXISTS ticket_access (
                    access_id INTEGER PRIMARY KEY,
                    ticket_id INTEGER NOT NULL,
                    amount INTEGER NOT NULL,
                    status TEXT NOT NULL
                );
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void save(TicketAccess access) throws SQLException {
        String sql = """
                INSERT INTO ticket_access (access_id, ticket_id, amount, status)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(access_id) DO UPDATE SET
                    ticket_id = excluded.ticket_id,
                    amount = excluded.amount,
                    status = excluded.status;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, access.getAccessId());
            statement.setInt(2, access.getTicketId());
            statement.setInt(3, access.getAmount());
            statement.setString(4, access.getStatus());
            statement.executeUpdate();
        }
    }

    public TicketAccess findById(int accessId) throws SQLException {
        String sql = """
                SELECT access_id, ticket_id, amount, status
                FROM ticket_access
                WHERE access_id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accessId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new TicketAccess(
                            resultSet.getInt("access_id"),
                            resultSet.getInt("ticket_id"),
                            resultSet.getInt("amount"),
                            resultSet.getString("status"));
                }
                return null;
            }
        }
    }
}