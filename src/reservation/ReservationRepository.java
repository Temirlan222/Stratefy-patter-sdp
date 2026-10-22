package reservation;


import java.sql.*;

public class ReservationRepository {
    String adressDatabase = "";
    public ReservationRepository(String adressDatabase) throws SQLException {
        this.adressDatabase = adressDatabase;
        try (Connection connection = DriverManager.getConnection(adressDatabase);
             Statement statement = connection.createStatement()) {
            String sql = """
        CREATE TABLE IF NOT EXISTS reservations (
            reservation_id INTEGER PRIMARY KEY,
            event_id INTEGER NOT NULL,
            seat_id INTEGER NOT NULL,
            price INTEGER NOT NULL,
            status TEXT NOT NULL
        );
        """;
            statement.executeUpdate(sql);

        }

    }


    public void save(SeatReservation reservation) throws SQLException {
        String sql =
                """
                        INSERT INTO reservations (
                            reservation_id, event_id, seat_id, price, status
                        )
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT(reservation_id) DO UPDATE SET
                            event_id = excluded.event_id,
                            seat_id = excluded.seat_id,
                            price = excluded.price,
                            status = excluded.status;
                """;
        try (Connection connection = DriverManager.getConnection(adressDatabase);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1,reservation.getReservationId());
            pstmt.setInt(2,reservation.getEventId());
            pstmt.setInt(3,reservation.getSeatId());
            pstmt.setInt(4,reservation.getPrice());
            pstmt.setString(5,reservation.getStatus());
            int rowsAffected = pstmt.executeUpdate();

        }
    }
    public SeatReservation findById(int reservationId) throws SQLException{
        String sql = """
                SELECT reservation_id, event_id, seat_id, price, status
                FROM reservations
                WHERE reservation_id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(adressDatabase);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1,reservationId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return new SeatReservation(
                            resultSet.getInt("reservation_id"),
                            resultSet.getInt("event_id"),
                            resultSet.getInt("seat_id"),
                            resultSet.getInt("price"),
                            resultSet.getString("status")
                    );
                }
            }
            return null;
        }
    }

    public boolean isSeatReserved(int eventId, int seatId) throws SQLException {
        String sql = """
                SELECT 1
                FROM reservations
                WHERE event_id = ?
                  AND seat_id = ?
                  AND status = 'RESERVED'
                LIMIT 1;
                """;
        try (Connection connection = DriverManager.getConnection(adressDatabase);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, seatId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    public int countReservedSeats(int eventId) throws SQLException{
        String sql = """
                SELECT COUNT(*) AS reserved_count
                FROM reservations
                WHERE event_id = ?
                  AND status = 'RESERVED';
                """;
        try (Connection connection = DriverManager.getConnection(adressDatabase);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1,eventId);
                try (ResultSet resultSet = pstmt.executeQuery()){
                    resultSet.next();
                    return resultSet.getInt("reserved_count");
            }
        }
    }
}
