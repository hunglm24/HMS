<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% if (session.getAttribute("currentUser") == null) {
       response.sendRedirect(request.getContextPath() + "/login");
       return;
   }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="page-container">
        <h1>Xin chào, ${sessionScope.currentUser.fullName}</h1>
        <p>Bạn đã đăng nhập thành công vào hệ thống quản lý khách sạn.</p>
    </main>
</body>
</html>
