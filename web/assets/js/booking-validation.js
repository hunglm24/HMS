(() => {
    function todayIso() {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        return now.toISOString().slice(0, 10);
    }

    function attachDateValidation() {
        const today = todayIso();
        document.querySelectorAll('input[type="date"]').forEach((input) => {
            input.min = input.min || today;
        });

        document.querySelectorAll('form').forEach((form) => {
            const checkIn = form.querySelector('input[name="checkIn"]');
            const checkOut = form.querySelector('input[name="checkOut"]');
            if (!checkIn && !checkOut) return;

            const validate = () => {
                if (checkIn) checkIn.setCustomValidity('');
                if (checkOut) checkOut.setCustomValidity('');

                if (checkIn && checkIn.value && checkIn.value < today) {
                    checkIn.setCustomValidity('Không được chọn ngày trong quá khứ.');
                }
                if (checkOut && checkOut.value && checkOut.value < today) {
                    checkOut.setCustomValidity('Không được chọn ngày trong quá khứ.');
                }
                if (checkIn && checkOut && checkIn.value && checkOut.value && checkOut.value <= checkIn.value) {
                    checkOut.setCustomValidity('Ngày trả phòng phải sau ngày nhận phòng.');
                }
            };

            checkIn?.addEventListener('change', validate);
            checkOut?.addEventListener('change', validate);
            form.addEventListener('submit', (event) => {
                validate();
                if (!form.checkValidity()) {
                    event.preventDefault();
                    form.reportValidity();
                }
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', attachDateValidation);
    } else {
        attachDateValidation();
    }
})();
