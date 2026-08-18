<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><c:out value="${roomTypePageTitle}" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="${cp}/assets/css/room-type-create.css">
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
                <a class="room-type-create-back" href="${cp}${roomTypeBackUrl}">Back to Room Types</a>
                <h1><c:out value="${roomTypePageHeading}" /></h1>
                <p><c:out value="${roomTypePageSubtitle}" /></p>
            </div>

            <div class="room-type-create-hero__actions">
                <button class="btn btn-secondary" type="button">Save Draft</button>
                <button class="btn btn-warning" type="submit"><c:out value="${roomTypeSubmitLabel}" /></button>
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
                        <h2>Basic Information</h2>
                        <div class="room-type-create-card__tools">
                            <button class="room-type-create-mini-btn" type="button">Save</button>
                            <button class="room-type-create-icon-btn" type="button" aria-label="Edit basic information">✎</button>
                        </div>
                    </header>

                    <label class="room-type-create-field${not empty errors.name ? ' is-error' : ''}" data-validate-field="name">
                        <span>Room Type Name <strong class="room-type-create-required">*</strong></span>
                        <input id="roomTypeName" name="name" type="text" value="<c:out value='${form.name}' />" maxlength="100" required placeholder="Enter room type name">
                        <c:if test="${not empty errors.name}">
                            <div class="room-type-create-field__error"><c:out value="${errors.name}" /></div>
                        </c:if>
                    </label>

                    <label class="room-type-create-field${not empty errors.description ? ' is-error' : ''}" data-validate-field="description">
                        <span>About Room</span>
                        <textarea id="roomTypeDescription" name="description" rows="8" maxlength="500" placeholder="Optional description"><c:out value="${form.description}" /></textarea>
                        <c:if test="${not empty errors.description}">
                            <div class="room-type-create-field__error"><c:out value="${errors.description}" /></div>
                        </c:if>
                    </label>

                    <div class="room-type-create-grid-2 room-type-create-grid-2--basic">
                        <label class="room-type-create-field${not empty errors.capacity ? ' is-error' : ''}" data-validate-field="capacity">
                            <span>Capacity <strong class="room-type-create-required">*</strong></span>
                            <input id="roomTypeCapacity" name="capacity" type="number" value="<c:out value='${form.capacity}' />" min="1" step="1" required placeholder="0">
                            <c:if test="${not empty errors.capacity}">
                                <div class="room-type-create-field__error"><c:out value="${errors.capacity}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field${not empty errors.basePrice ? ' is-error' : ''}" data-validate-field="basePrice">
                            <span>Base Price <strong class="room-type-create-required">*</strong></span>
                            <div class="room-type-create-money">
                                <input id="roomTypeBasePrice" name="basePrice" type="text" value="<c:out value='${form.basePrice}' />" inputmode="numeric" autocomplete="off" required placeholder="0">
                                <strong>VND</strong>
                            </div>
                            <c:if test="${not empty errors.basePrice}">
                                <div class="room-type-create-field__error"><c:out value="${errors.basePrice}" /></div>
                            </c:if>
                        </label>
                    </div>

                    <div class="room-type-create-field room-type-create-field--status${not empty errors.status ? ' is-error' : ''}" data-validate-field="status">
                        <span>Availability Status <strong class="room-type-create-required">*</strong></span>
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
                                    <span><c:out value="${status}" /></span>
                                </label>
                            </c:forEach>
                        </div>
                        <c:if test="${not empty errors.status}">
                            <div class="room-type-create-field__error"><c:out value="${errors.status}" /></div>
                        </c:if>
                    </div>
                </article>
            </div>

            <aside class="room-type-create-sidebar">
                <article class="panel room-type-create-card room-type-create-card--right">
                    <header class="room-type-create-card__head">
                        <div>
                            <h2>Room Configuration</h2>
                        </div>
                        <div class="room-type-create-card__tools">
                            <button class="room-type-create-mini-btn" type="button">Save</button>
                            <button class="room-type-create-icon-btn" type="button" aria-label="Edit configuration">✎</button>
                        </div>
                    </header>

                    <div class="room-type-create-grid-2">
                        <label class="room-type-create-field${not empty errors.sizeM2 ? ' is-error' : ''}">
                            <span>Room Size</span>
                            <input id="roomTypeSizeM2" name="sizeM2" type="number" value="<c:out value='${form.sizeM2}' />" min="0" step="0.01" placeholder="Optional size in m2">
                            <c:if test="${not empty errors.sizeM2}">
                                <div class="room-type-create-field__error"><c:out value="${errors.sizeM2}" /></div>
                            </c:if>
                        </label>

                        <label class="room-type-create-field${not empty errors.bedType ? ' is-error' : ''}">
                            <span>Bed Type</span>
                            <select id="roomTypeBedType" name="bedType" class="room-type-create-select">
                                <option value="">Select bed type</option>
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
                    </div>

                    <div class="room-type-create-field room-type-create-field--compact">
                        <span>Amenities</span>
                        <div class="room-type-create-switches room-type-create-switches--grid room-type-create-switches--compact-grid room-type-create-switches--amenities">
                            <c:choose>
                                <c:when test="${empty amenities}">
                                    <span class="room-type-create-empty-state">No amenities available.</span>
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

                <article class="panel room-type-create-card room-type-create-card--media">
                    <header class="room-type-create-card__head">
                        <div>
                            <h2>Media</h2>
                        </div>
                        <div class="room-type-create-card__tools">
                            <button class="room-type-create-mini-btn" type="button">Save</button>
                            <button class="room-type-create-icon-btn" type="button" aria-label="Edit media">✎</button>
                        </div>
                    </header>

                    <div class="room-type-create-field room-type-create-field--upload${not empty errors.coverImage ? ' is-error' : ''}" data-validate-field="coverImage">
                            <span>Cover Image</span>
                            <input id="roomTypeCoverImage" name="coverImage" type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp" hidden>

                        <div class="room-type-create-upload" role="button" tabindex="0" data-room-type-upload-zone>
                            <div class="room-type-create-upload__icon">↑</div>
                            <strong>Drag and drop to upload photo</strong>
                            <span>or</span>
                            <button class="btn btn-secondary" type="button" data-room-type-upload-trigger>Upload Photo</button>
                            <small>Recommended JPG or WEBP, up to 5 MB</small>
                            <small class="room-type-create-upload__name" data-room-type-file-name></small>
                        </div>

                        <c:if test="${not empty errors.coverImage}">
                            <div class="room-type-create-field__error"><c:out value="${errors.coverImage}" /></div>
                        </c:if>

                        <div class="room-type-create-preview-list">
                            <span>Cover Image Preview</span>
                            <div class="room-type-create-cover">
                                <div class="room-type-create-cover__image room-type-create-cover__image--preview">
                                    <c:choose>
                                        <c:when test="${not empty roomTypeExistingImageUrl}">
                                            <img
                                                    id="roomTypeCoverPreview"
                                                    src="${cp}${roomTypeExistingImageUrl}"
                                                    data-original-src="${cp}${roomTypeExistingImageUrl}"
                                                    alt="Selected cover preview">
                                            <span id="roomTypeCoverPlaceholder" hidden>Cover</span>
                                        </c:when>
                                        <c:otherwise>
                                            <img id="roomTypeCoverPreview" alt="Selected cover preview" hidden>
                                            <span id="roomTypeCoverPlaceholder">Cover</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
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
