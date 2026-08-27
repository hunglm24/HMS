<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nội quy chung khách sạn | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260821-1">
    <style>
        .public-page { background: #f6f8fb; }
        .policy-page { max-width: 900px; margin: 0 auto; padding: 24px 0 48px; }
        .policy-title { margin: 0 0 16px; }
        .policy-card { background: #fff; border: 1px solid #d9e0ea; border-radius: 12px; padding: 24px; }
        .policy-content { white-space: pre-line; line-height: 1.9; color: #344054; font-size: 1rem; }
        .policy-empty { color: #667085; }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="public-page">
    <section class="policy-page">
        <h1 class="policy-title">Nội quy chung khách sạn</h1>
        <div class="policy-card">
            <c:choose>
                <c:when test="${not empty policy}">
                    <div class="policy-content"><c:out value="${policy.content}" /></div>
                </c:when>
                <c:otherwise>
                    <div class="policy-empty">Nội quy đang được cập nhật.</div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
