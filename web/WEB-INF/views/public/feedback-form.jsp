<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh Giá Đặt Phòng | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="${cp}/assets/css/feedback.css?v=20260824-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body class="bg-light">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="feedback-wrapper">
        <div class="feedback-container">
            <h1 class="feedback-title">Đánh Giá Đặt Phòng</h1>
            <p class="feedback-subtitle">Mã đặt phòng: <strong>${booking.bookingCode}</strong></p>
            
            <form action="${cp}/customer/feedback" method="post">
                <input type="hidden" name="bookingId" value="${booking.id}">
                
                <div class="feedback-form-group">
                    <label class="feedback-form-label text-center">Mức độ hài lòng của bạn</label>
                    <div class="rating-stars">
                        <input type="radio" id="star5" name="rating" value="5" required />
                        <label for="star5" class="fas fa-star" title="5 sao - Tuyệt vời"></label>
                        <input type="radio" id="star4" name="rating" value="4" />
                        <label for="star4" class="fas fa-star" title="4 sao - Rất tốt"></label>
                        <input type="radio" id="star3" name="rating" value="3" />
                        <label for="star3" class="fas fa-star" title="3 sao - Bình thường"></label>
                        <input type="radio" id="star2" name="rating" value="2" />
                        <label for="star2" class="fas fa-star" title="2 sao - Chưa hài lòng"></label>
                        <input type="radio" id="star1" name="rating" value="1" />
                        <label for="star1" class="fas fa-star" title="1 sao - Kém"></label>
                    </div>
                </div>

                <div class="feedback-form-group">
                    <label for="comment" class="feedback-form-label">Chia sẻ trải nghiệm của bạn</label>
                    <textarea name="comment" id="comment" class="feedback-textarea" rows="4" maxlength="500" placeholder="Bạn cảm thấy dịch vụ của chúng tôi như thế nào? Điều gì làm bạn hài lòng hoặc chưa hài lòng?"></textarea>
                </div>

                <div class="feedback-btn-group">
                    <a href="${cp}/my-bookings" class="btn-custom btn-cancel">Trở lại</a>
                    <button type="submit" class="btn-custom btn-submit">
                        Gửi Đánh Giá <i class="fas fa-paper-plane" style="margin-left: 8px;"></i>
                    </button>
                </div>
            </form>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
    <script src="${cp}/assets/js/feedback.js?v=20260824-1"></script>
</body>
</html>
