<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nhân sự | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar-internal.jsp" />
<main class="page-container">
    <section class="section-head">
        <div>
            <p class="section-kicker">Quản lý</p>
            <h1>Nhân sự</h1>
            <p>Theo dõi lễ tân, dọn phòng và kỹ thuật.</p>
        </div>
        <button type="button" class="btn btn-primary">Thêm nhân viên</button>
    </section>
    <div class="placeholder-table">
        <table>
            <thead>
                <tr>
                    <th>Nhân viên</th>
                    <th>Bộ phận</th>
                    <th>Ca</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Trần Thị B</td>
                    <td>Lễ tân</td>
                    <td>Ca sáng</td>
                    <td><span class="status-chip status-working">Đang làm</span></td>
                </tr>
                <tr>
                    <td>Lê Văn C</td>
                    <td>Dọn phòng</td>
                    <td>Ca chiều</td>
                    <td><span class="status-chip status-working">Đang làm</span></td>
                </tr>
            </tbody>
        </table>
    </div>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
