(function () {
    const form = document.getElementById('hotelConfigForm');
    if (!form) {
        return;
    }

    const fields = ['hotelName', 'checkInTime', 'checkOutTime', 'sameDayRefundRate', 'beforeDayRefundRate', 'taxRate', 'serviceFeeRate'];
    const previewMap = {
        hotelName: document.querySelector('[data-preview="hotelName"]'),
        checkInTime: document.querySelector('[data-preview="checkInTime"]'),
        checkOutTime: document.querySelector('[data-preview="checkOutTime"]'),
        sameDayRefundRate: document.querySelector('[data-preview="sameDayRefundRate"]'),
        beforeDayRefundRate: document.querySelector('[data-preview="beforeDayRefundRate"]'),
        taxRate: document.querySelector('[data-preview="taxRate"]'),
        serviceFeeRate: document.querySelector('[data-preview="serviceFeeRate"]')
    };

    const dirtyBanner = document.createElement('div');
    dirtyBanner.className = 'config-toast is-error';
    dirtyBanner.textContent = 'Có thay đổi chưa lưu.';
    dirtyBanner.style.display = 'none';
    dirtyBanner.setAttribute('aria-live', 'polite');
    form.parentElement.insertBefore(dirtyBanner, form.nextSibling);

    const initialValues = {};
    fields.forEach((name) => {
        const input = form.elements.namedItem(name);
        initialValues[name] = input ? input.value : '';
    });

    function isDirty() {
        return fields.some((name) => {
            const input = form.elements.namedItem(name);
            if (!input) {
                return false;
            }
            return String(input.value).trim() !== String(initialValues[name]).trim();
        });
    }

    function syncPreview() {
        fields.forEach((name) => {
            const input = form.elements.namedItem(name);
            const preview = previewMap[name];
            if (!input || !preview) {
                return;
            }
            const value = String(input.value).trim();
            preview.textContent = value || '-';
        });
        dirtyBanner.style.display = isDirty() ? 'block' : 'none';
    }

    form.addEventListener('input', syncPreview);
    form.addEventListener('change', syncPreview);
    form.addEventListener('reset', () => {
        window.setTimeout(syncPreview, 0);
    });

    window.addEventListener('beforeunload', (event) => {
        if (!isDirty()) {
            return;
        }
        event.preventDefault();
        event.returnValue = '';
    });

    syncPreview();
})();
