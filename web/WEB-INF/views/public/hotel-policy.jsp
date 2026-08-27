<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nội quy chung khách sạn | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260821-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/hotel-policy.css?v=20260827-1">
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="policy-guest-page">
    <section class="policy-guest-shell">
        <div class="policy-hero policy-hero--public">
            <div class="policy-hero__overlay"></div>
            <div class="policy-hero__content">
                <p class="policy-hero__eyebrow">Privacy Policy</p>
                <h1>Nội quy chung khách sạn</h1>
            </div>
        </div>

        <div class="policy-public-copy">
            <c:choose>
                <c:when test="${not empty policy}">
                    <div class="policy-public-intro">
                        <p class="policy-public-intro__eyebrow">Nội quy</p>
                        <p class="policy-public-intro__lead">
                            Đây là bản nội quy chung áp dụng cho toàn khách lưu trú. Vui lòng đọc kỹ trước khi nhận phòng và trong suốt thời gian ở khách sạn.
                        </p>
                    </div>

                    <article class="policy-public-article">
                        <div class="policy-public-article__content">
                            <c:out value="${policy.displayContent}" />
                        </div>
                    </article>
                </c:when>
                <c:otherwise>
                    <div class="policy-empty policy-empty--public">Nội quy đang được cập nhật.</div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
