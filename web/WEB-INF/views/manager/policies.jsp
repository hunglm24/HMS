<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chính sách | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260819-2">
    <style>
        .policy-page { min-width: 0; background: #f6f8fb; }
        .policy-section { background: #fff; border: 1px solid #d9e0ea; border-radius: 8px; padding: 20px; }
        .policy-form { display: grid; grid-template-columns: 1fr 1fr 220px; gap: 12px; align-items: end; }
        .policy-form label { display: grid; gap: 6px; font-size: .9rem; font-weight: 700; color: #253246; }
        .policy-form input, .policy-form select, .policy-form textarea { width: 100%; min-height: 40px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 8px 10px; font: inherit; }
        .policy-form textarea { min-height: 92px; resize: vertical; }
        .policy-form .span-all { grid-column: 1 / -1; }
        .policy-table { width: 100%; border-collapse: collapse; margin-top: 18px; background: #fff; }
        .policy-table th, .policy-table td { border-top: 1px solid #e2e8f0; padding: 12px; text-align: left; vertical-align: top; }
        .policy-table th { color: #526174; font-size: .82rem; text-transform: uppercase; }
        .inline-policy-form { display: grid; grid-template-columns: 1fr 1fr 160px; gap: 8px; align-items: end; }
        .inline-policy-form input, .inline-policy-form select, .inline-policy-form textarea { min-height: 36px; border: 1px solid #cfd8e3; border-radius: 6px; padding: 7px 8px; font: inherit; }
        .inline-policy-form textarea { grid-column: 1 / -1; min-height: 60px; }
        .status-active { color: #067647; font-weight: 800; }
        .status-inactive { color: #b42318; font-weight: 800; }
        .hint { color: #667085; font-size: .9rem; }
        @media (max-width: 900px) {
            .policy-form, .inline-policy-form { grid-template-columns: 1fr; }
            .policy-table { display: block; overflow-x: auto; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<main class="page-container policy-page">
    <section class="section-head">
        <div>
            <p class="section-kicker">Manager</p>
            <h1>Chính sách khách sạn</h1>
            <p>Thêm, sửa và xóa chính sách hủy phòng, check-in, check-out, đặt cọc hoặc quy định khác.</p>
        </div>
    </section>

    <section class="policy-section">
        <form class="policy-form" method="post" action="${cp}/manager/policies/save">
            <label>Tiêu đề<input name="title" maxlength="150" required></label>
            <label>Nhóm chính sách<input name="category" maxlength="80" placeholder="Hủy phòng, Check-in..." required></label>
            <label>Trạng thái
                <select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Tạm dừng</option></select>
            </label>
            <label class="span-all">Nội dung<textarea name="content" maxlength="2000" required></textarea></label>
            <button class="btn span-all" type="submit">Thêm chính sách</button>
        </form>

        <table class="policy-table">
            <thead><tr><th>Nhóm</th><th>Chính sách</th><th>Trạng thái</th><th>Cập nhật</th></tr></thead>
            <tbody>
            <c:forEach var="policy" items="${policies}">
                <tr>
                    <td><c:out value="${policy.category}" /></td>
                    <td><strong><c:out value="${policy.title}" /></strong><br><span class="hint"><c:out value="${policy.content}" /></span></td>
                    <td><span class="${policy.status eq 'ACTIVE' ? 'status-active' : 'status-inactive'}"><c:out value="${policy.status}" /></span></td>
                    <td>
                        <form class="inline-policy-form" method="post" action="${cp}/manager/policies/save">
                            <input type="hidden" name="id" value="${policy.id}">
                            <input name="title" value="${policy.title}" required>
                            <input name="category" value="${policy.category}" required>
                            <select name="status"><option value="ACTIVE" ${policy.status eq 'ACTIVE' ? 'selected' : ''}>Hoạt động</option><option value="INACTIVE" ${policy.status eq 'INACTIVE' ? 'selected' : ''}>Tạm dừng</option></select>
                            <textarea name="content" required>${policy.content}</textarea>
                            <button class="btn btn-secondary" type="submit">Lưu</button>
                        </form>
                        <form method="post" action="${cp}/manager/policies/delete" style="margin-top:8px">
                            <input type="hidden" name="id" value="${policy.id}">
                            <button class="btn btn-secondary" type="submit" onclick="return confirm('Xóa chính sách này?')">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty policies}"><tr><td colspan="4">Chưa có chính sách.</td></tr></c:if>
            </tbody>
        </table>
    </section>
</main>
</body>
</html>
