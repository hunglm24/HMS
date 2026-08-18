package controller.page.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Amenity;
import service.AmenityService;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/manager/amenity",
        "/manager/amenity/new",
        "/manager/amenity/edit",
        "/manager/amenity/create",
        "/manager/amenity/update"
})
public class AmenityManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AmenityService amenityService;

    @Override
    public void init() throws ServletException {
        amenityService = new AmenityService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/amenity".equals(path)) {
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

    // Build the amenity list page model.
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

    // Load lookup data for the amenity form.
    private void prepareAmenityFormLookup(HttpServletRequest req) {
        req.setAttribute("amenityStatuses", amenityService.findStatuses());
    }

    // Build the shared form view model.
    private void prepareAmenityFormView(HttpServletRequest req, Amenity form, boolean updating, Long amenityId) {
        req.setAttribute("form", form);
        req.setAttribute("isEditMode", updating);
        req.setAttribute("amenityId", amenityId != null ? amenityId : form.getId());
        req.setAttribute("amenityFormAction", updating ? "/manager/amenity/update" : "/manager/amenity/create");
        req.setAttribute("amenityPageTitle", updating ? "Edit Amenity | HMS" : "New Amenity | HMS");
        req.setAttribute("amenityPageHeading", updating ? "Edit Amenity" : "New Amenity");
        req.setAttribute(
                "amenityPageSubtitle",
                updating
                        ? "Update the amenity details and icon."
                        : "Create a new amenity item for room configuration."
        );
        req.setAttribute("amenityBackUrl", "/manager/amenity");
        req.setAttribute("amenitySubmitLabel", updating ? "Update" : "Save");
    }

    // Load the edit form for the selected amenity item.
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

    // Handle create/update submissions from the shared amenity form.
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
                    errors.put("general", "Amenity not found.");
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
            amenity.setName(ValidationUtil.requireText(name, "Amenity name", 2, 100));
        } catch (IllegalArgumentException ex) {
            errors.put("name", ex.getMessage());
        }

        try {
            amenity.setDescription(ValidationUtil.optionalText(description, 500));
        } catch (IllegalArgumentException ex) {
            errors.put("description", ex.getMessage());
        }

        try {
            amenity.setIcon(ValidationUtil.requireText(icon, "Icon class", 2, 50));
        } catch (IllegalArgumentException ex) {
            errors.put("icon", ex.getMessage());
        }

        try {
            amenity.setStatus(ValidationUtil.requireStatus(statusRaw, "Status", java.util.Set.of("ACTIVE", "INACTIVE")));
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
            } else {
                amenityService.createAmenity(amenity);
            }

            req.getSession().setAttribute("toastMessage", updating ? "Amenity updated successfully." : "Amenity created successfully.");
            req.getSession().setAttribute("toastType", "success");
            resp.sendRedirect(req.getContextPath() + "/manager/amenity");
        } catch (IllegalArgumentException ex) {
            errors.put("general", ex.getMessage());
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        } catch (SQLException ex) {
            errors.put("general", updating ? "Failed to update amenity." : "Failed to create amenity.");
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            errors.put("general", ex.getMessage() == null
                    ? (updating ? "Unexpected error while updating amenity." : "Unexpected error while creating amenity.")
                    : ex.getMessage());
            prepareAmenityFormLookup(req);
            req.setAttribute("errors", errors);
            prepareAmenityFormView(req, amenity, updating, amenityId);
            req.getRequestDispatcher("/WEB-INF/views/manager/amenity-form.jsp").forward(req, resp);
        }
    }
}
