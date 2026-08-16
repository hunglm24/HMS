<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal" id="reportIssueModal" aria-hidden="true">
    <div class="modal-content">
        <div class="modal-header"><h3>Báo cáo sự cố</h3><button type="button" class="close-btn" data-close-modal>&times;</button></div>
        <form method="post" action="${pageContext.request.contextPath}/housekeeping/tasks/report-issue">
            <label>Loại sự cố<select name="issueType"><option>Hư hỏng</option><option>Thiếu đồ</option><option>Bảo trì</option></select></label>
            <label>Mô tả<textarea name="description" required></textarea></label>
            <button type="submit">Gửi báo cáo</button>
        </form>
    </div>
</div>
