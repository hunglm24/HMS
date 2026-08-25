<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh Sách Phản Hồi - Lễ Tân | HMS</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260820-7">
    <link rel="stylesheet" href="${cp}/assets/css/feedback.css?v=20260824-1">
</head>
<body class="bg-light">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="container">
        <section class="section-head">
            <div>
                <p class="section-kicker">CHẤT LƯỢNG DỊCH VỤ</p>
                <h1>Danh Sách Đánh Giá Của Khách Hàng</h1>
            </div>
        </section>
        
        <div class="card feedback-table-panel">
            <div class="table-responsive">
                <table class="feedback-table">
                    <thead>
                        <tr>
                            <th>Mã Đặt Phòng</th>
                            <th>Phòng</th>
                            <th>Khách Hàng</th>
                            <th>Đánh Giá</th>
                            <th>Bình Luận</th>
                            <th>Ngày Đánh Giá</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty feedbacks}">
                                <c:forEach var="f" items="${feedbacks}">
                                    <tr>
                                        <td class="feedback-code-cell">${f.bookingCode}</td>
                                        <td>${not empty f.roomNumbers ? f.roomNumbers : '-'}</td>
                                        <td class="feedback-guest-cell">${f.customerName}</td>
                                        <td>
                                            <div class="feedback-star-display">
                                                <c:forEach begin="1" end="${f.rating}">
                                                    <i class="fas fa-star"></i>
                                                </c:forEach>
                                                <c:forEach begin="${f.rating + 1}" end="5">
                                                    <i class="far fa-star feedback-star-empty"></i>
                                                </c:forEach>
                                            </div>
                                        </td>
                                        <td class="feedback-comment-cell">${f.comment}</td>
                                        <td class="feedback-date-cell"><fmt:formatDate value="${f.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="feedback-empty-state">Chưa có phản hồi nào.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
