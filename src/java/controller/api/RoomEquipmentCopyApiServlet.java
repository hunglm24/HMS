package controller.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Room;
import model.RoomEquipment;
import service.RoomEquipmentService;
import service.RoomService;
import util.ValidationUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = "/api/manager/room-equipment/copy-from-room")
public class RoomEquipmentCopyApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RoomService roomService = new RoomService();
    private final RoomEquipmentService roomEquipmentService = new RoomEquipmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long sourceRoomId = ValidationUtil.optionalPositiveLong(req.getParameter("sourceRoomId"), "Room");
        if (sourceRoomId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing source room.");
            return;
        }

        Room sourceRoom = roomService.getRoomById(sourceRoomId).orElse(null);
        if (sourceRoom == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Room not found.");
            return;
        }

        List<RoomEquipment> roomEquipments = roomEquipmentService.findRoomEquipments(sourceRoomId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("[");
            for (int i = 0; i < roomEquipments.size(); i++) {
                RoomEquipment roomEquipment = roomEquipments.get(i);
                if (i > 0) {
                    writer.write(",");
                }
                writer.write("{");
                writer.write("\"id\":" + roomEquipment.getEquipmentId() + ",");
                writer.write("\"name\":\"" + escapeJson(roomEquipment.getEquipmentName()) + "\",");
                writer.write("\"quantity\":" + roomEquipment.getQuantity() + ",");
                writer.write("\"status\":\"" + escapeJson(roomEquipment.getStatus()) + "\",");
                writer.write("\"note\":\"" + escapeJson(roomEquipment.getNote()) + "\"");
                writer.write("}");
            }
            writer.write("]");
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
