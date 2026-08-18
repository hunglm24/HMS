<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage News - Admin Portal</title>
    <!-- Include your existing CSS framework here, e.g., Bootstrap or Tailwind -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .news-thumbnail { width: 80px; height: 50px; object-fit: cover; border-radius: 4px; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Manage News</h2>
            <a href="${pageContext.request.contextPath}/manager/news/create" class="btn btn-primary">
                <i class="fas fa-plus"></i> Add New
            </a>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/manager/news" method="GET" class="row g-3">
                    <div class="col-md-6">
                        <input type="text" name="search" class="form-control" placeholder="Search by title..." value="${search}">
                    </div>
                    <div class="col-md-4">
                        <select name="status" class="form-select">
                            <option value="ALL" ${status == 'ALL' ? 'selected' : ''}>All Statuses</option>
                            <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>Draft</option>
                            <option value="PUBLISHED" ${status == 'PUBLISHED' ? 'selected' : ''}>Published</option>
                            <option value="HIDDEN" ${status == 'HIDDEN' ? 'selected' : ''}>Hidden</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-secondary w-100"><i class="fas fa-search"></i> Filter</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Thumbnail</th>
                        <th>Title</th>
                        <th>Status</th>
                        <th>Author</th>
                        <th>Published Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="n" items="${newsList}">
                        <tr>
                            <td>${n.id}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty n.thumbnailUrl}">
                                        <img src="${n.thumbnailUrl}" class="news-thumbnail" alt="thumbnail">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="bg-secondary text-white text-center rounded d-flex align-items-center justify-content-center" style="width: 80px; height: 50px; font-size: 10px;">No Image</div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${n.title}</td>
                            <td>
                                <span class="badge ${n.status == 'PUBLISHED' ? 'bg-success' : (n.status == 'DRAFT' ? 'bg-warning text-dark' : 'bg-secondary')}">
                                    ${n.status}
                                </span>
                            </td>
                            <td>${n.creatorName}</td>
                            <td><fmt:formatDate value="${n.publishedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/manager/news/edit?id=${n.id}" class="btn btn-sm btn-outline-primary"><i class="fas fa-edit"></i></a>
                                <form action="${pageContext.request.contextPath}/manager/news/delete" method="POST" class="d-inline" onsubmit="return confirm('Are you sure you want to delete this news?');">
                                    <input type="hidden" name="id" value="${n.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger"><i class="fas fa-trash"></i></button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty newsList}">
                        <tr>
                            <td colspan="7" class="text-center">No news found.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <nav>
                <ul class="pagination justify-content-center">
                    <c:forEach begin="1" end="${totalPages}" var="p">
                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                            <a class="page-link" href="?page=${p}&search=${search}&status=${status}">${p}</a>
                        </li>
                    </c:forEach>
                </ul>
            </nav>
        </c:if>
    </div>
</body>
</html>
