/**
 * Amenity Form JavaScript
 * Hotel Management System (HMS)
 */
document.addEventListener('DOMContentLoaded', function () {
    const iconInput = document.getElementById('iconInput');
    const previewIconEl = document.getElementById('previewIconEl');
    if (iconInput && previewIconEl) {
        iconInput.addEventListener('input', function () {
            const val = this.value && this.value.trim() ? this.value.trim() : 'fa-solid fa-star';
            previewIconEl.className = val;
        });
    }

    const amenityForm = document.querySelector('form.amenity-form');
    if (amenityForm) {
        const submitBtn = amenityForm.querySelector('button[type="submit"]');
        amenityForm.addEventListener('submit', function (e) {
            const nameInput = amenityForm.querySelector('input[name="name"]');
            if (nameInput && !nameInput.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập tên tiện nghi.');
                nameInput.focus();
                return;
            }
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i> Đang lưu...';
            }
        });
    }
});
