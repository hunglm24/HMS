package service;

import dao.EquipmentDao;
import model.Equipment;
import util.DBConnectionUtil;
import util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EquipmentService {
    private static final Set<String> EQUIPMENT_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> FILTER_STATUSES = Set.of("ACTIVE", "INACTIVE", "ALL");
    private static final Set<String> FILTER_FLAGS = Set.of("ALL", "YES", "NO");
    private final EquipmentDao equipmentDao;

    public EquipmentService() {
        this(new EquipmentDao());
    }

    public EquipmentService(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    // Return all equipment items for the management list.
    public List<Equipment> findEquipments() {
        List<Equipment> equipments = equipmentDao.findAll();
        return equipments == null ? Collections.emptyList() : equipments;
    }

    // Return filtered equipment items for the management list.
    public List<Equipment> findEquipments(String keyword, String status, String maintainable, String hasImage) {
        String normalizedKeyword = ValidationUtil.normalizeLower(keyword);
        String filterKeyword = normalizedKeyword.length() > 100
                ? normalizedKeyword.substring(0, 100)
                : normalizedKeyword;
        String normalizedStatus = ValidationUtil.optionalStatus(status, FILTER_STATUSES);
        String normalizedMaintainable = ValidationUtil.optionalStatus(maintainable, FILTER_FLAGS);
        String normalizedHasImage = ValidationUtil.optionalStatus(hasImage, FILTER_FLAGS);

        List<Equipment> equipments = findEquipments();
        if (equipments.isEmpty()) {
            return Collections.emptyList();
        }

        return equipments.stream()
                .filter(equipment -> matchesKeyword(equipment, filterKeyword))
                .filter(equipment -> normalizedStatus == null
                        || "ALL".equals(normalizedStatus)
                        || normalizedStatus.equalsIgnoreCase(equipment.getStatus()))
                .filter(equipment -> normalizedMaintainable == null
                        || "ALL".equals(normalizedMaintainable)
                        || ("YES".equals(normalizedMaintainable) && equipment != null && equipment.isMaintainable())
                        || ("NO".equals(normalizedMaintainable) && equipment != null && !equipment.isMaintainable()))
                .filter(equipment -> normalizedHasImage == null
                        || "ALL".equals(normalizedHasImage)
                        || ("YES".equals(normalizedHasImage) && equipment != null && !ValidationUtil.isBlank(equipment.getImageUrl()))
                        || ("NO".equals(normalizedHasImage) && equipment != null && ValidationUtil.isBlank(equipment.getImageUrl())))
                .toList();
    }

    // Find one equipment item by id.
    public Equipment findEquipmentById(long id) {
        return equipmentDao.findById(id).orElse(null);
    }

    // Create a new equipment row after validation and uniqueness checks.
    public boolean createEquipment(Equipment equipment) throws SQLException {
        return persistEquipment(equipment, false);
    }

    // Update an existing equipment row after validation and uniqueness checks.
    public boolean updateEquipment(Equipment equipment) throws SQLException {
        return persistEquipment(equipment, true);
    }

    // Return the allowed status list used by the form radios.
    public List<String> findStatuses() {
        return List.of("ACTIVE", "INACTIVE");
    }

    // Check whether the equipment matches the typed keyword.
    private boolean matchesKeyword(Equipment equipment, String keyword) {
        if (ValidationUtil.isBlank(keyword)) {
            return true;
        }
        String name = ValidationUtil.normalizeLower(equipment == null ? null : equipment.getName());
        String description = ValidationUtil.normalizeLower(equipment == null ? null : equipment.getDescription());
        return name.contains(keyword) || description.contains(keyword);
    }

    // Validate the core equipment fields before persistence.
    private void validateEquipmentCoreFields(Equipment equipment) {
        ValidationUtil.requireTrue(equipment != null, "Equipment is required.");
        equipment.setName(ValidationUtil.requireText(equipment.getName(), "Equipment name", 2, 100));
        equipment.setDescription(ValidationUtil.optionalText(equipment.getDescription(), 500));
        equipment.setStatus(ValidationUtil.requireStatus(equipment.getStatus(), "Status", EQUIPMENT_STATUSES));
        equipment.setDefaultCompensationPrice(ValidationUtil.requirePositiveBigDecimal(
                equipment.getDefaultCompensationPrice() == null ? null : equipment.getDefaultCompensationPrice().toPlainString(),
                "Compensation price"
        ));
    }

    // Ensure the equipment name is unique.
    private void ensureEquipmentNameUnique(String name, Long excludedEquipmentId) {
        String normalizedName = ValidationUtil.normalizeLower(name);
        boolean duplicate = findEquipments().stream()
                .filter(equipment -> equipment != null)
                .filter(equipment -> excludedEquipmentId == null
                        || !excludedEquipmentId.equals(equipment.getId()))
                .anyMatch(equipment -> normalizedName.equals(ValidationUtil.normalizeLower(equipment.getName())));
        ValidationUtil.requireTrue(!duplicate, "Equipment name already exists.");
    }

    // Shared create/update persistence flow.
    private boolean persistEquipment(Equipment equipment, boolean updating) throws SQLException {
        validateEquipmentCoreFields(equipment);
        if (updating) {
            ValidationUtil.requireTrue(equipment.getId() != null && equipment.getId() > 0L, "Equipment id is required.");
            ensureEquipmentNameUnique(equipment.getName(), equipment.getId());
        } else {
            ensureEquipmentNameUnique(equipment.getName(), null);
        }

        try (Connection conn = DBConnectionUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (updating) {
                    ValidationUtil.requireTrue(equipmentDao.update(conn, equipment) > 0, "Equipment not found.");
                } else {
                    equipmentDao.insert(conn, equipment);
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException sqlException) {
                    throw sqlException;
                }
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new SQLException("Failed to save equipment.", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
