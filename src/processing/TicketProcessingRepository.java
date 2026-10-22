package processing;

import java.sql.*;

public class TicketProcessingRepository {
    private final String databaseUrl;

    public TicketProcessingRepository(String databaseUrl) throws SQLException {
        this.databaseUrl = databaseUrl;
        String sql = """
                CREATE TABLE IF NOT EXISTS ticket_processing (
                    ticket_id INTEGER PRIMARY KEY,
                    reservation_id INTEGER NOT NULL,
                    status TEXT NOT NULL
                );
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void save(TicketProcessing ticket) throws SQLException {
        String sql = """
                INSERT INTO ticket_processing (ticket_id, reservation_id, status)
                VALUES (?, ?, ?)
                ON CONFLICT(ticket_id) DO UPDATE SET
                    reservation_id = excluded.reservation_id,
                    status = excluded.status;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ticket.getTicketId());
            statement.setInt(2, ticket.getReservationId());
            statement.setString(3, ticket.getStatus());
            statement.executeUpdate();
        }
    }

    public TicketProcessing findById(int ticketId) throws SQLException {
        String sql = """
                SELECT ticket_id, reservation_id, status
                FROM ticket_processing
                WHERE ticket_id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ticketId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new TicketProcessing(
                            resultSet.getInt("ticket_id"),
                            resultSet.getInt("reservation_id"),
                            resultSet.getString("status"));
                }
                return null;
            }
        }
    }
}