<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentPage" value="${empty paginationCurrentPage ? 1 : paginationCurrentPage}" />
<c:set var="totalPages" value="${empty paginationTotalPages ? 1 : paginationTotalPages}" />
<c:if test="${totalPages gt 1}">
    <nav class="hk-pagination" aria-label="Pagination">
        <c:choose>
            <c:when test="${currentPage gt 1 and not empty paginationPrevUrl}">
                <a class="hk-pagination__link" href="${paginationPrevUrl}">Trước</a>
            </c:when>
            <c:otherwise>
                <span class="hk-pagination__link is-disabled">Trước</span>
            </c:otherwise>
        </c:choose>

        <strong class="hk-pagination__page">Trang ${currentPage} / ${totalPages}</strong>

        <c:choose>
            <c:when test="${currentPage lt totalPages and not empty paginationNextUrl}">
                <a class="hk-pagination__link" href="${paginationNextUrl}">Sau</a>
            </c:when>
            <c:otherwise>
                <span class="hk-pagination__link is-disabled">Sau</span>
            </c:otherwise>
        </c:choose>
    </nav>
</c:if>
