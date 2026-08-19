<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%!
    private String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(ch);
                    break;
            }
        }
        return escaped.toString();
    }

    private String resolveImageSrc(String contextPath, String imageUrl, String fallbackUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return fallbackUrl;
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return contextPath + trimmed;
        }
        return contextPath + "/" + trimmed;
    }
%>
<%
    model.RoomType room = (model.RoomType) request.getAttribute("room");
    java.util.List<model.Amenity> roomTypeAmenities = (java.util.List<model.Amenity>) request.getAttribute("roomTypeAmenities");
    if (roomTypeAmenities == null) {
        roomTypeAmenities = java.util.Collections.emptyList();
    }

    String imageSrc = room == null
            ? "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80"
            : resolveImageSrc(
                    request.getContextPath(),
                    room.getImageUrl(),
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80");
    String roomDescription = room != null && room.getDescription() != null && !room.getDescription().isBlank()
            ? room.getDescription()
            : "Thông tin phòng đang được cập nhật.";
    String roomSize = room != null && room.getSizeM2() != null
            ? room.getSizeM2().stripTrailingZeros().toPlainString()
            : null;
    String roomBedType = room != null && room.getBedType() != null && !room.getBedType().isBlank()
            ? room.getBedType()
            : null;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-2">
    <style>
        .room-detail-page {
            max-width: 1120px;
            margin: 0 auto;
            padding: 28px 16px 40px;
        }

        .room-detail-grid {
            display: grid;
            grid-template-columns: minmax(0, 1.45fr) minmax(300px, 0.75fr);
            gap: 20px;
            align-items: start;
        }

        .room-detail-card,
        .room-booking-card {
            background: #fff;
            border-radius: 28px;
            box-shadow: 0 10px 30px rgba(18, 38, 76, 0.08);
            overflow: hidden;
        }

        .room-detail-card__image {
            width: 100%;
            aspect-ratio: 16 / 9;
            object-fit: cover;
            display: block;
            background: #eef2f8;
        }

        .room-detail-card__body {
            padding: 24px 26px 28px;
        }

        .room-detail-title {
            margin: 0;
            font-size: clamp(32px, 4vw, 46px);
            line-height: 1.05;
            color: #0f172a;
        }

        .room-detail-status {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-top: 12px;
            padding: 8px 14px;
            border-radius: 999px;
            background: #eef4ff;
            color: #2552e8;
            font-weight: 600;
            font-size: 14px;
        }

        .room-detail-pills {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin-top: 18px;
        }

        .room-detail-pill {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            padding: 14px 18px;
            border-radius: 999px;
            background: #f2f4f8;
            color: #516074;
            font-size: 17px;
            font-weight: 600;
        }

        .room-detail-pill__icon {
            width: 20px;
            height: 20px;
            color: #93a0b8;
            flex: none;
        }

        .room-detail-description {
            margin: 20px 0 0;
            color: #667084;
            font-size: 17px;
            line-height: 1.7;
        }

        .room-detail-section {
            margin-top: 28px;
        }

        .room-detail-section h2 {
            margin: 0;
            font-size: 26px;
            color: #0f172a;
        }

        .amenity-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px 22px;
            margin-top: 18px;
        }

        .amenity-item {
            display: flex;
            align-items: center;
            gap: 14px;
            font-size: 17px;
            color: #1f2937;
        }

        .amenity-item__check {
            width: 22px;
            height: 22px;
            color: #2552e8;
            flex: none;
        }

        .room-booking-card {
            padding: 22px;
            position: sticky;
            top: 20px;
        }

        .room-booking-card h3 {
            margin: 0;
            font-size: 22px;
            color: #0f172a;
        }

        .room-booking-card__price {
            margin-top: 8px;
            color: #5b667b;
            font-size: 16px;
            line-height: 1.6;
        }

        .room-booking-card form {
            margin-top: 16px;
            display: grid;
            gap: 12px;
        }

        .room-booking-card label {
            display: grid;
            gap: 8px;
            color: #334155;
            font-weight: 600;
        }

        .room-booking-card input {
            border: 1px solid #d8deea;
            border-radius: 14px;
            padding: 11px 14px;
            font: inherit;
            background: #fff;
            color: #0f172a;
        }

        .room-booking-card button {
            margin-top: 4px;
            width: 100%;
            border: 0;
            border-radius: 14px;
            padding: 13px 18px;
            background: #2552e8;
            color: #fff;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
        }

        .room-booking-card button:disabled {
            background: #c7cfdd;
            cursor: not-allowed;
        }

        .room-booking-card__note {
            margin: 12px 0 0;
            color: #dc2626;
            font-size: 14px;
        }

        .room-detail-empty {
            padding: 32px;
            border-radius: 24px;
            background: #fff;
            box-shadow: 0 10px 30px rgba(18, 38, 76, 0.08);
        }

        @media (max-width: 980px) {
            .room-detail-grid {
                grid-template-columns: 1fr;
            }

            .room-booking-card {
                position: static;
            }
        }

        @media (max-width: 720px) {
            .room-detail-page {
                padding: 18px 12px 32px;
            }

            .room-detail-card__body,
            .room-booking-card {
                padding: 18px;
            }

            .room-detail-pill,
            .amenity-item {
                font-size: 15px;
            }

            .amenity-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="room-detail-page public-page">
        <c:choose>
            <c:when test="${not empty room}">
                <section class="room-detail-grid">
                    <article class="room-detail-card">
                        <img class="room-detail-card__image" src="<%= escapeHtml(imageSrc) %>" alt="<%= escapeHtml(room.getName()) %>">
                        <div class="room-detail-card__body">
                            <h1 class="room-detail-title"><c:out value="${room.name}" /></h1>

                            <div class="room-detail-pills">
                                <div class="room-detail-pill">
                                    <svg class="room-detail-pill__icon" viewBox="0 0 24 24" aria-hidden="true">
                                        <rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.6" />
                                        <path d="M8 8h8M8 12h8M8 16h4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                                    </svg>
                                    <span><%= roomSize != null ? roomSize : "-" %> m&sup2;</span>
                                </div>
                                <div class="room-detail-pill">
                                    <svg class="room-detail-pill__icon" viewBox="0 0 24 24" aria-hidden="true">
                                        <path d="M4 10h16v6H4z" fill="none" stroke="currentColor" stroke-width="1.6" />
                                        <path d="M6 10V7h5v3M13 10V7h5v3" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                                    </svg>
                                    <span><%= roomBedType != null ? escapeHtml(roomBedType) : "-" %></span>
                                </div>
                                <div class="room-detail-pill">
                                    <svg class="room-detail-pill__icon" viewBox="0 0 24 24" aria-hidden="true">
                                        <circle cx="9" cy="8" r="3" fill="none" stroke="currentColor" stroke-width="1.6" />
                                        <circle cx="16" cy="8.5" r="2.5" fill="none" stroke="currentColor" stroke-width="1.6" />
                                        <path d="M4 18c0-2.8 2.5-5 5.5-5s5.5 2.2 5.5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                                        <path d="M13 18c.2-1.8 1.5-3.2 3.5-4" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
                                    </svg>
                                    <span><c:out value="${room.capacity}" /> guests</span>
                                </div>
                            </div>

                            <p class="room-detail-description"><%= escapeHtml(roomDescription) %></p>

                            <section class="room-detail-section">
                                <h2>Amenities</h2>
                                <c:choose>
                                    <c:when test="${not empty roomTypeAmenities}">
                                        <div class="amenity-grid">
                                            <c:forEach var="amenity" items="${roomTypeAmenities}">
                                                <div class="amenity-item">
                                                    <svg class="amenity-item__check" viewBox="0 0 24 24" aria-hidden="true">
                                                        <path d="M5 12.5l4 4L19 7.5" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
                                                    </svg>
                                                    <span><c:out value="${amenity.name}" /></span>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="room-detail-description">Chưa có tiện ích nào được gắn cho loại phòng này.</p>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </div>
                    </article>

                    <aside class="room-booking-card">
                        <span class="room-detail-kicker">Đặt phòng</span>
                        <h3>Thông tin lưu trú</h3>
                        <div class="room-booking-card__price">
                            <c:choose>
                                <c:when test="${not empty room.basePrice}">
                                    Giá từ <fmt:formatNumber value="${room.basePrice}" pattern="#,###" var="fmtPrice" />
                                    ${fn:replace(fmtPrice, ',', ' ')} VND/đêm. Có thể đổi phòng tùy theo tình trạng thực tế.
                                </c:when>
                                <c:otherwise>
                                    Giá phòng đang được cập nhật.
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <form class="booking-form" method="post" action="${pageContext.request.contextPath}/cart">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="roomId" value="${room.id}">
                            <label>Check-in<input type="date" name="checkIn" id="checkIn" value="${param.checkIn}" required></label>
                            <label>Check-out<input type="date" name="checkOut" id="checkOut" value="${param.checkOut}" required></label>
                            <label>Số khách<input type="number" name="guests" value="${param.guests != null ? param.guests : room.capacity}" min="1" max="${room.capacity}" required></label>
                            <label>Số phòng<input type="number" name="quantity" value="1" min="1" required></label>
                            <button type="submit" ${not requestScope.isAvailable ? 'disabled' : ''}>
                                ${requestScope.isAvailable ? 'Thêm vào giỏ' : 'Đã hết phòng'}
                            </button>
                            <c:if test="${not requestScope.isAvailable}">
                                <p class="room-booking-card__note">Không còn phòng trống cho ngày đã chọn.</p>
                            </c:if>
                        </form>
                    </aside>
                </section>
            </c:when>
            <c:otherwise>
                <section class="room-detail-empty">
                    <h1>Không tìm thấy phòng</h1>
                    <p>Phòng bạn đang xem không còn tồn tại hoặc đã ngừng hoạt động.</p>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
    <script>
        const checkInInput = document.getElementById('checkIn');
        const checkOutInput = document.getElementById('checkOut');

        if (checkInInput && checkOutInput) {
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const today = `${year}-${month}-${day}`;
            checkInInput.setAttribute('min', today);

            function updateCheckOutMin() {
                if (checkInInput.value) {
                    const nextDay = new Date(checkInInput.value);
                    nextDay.setDate(nextDay.getDate() + 1);
                    const minOut = nextDay.toISOString().split('T')[0];
                    checkOutInput.setAttribute('min', minOut);
                    if (checkOutInput.value && checkOutInput.value <= checkInInput.value) {
                        checkOutInput.value = minOut;
                    }
                }
            }

            checkInInput.addEventListener('change', updateCheckOutMin);
            updateCheckOutMin();
        }
    </script>
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
