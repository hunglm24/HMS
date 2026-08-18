<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh Sách Phản Hồi - Quản Lý</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-light">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="container">
        <h2 style="margin-top: 20px; margin-bottom: 20px;">Danh Sách Đánh Giá Của Khách Hàng</h2>
        
        <div class="card">
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Mã Đặt Phòng</th>
                            <th>Phòng</th>
                            <th>Khách Hàng</th>
                            <th>Đánh Giá</th>
                            <th>Bình Luận</th>
                            <th>Ngày Đánh Giá</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty feedbacks}">
                                <c:forEach var="f" items="${feedbacks}">
                                    <tr>
                                        <td>${f.bookingCode}</td>
                                        <td>${f.roomNumbers}</td>
                                        <td>${f.customerName}</td>
                                        <td>
                                            <div style="color: #ffd700;">
                                                <c:forEach begin="1" end="${f.rating}">
                                                    <i class="fas fa-star"></i>
                                                </c:forEach>
                                                <c:forEach begin="${f.rating + 1}" end="5">
                                                    <i class="far fa-star"></i>
                                                </c:forEach>
                                            </div>
                                        </td>
                                        <td style="max-width: 300px; white-space: normal;">${f.comment}</td>
                                        <td><fmt:formatDate value="${f.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/housekeeping/issues/report" class="btn btn-sm btn-primary">Báo Sự Cố</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center">Chưa có phản hồi nào.</td>
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
