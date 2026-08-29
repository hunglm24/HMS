package service;

import dao.EquipmentDao;
import dao.RoomEquipmentDao;
import model.Equipment;
import model.RoomEquipment;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RoomEquipmentService {
    private static final Set<String> STATUSES = Set.of(
            "NORMAL", "DAMAGED", "MISSING", "WAITING_REPAIR", "WAITING_REPLACEMENT", "MAINTENANCE");
    private static final List<String> STATUS_ORDER = List.of(
            "NORMAL", "DAMAGED", "MISSING", "WAITING_REPAIR", "WAITING_REPLACEMENT", "MAINTENANCE");
    private static final Pattern NOTE_PATTERN = Pattern.compile("(?s)^.{1,50}$");

    private final RoomEquipmentDao roomEquipmentDao;
    private final EquipmentDao equipmentDao;

    public RoomEquipmentService() {
        this(new RoomEquipmentDao(), new EquipmentDao());
    }

    public RoomEquipmentService(RoomEquipmentDao roomEquipmentDao, EquipmentDao equipmentDao) {
        this.roomEquipmentDao = roomEquipmentDao;
        this.equipmentDao = equipmentDao;
    }

    public List<RoomEquipment> findRoomEquipments(long roomId) {
        if (roomId <= 0) {
            return List.of();
        }
        return roomEquipmentDao.findByRoomId(roomId);
    }

    public Map<Long, String> findRoomEquipmentSummaries(List<model.Room> rooms) {
        Map<Long, String> summaries = new LinkedHashMap<>();
        if (rooms == null || rooms.isEmpty()) {
            return summaries;
        }

        List<Long> roomIds = rooms.stream()
                .filter(room -> room != null && room.getId() > 0)
                .map(model.Room::getId)
                .toList();
        if (roomIds.isEmpty()) {
            return summaries;
        }

        Map<Long, List<RoomEquipment>> grouped = new LinkedHashMap<>();
        for (RoomEquipment roomEquipment : roomEquipmentDao.findByRoomIds(roomIds)) {
            if (roomEquipment == null || roomEquipment.getRoomId() <= 0) {
                continue;
            }
            grouped.computeIfAbsent(roomEquipment.getRoomId(), key -> new ArrayList<>()).add(roomEquipment);
        }

        for (model.Room room : rooms) {
            if (room == null || room.getId() <= 0) {
                continue;
            }
            summaries.put(room.getId(), formatSummary(grouped.get(room.getId())));
        }

        return summaries;
    }

    public List<Equipment> findAssignableEquipments() {
        List<Equipment> equipments = equipmentDao.findAll();
        if (equipments.isEmpty()) {
            return List.of();
        }
        return equipments.stream()
                .filter(equipment -> equipment != null && "ACTIVE".equalsIgnoreCase(equipment.getStatus()))
                .sorted(Comparator.comparing(equipment -> ValidationUtil.normalizeLower(equipment.getName())))
                .toList();
    }

    public List<String> findStatuses() {
        return new ArrayList<>(STATUS_ORDER);
    }

    public List<RoomEquipment> normalizeAssignments(List<RoomEquipment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        Map<Long, RoomEquipment> normalized = new LinkedHashMap<>();
        for (RoomEquipment assignment : assignments) {
            if (assignment == null) {
                continue;
            }

            boolean emptyEquipment = assignment.getEquipmentId() <= 0;
            boolean emptyQuantity = assignment.getQuantity() <= 0;
            boolean emptyStatus = ValidationUtil.isBlank(assignment.getStatus());
            boolean emptyNote = ValidationUtil.isBlank(assignment.getNote());
            if (emptyEquipment && emptyQuantity && emptyStatus && emptyNote) {
                continue;
            }

            ValidationUtil.requireTrue(assignment.getEquipmentId() > 0, "Equipment is required.");
            ValidationUtil.requireTrue(assignment.getQuantity() > 0, "Equipment quantity must be greater than 0.");
            Equipment equipment = equipmentDao.findById(assignment.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Equipment not found."));

            String status = ValidationUtil.optionalStatus(assignment.getStatus(), STATUSES);
            if (status == null) {
                status = "NORMAL";
            }
            String note = ValidationUtil.normalizeText(assignment.getNote());
            if (!note.isEmpty()) {
                note = ValidationUtil.requirePatternText(
                        note,
                        "Ghi chu thiet bi",
                        1,
                        50,
                        NOTE_PATTERN,
                        "Ghi chu thiet bi khong duoc qua 50 ky tu."
                );
            }

            ValidationUtil.requireTrue(!normalized.containsKey(assignment.getEquipmentId()),
                    "Duplicate equipment selected: " + equipment.getName());

            RoomEquipment cleaned = new RoomEquipment();
            cleaned.setId(assignment.getId());
            cleaned.setEquipmentId(equipment.getId());
            cleaned.setQuantity(assignment.getQuantity());
            cleaned.setStatus(status);
            cleaned.setNote(note.isEmpty() ? null : note);
            cleaned.setEquipmentName(equipment.getName());
            normalized.put(cleaned.getEquipmentId(), cleaned);
        }

        return List.copyOf(normalized.values());
    }

    public void replaceRoomEquipments(Connection conn, long roomId, List<RoomEquipment> assignments, Long updatedBy)
            throws SQLException {
        roomEquipmentDao.deleteByRoomId(conn, roomId);
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        for (RoomEquipment assignment : assignments) {
            assignment.setRoomId(roomId);
            assignment.setUpdatedBy(updatedBy);
            roomEquipmentDao.insert(conn, assignment);
        }
    }

    private String formatSummary(List<RoomEquipment> roomEquipments) {
        if (roomEquipments == null || roomEquipments.isEmpty()) {
            return "";
        }

        List<String> items = new ArrayList<>();
        for (RoomEquipment roomEquipment : roomEquipments) {
            if (roomEquipment == null) {
                continue;
            }
            String equipmentName = roomEquipment.getEquipmentName();
            if (ValidationUtil.isBlank(equipmentName)) {
                continue;
            }
            int quantity = roomEquipment.getQuantity();
            items.add(quantity > 1 ? equipmentName + " x" + quantity : equipmentName);
        }

        if (items.isEmpty()) {
            return "";
        }

        int maxVisibleItems = 3;
        if (items.size() <= maxVisibleItems) {
            return String.join(", ", items);
        }

        return String.join(", ", items.subList(0, maxVisibleItems)) + " +" + (items.size() - maxVisibleItems);
    }
}
