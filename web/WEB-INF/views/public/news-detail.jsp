<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${news.title} | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .news-hero {
            width: 100%;
            height: 400px;
            object-fit: cover;
            border-radius: var(--radius-lg);
            margin-bottom: 32px;
        }
        .news-content {
            font-size: 16px;
            line-height: 1.8;
            color: #334155;
        }
        .news-content img {
            max-width: 100%;
            height: auto;
            border-radius: var(--radius-md);
            margin: 16px 0;
        }
        .news-meta {
            display: flex;
            gap: 16px;
            color: var(--color-muted);
            font-size: 14px;
            margin-bottom: 24px;
        }
        .related-section {
            margin-top: 64px;
            border-top: 1px solid var(--color-border);
            padding-top: 32px;
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container">
        <section class="section-heading" style="margin-top: 24px;">
            <div>
                <p class="section-kicker">
                    <a href="${pageContext.request.contextPath}/news" style="color: inherit; text-decoration: none;">Tin tức</a>
                </p>
                <h1 style="font-size: 2.5rem; margin-top: 8px;">${news.title}</h1>
            </div>
        </section>

        <section style="max-width: 900px; margin: 0 auto; width: 100%;">
            <div class="news-meta">
                <span>Ngày đăng: <fmt:formatDate value="${news.publishedAt}" pattern="dd/MM/yyyy"/></span>
                <span>Tác giả: ${news.creatorName}</span>
            </div>

            <c:if test="${not empty news.thumbnailUrl}">
                <img src="${fn:startsWith(news.thumbnailUrl, '/') ? cp.concat(news.thumbnailUrl) : news.thumbnailUrl}" alt="${news.title}" class="news-hero">
            </c:if>

            <div class="news-content">
                ${news.content}
            </div>
            
            <c:if test="${not empty relatedNews}">
                <div class="related-section">
                    <h2 style="margin-bottom: 24px; font-size: 1.8rem;">Tin tức liên quan</h2>
                    <div class="news-card-grid">
                        <c:forEach var="rn" items="${relatedNews}">
                            <article class="room-showcase-card">
                                <img src="${not empty rn.thumbnailUrl ? (fn:startsWith(rn.thumbnailUrl, '/') ? cp.concat(rn.thumbnailUrl) : rn.thumbnailUrl) : 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80'}" alt="${rn.title}">
                                <div class="room-showcase-card__body">
                                    <h3 style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${rn.title}</h3>
                                    <div class="room-meta" style="margin-top: 8px;">
                                        <span><fmt:formatDate value="${rn.publishedAt}" pattern="dd/MM/yyyy"/></span>
                                    </div>
                                    <a class="btn" href="${pageContext.request.contextPath}/news/detail?id=${rn.id}" style="margin-top: 16px;">Xem chi tiết</a>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </section>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
