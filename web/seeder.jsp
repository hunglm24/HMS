<%@ page import="java.sql.*" %>
<%@ page import="util.DBConnectionUtil" %>
<%@ page import="java.util.*" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.math.BigDecimal" %>

<%
    out.println("<h2>Seeding database...</h2>");
    Connection conn = null;
    try {
        conn = DBConnectionUtil.getConnection();
        conn.setAutoCommit(false);

        // Delete existing bookings and related data
        Statement stmt = conn.createStatement();
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        stmt.execute("TRUNCATE TABLE payments");
        stmt.execute("TRUNCATE TABLE booking_rooms");
        stmt.execute("TRUNCATE TABLE booking_guests");
        stmt.execute("TRUNCATE TABLE bookings");
        stmt.execute("UPDATE rooms SET status = 'AVAILABLE' WHERE status IN ('OCCUPIED', 'DIRTY')");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        out.println("<p>Cleared old bookings.</p>");

        // Get available rooms
        List<Map<String, Object>> rooms = new ArrayList<>();
        ResultSet rs = stmt.executeQuery("SELECT r.id, r.room_number, rt.name, rt.base_price, rt.id as type_id FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.status = 'AVAILABLE'");
        while (rs.next()) {
            Map<String, Object> room = new HashMap<>();
            room.put("id", rs.getLong("id"));
            room.put("room_number", rs.getString("room_number"));
            room.put("name", rs.getString("name"));
            room.put("base_price", rs.getBigDecimal("base_price"));
            room.put("type_id", rs.getLong("type_id"));
            rooms.add(room);
        }

        // Get an account ID for customer
        long customerId = 2; // Default to ID 2 (which is typically a customer based on data)
        ResultSet rsAcc = stmt.executeQuery("SELECT id FROM accounts WHERE role_id = 2 LIMIT 1");
        if (rsAcc.next()) {
            customerId = rsAcc.getLong("id");
        }

        Random rand = new Random();
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng"};
        String[] lastNames = {"An", "Bình", "Cường", "Dung", "Em", "Phúc", "Giang", "Hải", "Linh", "Minh", "Nga", "Phong", "Quân", "Sơn", "Trang", "Tuấn", "Vy", "Yến"};
        String[] statuses = {"PENDING_PAYMENT", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"};

        PreparedStatement psBooking = conn.prepareStatement("INSERT INTO bookings (customer_id, booking_code, booking_type, check_in_date, check_out_date, total_amount, deposit_amount, status, special_requests, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS);
        PreparedStatement psRoom = conn.prepareStatement("INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)");
        PreparedStatement psGuest = conn.prepareStatement("INSERT INTO booking_guests (booking_id, is_primary_guest, full_name, identity_number, phone, created_at) VALUES (?, 1, ?, ?, ?, CURRENT_TIMESTAMP)");

        LocalDate today = LocalDate.now();
        int seeded = 0;

        // Shuffle rooms to assign different rooms
        Collections.shuffle(rooms);

        for (int i = 1; i <= 20; i++) {
            if (i > rooms.size()) break; // Not enough rooms

            int offsetDays = rand.nextInt(30) - 10; // -10 to +20 days from today
            LocalDate checkIn = today.plusDays(offsetDays);
            int nights = rand.nextInt(5) + 1;
            LocalDate checkOut = checkIn.plusDays(nights);
            
            String code = "WLK-" + System.currentTimeMillis() + rand.nextInt(100);
            String status = statuses[rand.nextInt(statuses.length)];
            
            // For CHECKED_IN, checkIn date should be today or earlier
            if ("CHECKED_IN".equals(status)) {
                checkIn = today.minusDays(rand.nextInt(3)); // Today, yesterday, etc
                checkOut = checkIn.plusDays(nights);
            }
            if ("CHECKED_OUT".equals(status)) {
                checkOut = today.minusDays(rand.nextInt(5));
                checkIn = checkOut.minusDays(nights);
            }
            if ("PENDING_PAYMENT".equals(status) || "CONFIRMED".equals(status)) {
                checkIn = today.plusDays(rand.nextInt(20));
                checkOut = checkIn.plusDays(nights);
            }

            Map<String, Object> room = rooms.get(i - 1);
            long roomId = (Long) room.get("id");
            BigDecimal basePrice = (BigDecimal) room.get("base_price");
            BigDecimal totalAmount = basePrice.multiply(new BigDecimal(nights));
            BigDecimal deposit = "CONFIRMED".equals(status) || "CHECKED_IN".equals(status) || "CHECKED_OUT".equals(status) ? totalAmount.multiply(new BigDecimal("0.5")) : BigDecimal.ZERO;
            if ("CHECKED_OUT".equals(status)) deposit = totalAmount; // Fully paid
            
            // Insert Booking
            psBooking.setLong(1, customerId);
            psBooking.setString(2, code);
            psBooking.setString(3, "WALK_IN");
            psBooking.setDate(4, java.sql.Date.valueOf(checkIn));
            psBooking.setDate(5, java.sql.Date.valueOf(checkOut));
            psBooking.setBigDecimal(6, totalAmount);
            psBooking.setBigDecimal(7, deposit);
            psBooking.setString(8, status);
            psBooking.setString(9, "No special requests");
            psBooking.executeUpdate();

            ResultSet rsKeys = psBooking.getGeneratedKeys();
            if (rsKeys.next()) {
                long bId = rsKeys.getLong(1);
                
                // Insert Room
                psRoom.setLong(1, bId);
                psRoom.setLong(2, roomId);
                psRoom.setBigDecimal(3, basePrice);
                psRoom.setInt(4, nights);
                psRoom.setBigDecimal(5, totalAmount);
                psRoom.executeUpdate();

                // Insert Guest
                String fullName = firstNames[rand.nextInt(firstNames.length)] + " " + lastNames[rand.nextInt(lastNames.length)];
                String phone = "09" + (10000000 + rand.nextInt(90000000));
                String idNum = "0" + (10000000000L + (long)(rand.nextDouble() * 90000000000L));
                
                psGuest.setLong(1, bId);
                psGuest.setString(2, fullName);
                psGuest.setString(3, idNum);
                psGuest.setString(4, phone);
                psGuest.executeUpdate();
                
                // Update physical room status if CHECKED_IN
                if ("CHECKED_IN".equals(status)) {
                    stmt.execute("UPDATE rooms SET status = 'OCCUPIED' WHERE id = " + roomId);
                } else if ("CHECKED_OUT".equals(status)) {
                    stmt.execute("UPDATE rooms SET status = 'DIRTY' WHERE id = " + roomId);
                }
                
                seeded++;
            }
        }

        conn.commit();
        out.println("<p>Successfully seeded " + seeded + " bookings.</p>");
        out.println("<a href='reception/bookings'>Go to Bookings</a>");

    } catch (Exception e) {
        if (conn != null) conn.rollback();
        out.println("<p>Error: " + e.getMessage() + "</p>");
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
%>
