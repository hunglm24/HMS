<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><c:out value="${roomTypePageTitle}" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${cp}/assets/css/room-type-create.css?v=20260824-3">
</head>
<body class="room-management-body room-type-create-body">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="page-container room-type-create-page">
    <form id="roomTypeCreateForm" action="${cp}${roomTypeFormAction}" method="post" enctype="multipart/form-data" novalidate>
        <c:if test="${isEditMode}">
            <input type="hidden" name="id" value="${roomTypeId}" />
        </c:if>
        <section class="room-type-create-hero">
            <div class="room-type-create-hero__copy">
                <a class="room-type-create-back" href="${cp}${roomTypeBackUrl}">Quay lại danh sách loại phòng</a>
                <h1><c:out value="${roomTypePageHeading}" /></h1>
                <p><c:out value="${roomTypePageSubtitle}" /></p>
            </div>

            <div class="room-type-create-hero__actions">
                <button class="btn btn-secondary" type="button">Lưu nháp</button>
                <button class="btn btn-primary" type="submit"><c:out value="${roomTypeSubmitLabel}" /></button>
            </div>
        </section>

        <c:if test="${not empty errors.general}">
            <div class="room-type-create-form-alert">
                <c:out value="${errors.general}" />
            </div>
        </c:if>

        <section class="room-type-create-layout">
            <div class="room-type-create-column">
                <article class="panel room-type-create-card">
                    <header class="room-type-create-card__head">
                        <h2>Thông tin cơ bản</h2>
                        <p>Cột bên trái chứa các trường nhập chính và trạng thái kiểm tra dữ liệu.</p>
                    </header>

                    <div class="room-type-create-core-grid">
                        <label class="room-type-create-field room-type-create-field--left${not empty errors.name ? ' is-error' : ''}" data-validate-field="name">
                            <span>Tên loại phòng <strong class="room-type-create-required">*</strong></span>
                            <input id="roomTypeName" name="name" type="text" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Nhập tên loại phòng">
                            <c:if test="${not empty errors.name}">
                                <div class="room-type-create-field__error"><c:out value="${errors.name}" /></div>
                            </c:if>
                        </label>

                        <div class="room-type-create-field room-type-create-field--status room-type-create-field--right${not empty errors.status ? ' is-error' : ''}" data-validate-field="status">
                            <span>Trạng thái <strong class="room-type-create-required">*</strong></span>
                            <div class="room-type-create-switches room-type-create-switches--status">
                                <c:forEach var="status" items="${roomTypeStatuses}" varStatus="loop">
                                    <label>
                                        <input
                                                type="radio"
                                                id="roomTypeStatus_${loop.index}"
                                                name="status"
                                                value="<c:out value='${status}' />"
                                                <c:if test="${status eq form.status or (empty form.status and status eq roomTypeStatuses[0])}">checked</c:if>
                                                <c:if test="${loop.first}">required</c:if>
                                        >
                                        <span>
                                            <c:choose>
                                                <c:when test="${status eq 'ACTIVE'}">Đang hoạt động</c:when>
                                                <c:when test="${status eq 'INACTIVE'}">Ngừng hoạt động</c:when>
                                                <c:otherwise><c:out value="${status}" /></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </label>
                                </c:forEach>
                            </div>
                            <c:if test="${not empty errors.status}">
                                <div class="room-type-create-field__error"><c:out value="${errors.status}" /></div>
                            </c:if>
                        </div>

                        <label class="room-type-create-field room-type-create-field--left${not empty errors.capacity ? ' is-error' : ''}" data-validate-field="capacity">
                            <span>Sức chứa <strong class="room-type-create-required">*</strong></span>
                            <input id="roomTypeCapacity" name="capacity" type="number" value="<c:out value='${form.capacity}' />" min="1" step="1" required placeholder="2 khách">
                            <c:if test="${not empty errors.capacity}">
                                <div class="room-type-create-field__error"><c:out value="${errors.capacity}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field room-type-create-field--right${not empty errors.basePrice ? ' is-error' : ''}" data-validate-field="basePrice">
                            <span>Giá cơ bản <strong class="room-type-create-required">*</strong></span>
                            <div class="room-type-create-money">
                                <input id="roomTypeBasePrice" name="basePrice" type="text" value="<c:out value='${form.basePrice}' />" inputmode="numeric" autocomplete="off" required placeholder="1.200.000 VNĐ">
                                <strong>VNĐ</strong>
                            </div>
                            <c:if test="${not empty errors.basePrice}">
                                <div class="room-type-create-field__error"><c:out value="${errors.basePrice}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field room-type-create-field--right room-type-create-field--description${not empty errors.description ? ' is-error' : ''}" data-validate-field="description">
                            <span>Mô tả</span>
                            <textarea id="roomTypeDescription" name="description" rows="8" maxlength="500" placeholder="Mô tả ngắn, điểm nổi bật và ghi chú vị trí phòng."><c:out value="${form.description}" /></textarea>
                            <c:if test="${not empty errors.description}">
                                <div class="room-type-create-field__error"><c:out value="${errors.description}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field room-type-create-field--left${not empty errors.bedType ? ' is-error' : ''}">
                            <span>Loại giường</span>
                            <select id="roomTypeBedType" name="bedType" class="room-type-create-select">
                                <option value="">Chọn loại giường</option>
                                <c:forEach var="bedType" items="${bedTypes}">
                                    <option value="<c:out value='${bedType}' />" <c:if test="${bedType eq form.bedType}">selected</c:if>>
                                        <c:out value="${bedType}" />
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.bedType}">
                                <div class="room-type-create-field__error"><c:out value="${errors.bedType}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field room-type-create-field--right${not empty errors.sizeM2 ? ' is-error' : ''}">
                            <span>Diện tích</span>
                            <input id="roomTypeSizeM2" name="sizeM2" type="number" value="<c:out value='${form.sizeM2}' />" min="0" step="0.01" placeholder="32 m²">
                            <c:if test="${not empty errors.sizeM2}">
                                <div class="room-type-create-field__error"><c:out value="${errors.sizeM2}" /></div>
                            </c:if>
                        </label>

                        <div class="room-type-create-field room-type-create-field--validation room-type-create-field--right">
                            <div class="room-type-create-validation-box">
                                <h3>Khu vực kiểm tra và trợ giúp</h3>
                                <p>Hiển thị lỗi ngay dưới trường nhập với tông đỏ nhạt, đồng bộ ngôn ngữ hệ thống HMS.</p>
                                <p>Dùng khu vực này cho yêu cầu ảnh, cảnh báo trùng tên và phản hồi khi lưu.</p>
                            </div>
                        </div>
                    </div>
                </article>
            </div>

            <aside class="room-type-create-sidebar">
                <article class="panel room-type-create-card room-type-create-card--right">
                    <header class="room-type-create-card__head">
                        <div>
                            <h2>Ảnh và tổng quan</h2>
                            <p>Cột bên phải dùng cho tải ảnh, tiện ích và phần tổng quan trực tiếp.</p>
                        </div>
                    </header>

                    <div class="room-type-create-field room-type-create-field--upload${not empty errors.coverImage ? ' is-error' : ''}" data-validate-field="coverImage">
                        <span>Ảnh đại diện</span>
                        <input id="roomTypeCoverImage" name="coverImage" type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" hidden>

                        <div class="room-type-create-upload" role="button" tabindex="0" data-room-type-upload-zone>
                            <div class="room-type-create-upload__icon">↑</div>
                            <strong>Tải lên hoặc xem trước ảnh loại phòng</strong>
                            <span>Đồng bộ với ngôn ngữ thẻ hiện tại của hệ thống.</span>
                            <button class="btn btn-secondary" type="button" data-room-type-upload-trigger>Tải ảnh lên</button>
                            <small>JPG hoặc WEBP, tối đa 5 MB</small>
                            <small class="room-type-create-upload__name" data-room-type-file-name></small>
                        </div>

                        <c:if test="${not empty errors.coverImage}">
                            <div class="room-type-create-field__error"><c:out value="${errors.coverImage}" /></div>
                        </c:if>

                        <div class="room-type-create-preview-list">
                            <span>Xem trước trực tiếp</span>
                            <div class="room-type-create-cover">
                                <div class="room-type-create-cover__image room-type-create-cover__image--preview">
                                    <c:choose>
                                        <c:when test="${not empty roomTypeExistingImageUrl}">
                                            <img
                                                    id="roomTypeCoverPreview"
                                                    src="${cp}${roomTypeExistingImageUrl}"
                                                    data-original-src="${cp}${roomTypeExistingImageUrl}"
                                                    alt="Xem trước trực tiếp">
                                            <span id="roomTypeCoverPlaceholder" hidden>Ảnh đại diện</span>
                                        </c:when>
                                        <c:otherwise>
                                            <img id="roomTypeCoverPreview" alt="Xem trước trực tiếp" hidden>
                                            <span id="roomTypeCoverPlaceholder">Ảnh đại diện</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </article>

                <article class="panel room-type-create-card room-type-create-card--right room-type-create-card--card">
                    <header class="room-type-create-card__head">
                        <div>
                            <h2>Tiện ích</h2>
                        </div>
                    </header>

                    <div class="room-type-create-field room-type-create-field--compact">
                        <span>Tiện ích</span>
                        <div class="room-type-create-switches room-type-create-switches--grid room-type-create-switches--compact-grid room-type-create-switches--amenities">
                            <c:choose>
                                <c:when test="${empty amenities}">
                                    <span class="room-type-create-empty-state">Chưa có tiện ích nào.</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="amenity" items="${amenities}">
                                        <c:set var="amenityChecked" value="false" />
                                        <c:forEach var="selectedAmenityId" items="${selectedAmenityIds}">
                                            <c:if test="${selectedAmenityId eq amenity.id}">
                                                <c:set var="amenityChecked" value="true" />
                                            </c:if>
                                        </c:forEach>
                                        <label>
                                            <input type="checkbox" name="amenityIds" value="<c:out value='${amenity.id}' />" <c:if test="${amenityChecked}">checked</c:if>>
                                            <span><c:out value="${amenity.name}" /></span>
                                        </label>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </article>
            </aside>
        </section>
    </form>
</main>
<script src="${cp}/assets/js/room-type-create.js"></script>
</body>
</html>
