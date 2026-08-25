/**
 * Issue Report JavaScript
 * Hotel Management System (HMS)
 */
document.addEventListener('DOMContentLoaded', function () {
    const roomSelect = document.getElementById('roomId');
    const container = document.getElementById('equipmentListContainer');
    const issueForm = document.getElementById('issue-report-form');

    if (roomSelect && container) {
        roomSelect.addEventListener('change', function () {
            const roomId = this.value;
            const baseUrl = this.getAttribute('data-fetch-url') || (window.location.pathname + '?action=getEquipments&roomId=');
            
            if (!roomId) {
                container.innerHTML = '<span class="text-secondary">-- Chọn phòng trước --</span>';
                return;
            }

            container.innerHTML = '<span class="text-secondary"><i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i> Đang tải danh sách thiết bị...</span>';

            fetch(baseUrl + roomId)
                .then(function (response) { return response.text(); })
                .then(function (html) {
                    container.innerHTML = html;
                })
                .catch(function (err) {
                    container.innerHTML = '<span class="text-danger">Lỗi khi tải danh sách thiết bị. Vui lòng thử lại.</span>';
                });
        });

        // Auto trigger if room is pre-selected
        if (roomSelect.value) {
            roomSelect.dispatchEvent(new Event('change'));
        }
    }

    if (issueForm) {
        issueForm.addEventListener('submit', function (e) {
            const submitBtn = issueForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i> Đang gửi báo cáo...';
            }
        });
    }
});
