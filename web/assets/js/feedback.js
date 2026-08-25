/**
 * Feedback Module JavaScript
 * Hotel Management System (HMS)
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Feedback Form Enhancements
    const feedbackForm = document.querySelector('form[action*="/customer/feedback"]');
    if (feedbackForm) {
        const commentTextarea = feedbackForm.querySelector('#comment');
        const submitBtn = feedbackForm.querySelector('button[type="submit"]');

        // Character counter
        if (commentTextarea) {
            const charCountEl = document.createElement('div');
            charCountEl.className = 'feedback-char-count';
            charCountEl.textContent = '0 / 500 ký tự';
            commentTextarea.parentNode.appendChild(charCountEl);

            commentTextarea.addEventListener('input', function () {
                const currentLength = this.value.length;
                charCountEl.textContent = currentLength + ' / 500 ký tự';
                if (currentLength > 500) {
                    charCountEl.style.color = '#ef4444';
                } else {
                    charCountEl.style.color = '#9ca3af';
                }
            });
        }

        // Form submission validation
        feedbackForm.addEventListener('submit', function (e) {
            const ratingChecked = feedbackForm.querySelector('input[name="rating"]:checked');
            if (!ratingChecked) {
                e.preventDefault();
                alert('Vui lòng chọn số sao đánh giá (từ 1 đến 5 sao).');
                return;
            }

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 8px;"></i> Đang gửi...';
            }
        });
    }

    // 2. Manager Status Toggle Confirmation
    const toggleForms = document.querySelectorAll('form[action*="/manager/feedbacks"]');
    toggleForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const statusInput = form.querySelector('input[name="status"]');
            if (statusInput && statusInput.value === 'HIDDEN') {
                const confirmHide = confirm('Bạn có chắc chắn muốn ẩn đánh giá này khỏi trang chủ?');
                if (!confirmHide) {
                    e.preventDefault();
                }
            }
        });
    });
});
