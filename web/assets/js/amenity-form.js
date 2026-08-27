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
            let nameError = amenityForm.querySelector('.name-client-error');
            if (nameInput && !nameInput.value.trim()) {
                e.preventDefault();
                if (!nameError) {
                    nameError = document.createElement('small');
                    nameError.className = 'text-danger name-client-error';
                    nameError.style.display = 'block';
                    nameError.style.marginTop = '4px';
                    nameInput.parentNode.appendChild(nameError);
                }
                nameError.textContent = 'Vui lòng nhập tên tiện nghi.';
                nameInput.style.borderColor = '#ef4444';
                nameInput.focus();
                return;
            } else {
                if (nameError) nameError.remove();
                if (nameInput) nameInput.style.borderColor = '';
            }
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 6px;"></i> Đang lưu...';
            }
        });
    }
});

