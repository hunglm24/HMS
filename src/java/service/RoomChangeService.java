package service;

import dao.BookingRoomDao;
import dao.RoomDao;
import model.BookingRoom;
import model.Room;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class RoomChangeService {
    private final RoomDao roomDao;
    private final BookingRoomDao bookingRoomDao;

    public RoomChangeService() {
        this(new RoomDao(), new BookingRoomDao());
    }

    public RoomChangeService(RoomDao roomDao, BookingRoomDao bookingRoomDao) {
        this.roomDao = roomDao;
        this.bookingRoomDao = bookingRoomDao;
    }

    public void changeRoom(long bookingId, long currentRoomId, long newRoomId, String reason) throws SQLException {
        // Fail fast on invalid input before starting the transaction.
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Booking không hợp lệ.");
        }
        if (currentRoomId <= 0 || newRoomId <= 0) {
            throw new IllegalArgumentException("Phòng hiện tại hoặc phòng mới không hợp lệ.");
        }
        if (currentRoomId == newRoomId) {
            throw new IllegalArgumentException("Phòng mới phải khác phòng hiện tại.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do đổi phòng.");
        }

        Connection connection = DBConnectionUtil.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }

        // The swap must be atomic so room states and booking-room links stay in sync.
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            // The current booking-room record must exist before we can move it.
            Optional<BookingRoom> currentBookingRoom = bookingRoomDao
                    .findByBookingIdAndRoomId(connection, bookingId, currentRoomId);
            if (currentBookingRoom.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy phòng hiện tại trong booking này.");
            }

            // Prevent duplicate room assignments within the same booking.
            if (bookingRoomDao.findByBookingIdAndRoomId(connection, bookingId, newRoomId).isPresent()) {
                throw new IllegalArgumentException("Booking này đã có phòng mới được chọn.");
            }

            // Validate both room records before writing anything.
            Optional<Room> currentRoom = roomDao.findById(connection, currentRoomId);
            if (currentRoom.isEmpty()) {
                throw new IllegalArgumentException("Phòng hiện tại không tồn tại.");
            }

            Optional<Room> targetRoom = roomDao.findById(connection, newRoomId);
            if (targetRoom.isEmpty()) {
                throw new IllegalArgumentException("Phòng mới không tồn tại.");
            }

            if (!"AVAILABLE".equalsIgnoreCase(targetRoom.get().getStatus())) {
                throw new IllegalArgumentException("Phòng mới phải ở trạng thái trống.");
            }

            // Update booking-room mapping first, then release/occupy the physical rooms.
            boolean updatedBookingRoom = bookingRoomDao.updateRoomId(
                    connection, currentBookingRoom.get().getId(), newRoomId);
            if (!updatedBookingRoom) {
                throw new SQLException("Không thể cập nhật booking room.");
            }

            if (!roomDao.updateStatus(connection, currentRoomId, "AVAILABLE")) {
                throw new SQLException("Không thể cập nhật trạng thái phòng cũ.");
            }
            if (!roomDao.updateStatus(connection, newRoomId, "OCCUPIED")) {
                throw new SQLException("Không thể cập nhật trạng thái phòng mới.");
            }

            connection.commit();
        } catch (IllegalArgumentException ex) {
            connection.rollback();
            throw ex;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } finally {
                connection.close();
            }
        }
    }
}
