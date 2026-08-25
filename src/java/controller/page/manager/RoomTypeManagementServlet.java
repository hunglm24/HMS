package controller.page.manager;

import dto.RoomManagementPageData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Amenity;
import model.RoomType;
import service.RoomTypeService;
import service.AuditLogService;
import util.LocalFileUtil;
import util.MoneyUtil;
import util.MultipartUtil;
import util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {
        "/manager/room-types",
        "/manager/room-types/new",
        "/manager/room-types/create",
        "/manager/room-types/edit",
        "/manager/room-types/update",
        "/manager/room-types/toggle-status"
})
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 8L * 1024 * 1024
)
public class RoomTypeManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int ROOM_TYPES_PAGE_SIZE = 5;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String ALL_STATUS = "ALL";
    private static final long MAX_COVER_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final String ROOM_TYPE_IMAGE_DIR = "uploads/room-types";

    private RoomTypeService roomTypeService;
    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        roomTypeService = new RoomTypeService();
        auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/room-types".equals(path)) {
            prepareRoomTypesPage(req);
            req.getRequestDispatcher("/WEB-INF/views/manager/room-types.jsp").forward(req, resp);
            return;
        }

        if ("/manager/room-types/new".equals(path)) {
            prepareCreateForm(req);
            prepareRoomTypeFormView(req, new RoomType(), false, null, new LinkedHashSet<>());
            req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
            return;
        }

        if ("/manager/room-types/edit".equals(path)) {
            handleEditRoomType(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/manager/room-types/create".equals(path)) {
            handleCreateRoomType(req, resp);
            return;
        }

        if ("/manager/room-types/update".equals(path)) {
            handleUpdateRoomType(req, resp);
            return;
        }

        if ("/manager/room-types/toggle-status".equals(path)) {
            handleToggleRoomTypeStatus(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleCreateRoomType(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handlePersistRoomType(req, resp, false);
    }

    private void handleUpdateRoomType(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handlePersistRoomType(req, resp, true);
    }

    private void handleToggleRoomTypeStatus(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long roomTypeId = ValidationUtil.optionalPositiveLong(req.getParameter("id"), "Loại phòng");
        if (roomTypeId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            roomTypeService.toggleRoomTypeStatus(roomTypeId);
            auditLogService.log(req, "TOGGLE_ROOM_TYPE_STATUS", "ROOM_TYPE", roomTypeId,
                    "Toggled room type status for id " + roomTypeId);
            req.getSession().setAttribute("toastMessage", "Đã cập nhật trạng thái loại phòng.");
            req.getSession().setAttribute("toastType", "success");
            resp.sendRedirect(req.getContextPath() + "/manager/room-types?selectedRoomTypeId=" + roomTypeId);
        } catch (SQLException ex) {
            throw new ServletException("Không thể cập nhật trạng thái loại phòng", ex);
        }
    }

    private void handlePersistRoomType(HttpServletRequest req, HttpServletResponse resp, boolean updating) throws ServletException, IOException {
        Map<String, String> errors = new LinkedHashMap<>();
        RoomType existingRoomType = null;
        RoomType roomType = new RoomType();
        String roomTypeIdRaw = req.getParameter("id");
        Long roomTypeId = null;

        if (updating) {
            try {
                roomTypeId = ValidationUtil.requirePositiveLong(roomTypeIdRaw, "Loại phòng");
                existingRoomType = roomTypeService.findRoomTypeById(roomTypeId);
                if (existingRoomType == null) {
                    errors.put("general", "Không tìm thấy loại phòng.");
                } else {
                    roomType.setId(existingRoomType.getId());
                    roomType.setImageUrl(existingRoomType.getImageUrl());
                }
            } catch (IllegalArgumentException ex) {
                errors.put("general", ex.getMessage());
            }
        }

        String name = ValidationUtil.normalizeText(req.getParameter("name"));
        String description = req.getParameter("description");
        String capacityRaw = req.getParameter("capacity");
        String basePriceRaw = req.getParameter("basePrice");
        String statusRaw = req.getParameter("status");
        String sizeM2Raw = req.getParameter("sizeM2");
        String bedTypeRaw = req.getParameter("bedType");
        List<Long> amenityIds = parseAmenityIds(req.getParameterValues("amenityIds"), errors);
        Part coverImagePart = null;

        try {
            roomType.setName(ValidationUtil.requireText(name, "Tên loại phòng", 2, 100));
        } catch (IllegalArgumentException ex) {
            errors.put("name", ex.getMessage());
        }

        try {
            roomType.setDescription(ValidationUtil.optionalText(description, 500));
        } catch (IllegalArgumentException ex) {
            errors.put("description", ex.getMessage());
        }

        try {
            roomType.setSizeM2(ValidationUtil.optionalBigDecimal(sizeM2Raw, "Diện tích phòng"));
        } catch (IllegalArgumentException ex) {
            errors.put("sizeM2", ex.getMessage());
        }

        try {
            roomType.setBedType(ValidationUtil.optionalText(bedTypeRaw, 100));
        } catch (IllegalArgumentException ex) {
            errors.put("bedType", ex.getMessage());
        }

        try {
            roomType.setCapacity(ValidationUtil.requirePositiveInt(capacityRaw, "Sức chứa"));
        } catch (IllegalArgumentException ex) {
            errors.put("capacity", ex.getMessage());
        }

        try {
            roomType.setBasePrice(MoneyUtil.parseVndMoney(basePriceRaw, "Giá cơ bản"));
        } catch (IllegalArgumentException ex) {
            errors.put("basePrice", ex.getMessage());
        }

        try {
            String status = ValidationUtil.optionalStatus(statusRaw, Set.of("ACTIVE", "INACTIVE"));
            roomType.setStatus(status == null ? ACTIVE_STATUS : status);
        } catch (IllegalArgumentException ex) {
            errors.put("status", ex.getMessage());
        }

        try {
            coverImagePart = req.getPart("coverImage");
            if (coverImagePart != null && coverImagePart.getSize() > 0) {
                MultipartUtil.validateImagePart(
                        coverImagePart,
                        MAX_COVER_IMAGE_SIZE,
                        ALLOWED_IMAGE_EXTENSIONS,
                        ALLOWED_IMAGE_CONTENT_TYPES,
                        "Ảnh đại diện"
                );
            } else if (!updating) {
                errors.put("coverImage", "Vui lòng chọn ảnh đại diện.");
            }
        } catch (IllegalArgumentException ex) {
            errors.put("coverImage", ex.getMessage());
        } catch (IOException ex) {
            errors.put("coverImage", "Không thể đọc ảnh đại diện.");
        }

        if (!errors.isEmpty()) {
            prepareCreateForm(req);
            req.setAttribute("errors", errors);
            prepareRoomTypeFormView(req, roomType, updating, roomTypeId, new LinkedHashSet<>(amenityIds));
            req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
            return;
        }

        String uploadedImagePath = null;
        String previousImagePath = roomType.getImageUrl();

        try {
            if (coverImagePart != null && coverImagePart.getSize() > 0) {
                uploadedImagePath = LocalFileUtil.saveImagePart(
                        coverImagePart,
                        req.getServletContext(),
                        ROOM_TYPE_IMAGE_DIR,
                        roomType.getName()
                );
                roomType.setImageUrl(uploadedImagePath);
            }

            if (updating) {
                roomTypeService.updateRoomType(roomType, amenityIds);
                auditLogService.log(req, "UPDATE_ROOM_TYPE", "ROOM_TYPE", roomType.getId(),
                        "Updated room type " + roomType.getName());
            } else {
                roomTypeService.createRoomType(roomType, amenityIds);
                auditLogService.log(req, "CREATE_ROOM_TYPE", "ROOM_TYPE", roomType.getId(),
                        "Created room type " + roomType.getName());
            }

            if (updating && uploadedImagePath != null && previousImagePath != null && !previousImagePath.equals(uploadedImagePath)) {
                LocalFileUtil.deleteByWebPath(req.getServletContext(), previousImagePath);
            }

            req.getSession().setAttribute("toastMessage", updating ? "Đã cập nhật loại phòng." : "Đã tạo loại phòng.");
            req.getSession().setAttribute("toastType", "success");
            resp.sendRedirect(req.getContextPath() + "/manager/room-types");
        } catch (IllegalArgumentException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", ex.getMessage());
            prepareCreateForm(req);
            req.setAttribute("errors", errors);
            prepareRoomTypeFormView(req, roomType, updating, roomTypeId, new LinkedHashSet<>(amenityIds));
            req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
        } catch (SQLException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", updating ? "Không thể cập nhật loại phòng." : "Không thể tạo loại phòng.");
            prepareCreateForm(req);
            req.setAttribute("errors", errors);
            prepareRoomTypeFormView(req, roomType, updating, roomTypeId, new LinkedHashSet<>(amenityIds));
            req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
        } catch (RuntimeException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            errors.put("general", ex.getMessage() == null
                    ? (updating ? "Đã xảy ra lỗi khi cập nhật loại phòng." : "Đã xảy ra lỗi khi tạo loại phòng.")
                    : ex.getMessage());
            prepareCreateForm(req);
            req.setAttribute("errors", errors);
            prepareRoomTypeFormView(req, roomType, updating, roomTypeId, new LinkedHashSet<>(amenityIds));
            req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
        } catch (IOException ex) {
            LocalFileUtil.deleteByWebPath(req.getServletContext(), uploadedImagePath);
            throw new ServletException("Không thể lưu ảnh đại diện", ex);
        }
    }

    private void prepareCreateForm(HttpServletRequest req) {
        List<String> statuses = roomTypeService.findCreateStatuses();
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of("ACTIVE", "INACTIVE");
        }
        req.setAttribute("roomTypeStatuses", statuses);
        req.setAttribute("bedTypes", roomTypeService.findBedTypes());
        req.setAttribute("amenities", roomTypeService.findActiveAmenities());
    }

    private void prepareRoomTypeFormView(HttpServletRequest req, RoomType form, boolean updating, Long roomTypeId, Set<Long> selectedAmenityIds) {
        req.setAttribute("form", form);
        req.setAttribute("selectedAmenityIds", selectedAmenityIds == null ? new LinkedHashSet<Long>() : selectedAmenityIds);
        req.setAttribute("isEditMode", updating);
        req.setAttribute("roomTypeFormAction", updating ? "/manager/room-types/update" : "/manager/room-types/create");
        req.setAttribute("roomTypePageTitle", updating ? "Sửa loại phòng | HMS" : "Thêm loại phòng | HMS");
        req.setAttribute("roomTypePageHeading", updating ? "Sửa loại phòng" : "Thêm loại phòng");
        req.setAttribute(
                "roomTypePageSubtitle",
                updating
                        ? "Cập nhật thông tin cơ bản, cấu hình và ảnh đại diện của loại phòng."
                        : "Tạo loại phòng với các thông tin cơ bản bắt buộc và cấu hình bổ sung tùy chọn."
        );
        req.setAttribute("roomTypeBackUrl", "/manager/room-types");
        req.setAttribute("roomTypeSubmitLabel", updating ? "Cập nhật" : "Lưu");
        req.setAttribute("roomTypeId", roomTypeId != null ? roomTypeId : form.getId());
        req.setAttribute("roomTypeExistingImageUrl", form != null ? form.getImageUrl() : null);
    }

    private void handleEditRoomType(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long roomTypeId = ValidationUtil.optionalPositiveLong(req.getParameter("id"), "Loại phòng");
        if (roomTypeId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        RoomType roomType = roomTypeService.findRoomTypeById(roomTypeId);
        if (roomType == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        prepareCreateForm(req);
        Set<Long> selectedAmenityIds = roomTypeService.findAmenitiesByRoomTypeId(roomTypeId).stream()
                .map(Amenity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        prepareRoomTypeFormView(req, roomType, true, roomTypeId, selectedAmenityIds);
        req.getRequestDispatcher("/WEB-INF/views/manager/room-type-create.jsp").forward(req, resp);
    }

    private void prepareRoomTypesPage(HttpServletRequest req) {
        RoomManagementPageData pageData = new RoomManagementPageData();
        String keyword = req.getParameter("keyword");
        String roomTypeStatus = req.getParameter("roomTypeStatus");
        String roomTypeSort = ValidationUtil.isBlank(req.getParameter("sort")) ? "popular" : req.getParameter("sort");
        String roomTypeDirection = normalizeSortDirection(req.getParameter("direction"));
        if (ValidationUtil.isBlank(roomTypeStatus)) {
            roomTypeStatus = ALL_STATUS;
        }
        String selectedRoomTypeIdRaw = req.getParameter("selectedRoomTypeId");
        String pageRaw = req.getParameter("page");

        pageData.setKeyword(keyword);
        pageData.setRoomTypeStatus(roomTypeStatus);
        List<RoomType> allRoomTypes = ALL_STATUS.equalsIgnoreCase(roomTypeStatus)
                ? roomTypeService.findRoomTypes(keyword, null, roomTypeSort, roomTypeDirection)
                : roomTypeService.findRoomTypes(keyword, roomTypeStatus, roomTypeSort, roomTypeDirection);

        int currentPage = parsePage(pageRaw);
        RoomType selectedRoomType = resolveSelectedRoomType(allRoomTypes, selectedRoomTypeIdRaw);
        int selectedIndex = findRoomTypeIndex(allRoomTypes, selectedRoomType);
        if (selectedIndex >= 0 && ValidationUtil.isBlank(pageRaw)) {
            currentPage = selectedIndex / ROOM_TYPES_PAGE_SIZE + 1;
        }

        int totalPages = Math.max(1, (int) Math.ceil(allRoomTypes.size() / (double) ROOM_TYPES_PAGE_SIZE));
        currentPage = Math.min(Math.max(currentPage, 1), totalPages);
        int fromIndex = Math.min((currentPage - 1) * ROOM_TYPES_PAGE_SIZE, allRoomTypes.size());
        int toIndex = Math.min(fromIndex + ROOM_TYPES_PAGE_SIZE, allRoomTypes.size());
        List<RoomType> pagedRoomTypes = allRoomTypes.subList(fromIndex, toIndex);

        int roomTypeTotalCount = allRoomTypes.size();
        int roomTypeActiveCount = (int) allRoomTypes.stream()
                .filter(roomType -> ACTIVE_STATUS.equalsIgnoreCase(roomType.getStatus()))
                .count();
        int roomTypeInactiveCount = (int) allRoomTypes.stream()
                .filter(roomType -> "INACTIVE".equalsIgnoreCase(roomType.getStatus()))
                .count();

        if (selectedRoomType == null && !ValidationUtil.isBlank(selectedRoomTypeIdRaw)) {
            Long selectedRoomTypeId = ValidationUtil.optionalPositiveLong(selectedRoomTypeIdRaw, "Loại phòng");
            if (selectedRoomTypeId != null) {
                selectedRoomType = roomTypeService.findRoomTypeById(selectedRoomTypeId);
            }
        }

        if (selectedRoomType == null && !pagedRoomTypes.isEmpty()) {
            selectedRoomType = pagedRoomTypes.get(0);
        }

        pageData.setRoomTypes(pagedRoomTypes);
        req.setAttribute("pageData", pageData);
        req.setAttribute("roomTypes", pagedRoomTypes);
        req.setAttribute("roomTypeSort", roomTypeSort);
        req.setAttribute("roomTypeDirection", roomTypeDirection);
        req.setAttribute("roomTypeNextDirection", "ASC".equals(roomTypeDirection) ? "DESC" : "ASC");
        req.setAttribute("selectedRoomType", selectedRoomType);
        req.setAttribute("selectedRoomTypeId", selectedRoomType == null ? null : selectedRoomType.getId());
        req.setAttribute("roomTypeTotalCount", roomTypeTotalCount);
        req.setAttribute("roomTypeActiveCount", roomTypeActiveCount);
        req.setAttribute("roomTypeInactiveCount", roomTypeInactiveCount);
        req.setAttribute(
                "selectedRoomTypeAmenities",
                selectedRoomType == null ? List.of() : roomTypeService.findAmenitiesByRoomTypeId(selectedRoomType.getId())
        );
        req.setAttribute("paginationCurrentPage", currentPage);
        req.setAttribute("paginationTotalPages", totalPages);
        req.setAttribute("paginationPrevUrl", buildRoomTypesPageUrl(req, keyword, roomTypeStatus, roomTypeSort, roomTypeDirection, currentPage - 1));
        req.setAttribute("paginationNextUrl", buildRoomTypesPageUrl(req, keyword, roomTypeStatus, roomTypeSort, roomTypeDirection, currentPage + 1));
    }

    private int parsePage(String pageRaw) {
        if (ValidationUtil.isBlank(pageRaw)) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(pageRaw));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private int findRoomTypeIndex(List<RoomType> roomTypes, RoomType selectedRoomType) {
        if (roomTypes == null || roomTypes.isEmpty() || selectedRoomType == null || selectedRoomType.getId() == null) {
            return -1;
        }
        for (int i = 0; i < roomTypes.size(); i++) {
            RoomType roomType = roomTypes.get(i);
            if (roomType != null && roomType.getId() != null && roomType.getId().equals(selectedRoomType.getId())) {
                return i;
            }
        }
        return -1;
    }

    private String buildRoomTypesPageUrl(HttpServletRequest req, String keyword, String roomTypeStatus,
                                         String roomTypeSort, String roomTypeDirection, int page) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/manager/room-types?");
        boolean hasParam = false;
        if (!ValidationUtil.isBlank(keyword)) {
            url.append("keyword=").append(urlEncode(keyword));
            hasParam = true;
        }
        if (!ValidationUtil.isBlank(roomTypeStatus)) {
            if (hasParam) {
                url.append('&');
            }
            url.append("roomTypeStatus=").append(urlEncode(roomTypeStatus));
            hasParam = true;
        }
        if (hasParam) {
            url.append('&');
        }
        if (!ValidationUtil.isBlank(roomTypeSort)) {
            url.append("sort=").append(urlEncode(roomTypeSort));
            hasParam = true;
        }
        if (hasParam) {
            url.append('&');
        }
        if (!ValidationUtil.isBlank(roomTypeDirection)) {
            url.append("direction=").append(urlEncode(roomTypeDirection));
            hasParam = true;
        }
        if (hasParam) {
            url.append('&');
        }
        url.append("page=").append(Math.max(page, 1));
        return url.toString();
    }

    private String normalizeSortDirection(String directionRaw) {
        return "ASC".equalsIgnoreCase(directionRaw) ? "ASC" : "DESC";
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
            return value;
        }
    }

    private RoomType resolveSelectedRoomType(List<RoomType> roomTypes, String selectedRoomTypeIdRaw) {
        if (roomTypes == null || roomTypes.isEmpty() || ValidationUtil.isBlank(selectedRoomTypeIdRaw)) {
            return null;
        }

        try {
            long selectedRoomTypeId = ValidationUtil.requirePositiveLong(selectedRoomTypeIdRaw, "Loại phòng");
            for (RoomType roomType : roomTypes) {
                if (roomType != null && roomType.getId() != null && roomType.getId() == selectedRoomTypeId) {
                    return roomType;
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid selection and fall back to the first item.
        }
        return null;
    }

    private List<Long> parseAmenityIds(String[] rawIds, Map<String, String> errors) {
        List<Long> amenityIds = new ArrayList<>();
        if (rawIds == null || rawIds.length == 0) {
            return amenityIds;
        }

        for (String rawId : rawIds) {
            if (ValidationUtil.isBlank(rawId)) {
                continue;
            }
            try {
                long amenityId = ValidationUtil.requirePositiveLong(rawId, "Tiện ích");
                amenityIds.add(amenityId);
            } catch (IllegalArgumentException ex) {
                errors.put("amenityIds", ex.getMessage());
            }
        }
        return amenityIds;
    }
}
