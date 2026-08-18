<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Sửa Đặt Phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; font-weight: bold; margin-bottom: 5px; }
        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; }
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

        <form method="post" action="${pageContext.request.contextPath}/receptionist/edit-booking" class="toolbar-card" style="display: block;">
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
