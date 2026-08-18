<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khuyến mãi & Sự kiện | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .news-snippet {
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;  
            overflow: hidden;
            color: #6c757d;
            margin-top: 12px;
            font-size: 0.95rem;
            line-height: 1.5;
        }
        .pagination {
            display: flex;
            justify-content: center;
            gap: 8px;
            margin-top: 48px;
            list-style: none;
            padding: 0;
        }
        .pagination a {
            padding: 8px 16px;
            border: 1px solid #ddd;
            border-radius: 6px;
            text-decoration: none;
            color: #333;
            transition: all 0.2s;
        }
        .pagination a:hover, .pagination .active a {
            background-color: var(--primary);
            color: white;
            border-color: var(--primary);
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="public-page">
        <section class="section-head">
            <div>
                <p class="section-kicker">Tin tức</p>
                <h1>Khuyến mãi & Sự kiện</h1>
                <p>Cập nhật những thông tin mới nhất và các chương trình ưu đãi hấp dẫn.</p>
            </div>
        </section>

        <section class="room-card-grid">
            <c:forEach var="n" items="${newsList}">
                <article class="room-showcase-card">
                    <img src="${not empty n.thumbnailUrl ? n.thumbnailUrl : 'https://via.placeholder.com/900x500?text=News'}" alt="${n.title}">
                    <div class="room-showcase-card__body">
                        <h3 style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${n.title}</h3>
                        <div class="room-meta" style="margin-top: 8px;">
                            <span>Ngày đăng: <fmt:formatDate value="${n.publishedAt}" pattern="dd/MM/yyyy"/></span>
                        </div>
                        <div class="news-snippet">
                            ${n.content.replaceAll("<[^>]*>", "")}
                        </div>
                        <a class="btn" href="${pageContext.request.contextPath}/news/detail?id=${n.id}" style="margin-top: 16px;">Xem chi tiết</a>
                    </div>
                </article>
            </c:forEach>
        </section>

        <c:if test="${empty newsList}">
            <div style="text-align: center; padding: 64px 0; color: #666; font-size: 1.1rem;">
                <p>Hiện tại không có bài viết nào.</p>
            </div>
        </c:if>

        <c:if test="${totalPages > 1}">
            <ul class="pagination">
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="${p == currentPage ? 'active' : ''}">
                        <a href="?page=${p}">${p}</a>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
