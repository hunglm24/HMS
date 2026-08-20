<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sửa Đặt Phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; font-weight: bold; margin-bottom: 5px; }
        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
        .helper-text { margin-top: 6px; font-size: 0.875rem; color: #2563eb; }
        .form-section-title { margin: 20px 0 10px; font-size: 1rem; font-weight: 700; color: #111827; }
        .form-actions { margin-top: 20px; text-align: right; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <section class="section-head">
            <div>
                <p class="section-kicker">Lễ tân</p>
                <h1>Cập nhật Đặt Phòng: ${booking.bookingCode}</h1>
            </div>
            <a href="${pageContext.request.contextPath}/reception/bookings" class="btn btn-secondary">Quay lại</a>
        </section>

        <c:if test="${not empty error}">
            <div class="alert alert-error" style="background-color: var(--color-error-100); color: var(--color-error-600); padding: 15px; margin-bottom: 20px; border-radius: 4px;">
                ${error}
            </div>
            <c:remove var="error" scope="session" />
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/receptionist/edit-booking" class="toolbar-card" style="display: block;" onsubmit="if(this.dataset.submitted) return false; this.dataset.submitted = true;">
            <input type="hidden" name="id" value="${booking.id}">
            
            <div class="form-grid">
                <div class="form-group">
                    <label>Tên khách hàng</label>
                    <input type="text" name="fullName" value="${guestName}" required>
                </div>
                <div class="form-group">
                    <label>Số điện thoại</label>
                    <input type="text" name="phone" value="${phone}" required>
                </div>
                <div class="form-group">
                    <label>Ngày nhận phòng (Check-in)</label>
                    <input type="date" name="checkInDate" value="${booking.checkInDate}" required>
                </div>
                <div class="form-group">
                    <label>Ngày trả phòng (Check-out)</label>
                    <input type="date" name="checkOutDate" value="${booking.checkOutDate}" required>
                </div>
            </div>

            <div class="form-section-title">Chọn loại phòng / phòng</div>
            <div class="form-grid">
                <div class="form-group">
                    <label>Loại phòng</label>
                    <select name="roomType">
                        <option value="">Không chọn loại phòng</option>
                        <option value="STANDARD">Standard</option>
                        <option value="DELUXE">Deluxe</option>
                        <option value="SUITE">Suite</option>
                        <option value="FAMILY">Family</option>
                    </select>
                    <div class="helper-text">Có thể chọn hoặc bỏ trống nếu chưa cần đổi loại phòng.</div>
                </div>
                <div class="form-group">
                    <label>Phòng</label>
                    <select name="roomNumber" required>
                        <option value="">Chọn phòng</option>
                        <optgroup label="Standard">
                            <option value="101">101 - Standard</option>
                            <option value="102">102 - Standard</option>
                        </optgroup>
                        <optgroup label="Deluxe">
                            <option value="201">201 - Deluxe</option>
                            <option value="202">202 - Deluxe</option>
                        </optgroup>
                        <optgroup label="Suite">
                            <option value="301">301 - Suite</option>
                        </optgroup>
                        <optgroup label="Family">
                            <option value="401">401 - Family</option>
                        </optgroup>
                    </select>
                    <div class="helper-text">Nếu chưa muốn đổi loại phòng, bạn vẫn có thể chọn phòng trực tiếp.</div>
                </div>
            </div>

            <div class="form-group" style="margin-top: 15px;">
                <label>Lý do sửa đổi (Required)</label>
                <input type="text" name="reason" placeholder="Ví dụ: Khách yêu cầu đổi lịch / Nâng cấp phòng" required>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary" onclick="return confirm('Bạn có chắc chắn lưu các thay đổi này không?');">Lưu thay đổi</button>
            </div>
        </form>
    </main>
</body>
</html>
