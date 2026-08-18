<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh Giá Đặt Phòng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8f9fa;
        }
        .feedback-wrapper {
            padding: 60px 20px;
            display: flex;
            justify-content: center;
        }
        .feedback-container {
            width: 100%;
            max-width: 550px;
            background: #ffffff;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
            border: 1px solid rgba(0,0,0,0.05);
            position: relative;
            overflow: hidden;
        }
        .feedback-container::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 6px;
            background: linear-gradient(90deg, var(--primary, #4f46e5), #818cf8);
        }
        .feedback-title {
            font-size: 24px;
            font-weight: 700;
            color: #1f2937;
            margin-bottom: 8px;
            text-align: center;
        }
        .feedback-subtitle {
            font-size: 14px;
            color: #6b7280;
            text-align: center;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 24px;
        }
        .form-label {
            display: block;
            font-weight: 600;
            color: #374151;
            margin-bottom: 12px;
            font-size: 15px;
        }
        .rating-stars {
            display: flex;
            flex-direction: row-reverse;
            justify-content: center;
            gap: 8px;
            background: #f9fafb;
            padding: 16px;
            border-radius: 12px;
            border: 1px solid #f3f4f6;
        }
        .rating-stars input {
            display: none;
        }
        .rating-stars label {
            font-size: 32px;
            color: #d1d5db;
            cursor: pointer;
            transition: all 0.2s ease-in-out;
        }
        .rating-stars label:hover {
            transform: scale(1.15);
        }
        .rating-stars input:checked ~ label,
        .rating-stars label:hover,
        .rating-stars label:hover ~ label {
            color: #fbbf24;
            text-shadow: 0 0 10px rgba(251, 191, 36, 0.4);
        }
        .feedback-textarea {
            width: 100%;
            padding: 16px;
            border: 1px solid #d1d5db;
            border-radius: 12px;
            font-family: inherit;
            font-size: 15px;
            color: #1f2937;
            transition: all 0.2s;
            resize: vertical;
            background-color: #f9fafb;
        }
        .feedback-textarea:focus {
            outline: none;
            border-color: var(--primary, #4f46e5);
            background-color: #ffffff;
            box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
        }
        .feedback-textarea::placeholder {
            color: #9ca3af;
        }
        .btn-action-group {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 32px;
        }
        .btn-custom {
            padding: 12px 24px;
            font-weight: 600;
            font-size: 15px;
            border-radius: 8px;
            transition: all 0.2s;
            text-decoration: none;
            border: none;
            cursor: pointer;
            font-family: inherit;
        }
        .btn-cancel {
            background: #f3f4f6;
            color: #4b5563;
        }
        .btn-cancel:hover {
            background: #e5e7eb;
            color: #1f2937;
        }
        .btn-submit {
            background: var(--primary, #4f46e5);
            color: white;
            box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);
        }
        .btn-submit:hover {
            background: var(--primary-dark, #4338ca);
            transform: translateY(-1px);
            box-shadow: 0 6px 8px -1px rgba(79, 70, 229, 0.3);
        }
    </style>
</head>
<body class="bg-light">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="feedback-wrapper">
        <div class="feedback-container">
            <h2 class="feedback-title">Đánh Giá Đặt Phòng</h2>
            <p class="feedback-subtitle">Mã đặt phòng: <strong>${booking.bookingCode}</strong></p>
            
            <form action="${pageContext.request.contextPath}/customer/feedback" method="post">
                <input type="hidden" name="bookingId" value="${booking.id}">
                
                <div class="form-group">
                    <label class="form-label text-center">Mức độ hài lòng của bạn</label>
                    <div class="rating-stars">
                        <input type="radio" id="star5" name="rating" value="5" required />
                        <label for="star5" class="fas fa-star"></label>
                        <input type="radio" id="star4" name="rating" value="4" />
                        <label for="star4" class="fas fa-star"></label>
                        <input type="radio" id="star3" name="rating" value="3" />
                        <label for="star3" class="fas fa-star"></label>
                        <input type="radio" id="star2" name="rating" value="2" />
                        <label for="star2" class="fas fa-star"></label>
                        <input type="radio" id="star1" name="rating" value="1" />
                        <label for="star1" class="fas fa-star"></label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="comment" class="form-label">Chia sẻ trải nghiệm của bạn</label>
                    <textarea name="comment" id="comment" class="feedback-textarea" rows="4" placeholder="Bạn cảm thấy dịch vụ của chúng tôi như thế nào? Điều gì làm bạn hài lòng hoặc chưa hài lòng?"></textarea>
                </div>

                <div class="btn-action-group">
                    <a href="${pageContext.request.contextPath}/my-bookings" class="btn-custom btn-cancel">Trở lại</a>
                    <button type="submit" class="btn-custom btn-submit">Gửi Đánh Giá <i class="fas fa-paper-plane" style="margin-left: 8px;"></i></button>
                </div>
            </form>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
