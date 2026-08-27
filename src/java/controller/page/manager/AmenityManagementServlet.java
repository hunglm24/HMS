package controller.page.manager;

import model.Amenity;
import service.AmenityService;
import service.AuditLogService;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/manager/amenities", "/manager/amenity", "/manager/amenity/create", "/manager/amenity/new", "/manager/amenity/edit", "/manager/amenity/update"})
public class AmenityManagementServlet extends HttpServlet {
    private final AmenityService amenityService = new AmenityService();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/amenities".equals(path) || "/manager/amenity".equals(path)) {
            prepareAmenityListPage(req);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenities.jsp").forward(req, resp);
            return;
        }

        if ("/manager/amenity/new".equals(path)) {
            prepareAmenityFormLookup(req);
            prepareAmenityFormView(req, new Amenity(), false, null);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
            return;
        }

        if ("/manager/amenity/edit".equals(path)) {
            handleEditAmenity(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/manager/amenity/create".equals(req.getServletPath())) {
            handlePersistAmenity(req, resp, false);
            return;
        }

        if ("/manager/amenity/update".equals(req.getServletPath())) {
            handlePersistAmenity(req, resp, true);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void prepareAmenityListPage(HttpServletRequest req) {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        String normalizedStatus = ValidationUtil.normalizeUpper(status);
        if (ValidationUtil.isBlank(normalizedStatus)) {
            normalizedStatus = "ALL";
        }

        List<Amenity> amenities = amenityService.findAmenities(keyword, normalizedStatus);
        req.setAttribute("amenities", amenities);
        req.setAttribute("keyword", ValidationUtil.normalizeText(keyword));
        req.setAttribute("status", normalizedStatus);
        req.setAttribute("amenityCount", amenities.size());
        req.setAttribute(
                "activeAmenityCount",
                amenities.stream()
                        .filter(amenity -> amenity != null && "ACTIVE".equalsIgnoreCase(amenity.getStatus()))
                        .count()
        );
    }



    private void prepareAmenityFormLookup(HttpServletRequest req) {
        req.setAttribute("amenityStatuses", amenityService.findStatuses());
    }

    private void prepareAmenityFormView(HttpServletRequest req, Amenity form, boolean updating, Long amenityId) {
        req.setAttribute("form", form);
        req.setAttribute("isEditMode", updating);
        req.setAttribute("amenityId", amenityId != null ? amenityId : form.getId());
        req.setAttribute("amenityFormAction", updating ? "/manager/amenity/update" : "/manager/amenity/create");
        req.setAttribute("amenityPageTitle", updating ? "Chỉnh sửa tiện nghi | HMS" : "Thêm tiện nghi mới | HMS");
        req.setAttribute("amenityPageHeading", updating ? "Chỉnh sửa tiện nghi" : "Thêm tiện nghi mới");
        req.setAttribute(
                "amenityPageSubtitle",
                updating
                        ? "Cập nhật thông tin chi tiết và biểu tượng của tiện nghi."
                        : "Tạo mới tiện nghi để gán vào các loại phòng."
        );
        req.setAttribute("amenityBackUrl", "/manager/amenities");
        req.setAttribute("amenitySubmitLabel", updating ? "Cập nhật" : "Lưu tiện nghi");
    }

    private void handleEditAmenity(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long amenityId = ValidationUtil.optionalPositiveLong(req.getParameter("id"), "Amenity");
        if (amenityId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Amenity amenity = amenityService.findAmenityById(amenityId);
        if (amenity == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        prepareAmenityFormLookup(req);
        prepareAmenityFormView(req, amenity, true, amenityId);
        req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
    }

    private void handlePersistAmenity(HttpServletRequest req, HttpServletResponse resp, boolean updating)
            throws ServletException, IOException {
        Map<String, String> errors = new LinkedHashMap<>();
        Amenity amenity = new Amenity();
        Amenity existingAmenity = null;
        Long amenityId = null;

        if (updating) {
            try {
                amenityId = ValidationUtil.requirePositiveLong(req.getParameter("id"), "Amenity");
                existingAmenity = amenityService.findAmenityById(amenityId);
                if (existingAmenity == null) {
                    errors.put("general", "Không tìm thấy tiện nghi.");
                } else {
                    amenity.setId(existingAmenity.getId());
                }
            } catch (IllegalArgumentException ex) {
                errors.put("general", ex.getMessage());
            }
        }

        String name = ValidationUtil.normalizeText(req.getParameter("name"));
        String description = req.getParameter("description");
        String icon = ValidationUtil.normalizeText(req.getParameter("icon"));
        String statusRaw = req.getParameter("status");

        try {
            amenity.setName(ValidationUtil.requireText(name, "Tên tiện nghi", 2, 100));
        } catch (IllegalArgumentException ex) {
            errors.put("name", ex.getMessage());
        }

        try {
            amenity.setDescription(ValidationUtil.optionalText(description, 500));
        } catch (IllegalArgumentException ex) {
            errors.put("description", ex.getMessage());
        }

        try {
            amenity.setIcon(ValidationUtil.requireText(icon, "Mã icon", 2, 50));
        } catch (IllegalArgumentException ex) {
            errors.put("icon", ex.getMessage());
        }

        try {
            amenity.setStatus(ValidationUtil.requireStatus(statusRaw, "Trạng thái", java.util.Set.of("ACTIVE", "INACTIVE")));
        } catch (IllegalArgumentException ex) {
            errors.put("status", ex.getMessage());
        }

        if (!errors.isEmpty()) {
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
            return;
        }

        try {
            if (updating) {
                amenityService.updateAmenity(amenity);
                auditLogService.log(req, "UPDATE_AMENITY", "AMENITY", amenity.getId(),
                        "Updated amenity " + amenity.getName());
            } else {
                amenityService.createAmenity(amenity);
                auditLogService.log(req, "CREATE_AMENITY", "AMENITY", amenity.getId(),
                        "Created amenity " + amenity.getName());
            }

            req.getSession().setAttribute("message", updating ? "Cập nhật tiện nghi thành công." : "Thêm tiện nghi mới thành công.");
            resp.sendRedirect(req.getContextPath() + "/manager/amenities");
        } catch (IllegalArgumentException ex) {
            errors.put("general", ex.getMessage());
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        } catch (SQLException ex) {
            errors.put("general", updating ? "Lỗi cập nhật tiện nghi trong CSDL." : "Lỗi lưu tiện nghi vào CSDL.");
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            errors.put("general", ex.getMessage() == null
                    ? (updating ? "Lỗi không xác định khi cập nhật." : "Lỗi không xác định khi tạo mới.")
                    : ex.getMessage());
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        }
    }
}
