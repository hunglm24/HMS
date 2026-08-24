package controller.page.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Equipment;
import service.EquipmentService;
import util.LocalFileUtil;
import util.MultipartUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/manager/equipment",
        "/manager/equipment/new",
        "/manager/equipment/edit",
        "/manager/equipment/create",
        "/manager/equipment/update"
})
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 8L * 1024 * 1024
)
public class EquipmentManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final String EQUIPMENT_IMAGE_DIR = "uploads/equipment";
    private static final java.util.Set<String> ALLOWED_IMAGE_EXTENSIONS = java.util.Set.of("jpg", "jpeg", "png", "webp");
    private static final java.util.Set<String> ALLOWED_IMAGE_CONTENT_TYPES = java.util.Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private EquipmentService equipmentService;

    @Override
    public void init() throws ServletException {
        // Initialize the service once for the servlet lifecycle.
        equipmentService = new EquipmentService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/equipment".equals(path)) {
            prepareEquipmentListPage(req);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment.jsp").forward(req, resp);
            return;
        }

        if ("/manager/equipment/new".equals(path)) {
            prepareEquipmentFormLookup(req);
            prepareEquipmentFormView(req, new Equipment(), false, null);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
            return;
        }

        if ("/manager/equipment/edit".equals(path)) {
            handleEditEquipment(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/manager/equipment/create".equals(req.getServletPath())) {
            handlePersistEquipment(req, resp, false);
            return;
        }

        if ("/manager/equipment/update".equals(req.getServletPath())) {
            handlePersistEquipment(req, resp, true);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Build the equipment list page model.
    private void prepareEquipmentListPage(HttpServletRequest req) {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        String maintainable = req.getParameter("maintainable");
        String hasImage = req.getParameter("hasImage");
        String normalizedStatus = ValidationUtil.normalizeUpper(status);
        if (ValidationUtil.isBlank(normalizedStatus)) {
            normalizedStatus = "ALL";
        }

        String normalizedMaintainable = ValidationUtil.normalizeUpper(maintainable);
        if (ValidationUtil.isBlank(normalizedMaintainable)) {
            normalizedMaintainable = "ALL";
        }

        String normalizedHasImage = ValidationUtil.normalizeUpper(hasImage);
        if (ValidationUtil.isBlank(normalizedHasImage)) {
            normalizedHasImage = "ALL";
        }

        List<Equipment> equipments = equipmentService.findEquipments(keyword, normalizedStatus, normalizedMaintainable, normalizedHasImage);
        req.setAttribute("equipments", equipments);
        req.setAttribute("keyword", ValidationUtil.normalizeText(keyword));
        req.setAttribute("status", normalizedStatus);
        req.setAttribute("maintainable", normalizedMaintainable);
        req.setAttribute("hasImage", normalizedHasImage);
        long totalEquipmentCount = equipments.size();
        long activeEquipmentCount = equipments.stream()
                .filter(equipment -> equipment != null && "ACTIVE".equalsIgnoreCase(equipment.getStatus()))
                .count();
        long maintainableEquipmentCount = equipments.stream()
                .filter(equipment -> equipment != null && equipment.isMaintainable())
                .count();
        long inactiveEquipmentCount = equipments.stream()
                .filter(equipment -> equipment != null && "INACTIVE".equalsIgnoreCase(equipment.getStatus()))
                .count();

        req.setAttribute("equipmentCount", totalEquipmentCount);
        req.setAttribute("activeEquipmentCount", activeEquipmentCount);
        req.setAttribute("maintainableEquipmentCount", maintainableEquipmentCount);
        req.setAttribute("inactiveEquipmentCount", inactiveEquipmentCount);
    }

    // Load lookup data for the equipment form.
    private void prepareEquipmentFormLookup(HttpServletRequest req) {
        req.setAttribute("equipmentStatuses", equipmentService.findStatuses());
    }

    // Build the shared form view model.
    private void prepareEquipmentFormView(HttpServletRequest req, Equipment form, boolean updating, Long equipmentId) {
        req.setAttribute("form", form);
        req.setAttribute("isEditMode", updating);
        req.setAttribute("equipmentId", equipmentId != null ? equipmentId : form.getId());
        req.setAttribute("equipmentFormAction", updating ? "/manager/equipment/update" : "/manager/equipment/create");
        req.setAttribute("equipmentPageTitle", updating ? "Edit Equipment | HMS" : "New Equipment | HMS");
        req.setAttribute("equipmentPageHeading", updating ? "Edit Equipment" : "New Equipment");
        req.setAttribute(
                "equipmentPageSubtitle",
                updating
                        ? "Update the equipment information and cover image."
                        : "Create a new equipment item with local image upload."
        );
        req.setAttribute("equipmentBackUrl", "/manager/equipment");
        req.setAttribute("equipmentSubmitLabel", updating ? "Update" : "Save");
        req.setAttribute("equipmentExistingImageUrl", form != null ? form.getImageUrl() : null);
    }

    // Load the edit form for the selected equipment item.
    private void handleEditEquipment(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long equipmentId = ValidationUtil.optionalPositiveLong(req.getParameter("id"), "Equipment");
        if (equipmentId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Equipment equipment = equipmentService.findEquipmentById(equipmentId);
        if (equipment == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        prepareEquipmentFormLookup(req);
        prepareEquipmentFormView(req, equipment, true, equipmentId);
        req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
    }

    // Handle create/update submissions from the shared equipment form.
    private void handlePersistEquipment(HttpServletRequest req, HttpServletResponse resp, boolean updating)
            throws ServletException, IOException {
        Map<String, String> errors = new LinkedHashMap<>();
        Equipment equipment = new Equipment();
        Equipment existingEquipment = null;
        Long equipmentId = null;

        if (updating) {
            try {
                equipmentId = ValidationUtil.requirePositiveLong(req.getParameter("id"), "Equipment");
                existingEquipment = equipmentService.findEquipmentById(equipmentId);
                if (existingEquipment == null) {
                    errors.put("general", "Equipment not found.");
                } else {
                    equipment.setId(existingEquipment.getId());
                    equipment.setImageUrl(existingEquipment.getImageUrl());
                }
            } catch (IllegalArgumentException ex) {
                errors.put("general", ex.getMessage());
            }
        }

        String name = ValidationUtil.normalizeText(req.getParameter("name"));
        String description = req.getParameter("description");
        String priceRaw = req.getParameter("defaultCompensationPrice");
        String statusRaw = req.getParameter("status");
        Part imagePart = null;

        try {
            equipment.setName(ValidationUtil.requireText(name, "Equipment name", 2, 100));
        } catch (IllegalArgumentException ex) {
            errors.put("name", ex.getMessage());
        }

        try {
            equipment.setDescription(ValidationUtil.optionalText(description, 500));
        } catch (IllegalArgumentException ex) {
            errors.put("description", ex.getMessage());
        }

        try {
            equipment.setDefaultCompensationPrice(ValidationUtil.requirePositiveBigDecimal(priceRaw, "Compensation price"));
        } catch (IllegalArgumentException ex) {
            errors.put("defaultCompensationPrice", ex.getMessage());
        }

        try {
            equipment.setStatus(ValidationUtil.requireStatus(statusRaw, "Status", java.util.Set.of("ACTIVE", "INACTIVE")));
        } catch (IllegalArgumentException ex) {
            errors.put("status", ex.getMessage());
        }

        try {
            imagePart = req.getPart("imageFile");
            if (imagePart != null && imagePart.getSize() > 0) {
                MultipartUtil.validateImagePart(
                        imagePart,
                        MAX_IMAGE_SIZE,
                        ALLOWED_IMAGE_EXTENSIONS,
                        ALLOWED_IMAGE_CONTENT_TYPES,
                        "Equipment image"
                );
            }
        } catch (IllegalArgumentException ex) {
            errors.put("imageFile", ex.getMessage());
        } catch (java.io.IOException ex) {
            errors.put("imageFile", "Unable to read equipment image.");
        }

        if (!errors.isEmpty()) {
            prepareEquipmentFormLookup(req);
            req.setAttribute("errors", errors);
            prepareEquipmentFormView(req, equipment, updating, equipmentId);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
            return;
        }

        String uploadedImagePath = null;
        String previousImagePath = equipment.getImageUrl();
        try {
            if (imagePart != null && imagePart.getSize() > 0) {
                uploadedImagePath = LocalFileUtil.saveImagePart(
                        imagePart,
                        req.getServletContext(),
                        EQUIPMENT_IMAGE_DIR,
                        equipment.getName()
                );
                equipment.setImageUrl(uploadedImagePath);
            }

            if (updating) {
                equipmentService.updateEquipment(equipment);
            } else {
                equipmentService.createEquipment(equipment);
            }

            if (updating && uploadedImagePath != null && previousImagePath != null && !previousImagePath.equals(uploadedImagePath)) {
                LocalFileUtil.deleteByWebPath(req.getServletContext(), previousImagePath);
            }

            req.getSession().setAttribute("toastMessage", updating ? "Equipment updated successfully." : "Equipment created successfully.");
            req.getSession().setAttribute("toastType", "success");
            resp.sendRedirect(req.getContextPath() + "/manager/equipment");
        } catch (IllegalArgumentException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", ex.getMessage());
            prepareEquipmentFormLookup(req);
            req.setAttribute("errors", errors);
            prepareEquipmentFormView(req, equipment, updating, equipmentId);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
        } catch (SQLException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", updating ? "Failed to update equipment." : "Failed to create equipment.");
            prepareEquipmentFormLookup(req);
            req.setAttribute("errors", errors);
            prepareEquipmentFormView(req, equipment, updating, equipmentId);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", ex.getMessage() == null
                    ? (updating ? "Unexpected error while updating equipment." : "Unexpected error while creating equipment.")
                    : ex.getMessage());
            prepareEquipmentFormLookup(req);
            req.setAttribute("errors", errors);
            prepareEquipmentFormView(req, equipment, updating, equipmentId);
            req.getRequestDispatcher("/WEB-INF/views/manager/equipment-form.jsp").forward(req, resp);
        } catch (IOException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            throw new ServletException("Failed to save equipment image", ex);
        }
    }
}
