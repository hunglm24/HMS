(function () {
    const DEFAULT_PAGE_SIZE = 5;

    function getStorageKey(root) {
        return `pagination:${root.dataset.paginationKey || root.id || root.className || 'default'}`;
    }

    function getPageSize(root) {
        const parsed = Number.parseInt(root.dataset.paginationSize || `${DEFAULT_PAGE_SIZE}`, 10);
        return Number.isFinite(parsed) && parsed > 0 ? parsed : DEFAULT_PAGE_SIZE;
    }

    function getItems(root) {
        return Array.from(root.querySelectorAll('[data-pagination-item]'));
    }

    function clampPage(page, totalPages) {
        return Math.min(Math.max(page, 1), Math.max(totalPages, 1));
    }

    function createButton(label, page, disabled, isActive) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = `room-management-pagination__page${isActive ? ' is-active' : ''}`;
        button.textContent = label;
        button.dataset.page = String(page);
        button.disabled = disabled;
        return button;
    }

    function renderPagination(root) {
        const items = getItems(root);
        const controls = root.querySelector('[data-pagination-controls]');
        if (!controls) return;

        const pageSize = getPageSize(root);
        const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
        const storageKey = getStorageKey(root);
        const savedPage = Number.parseInt(sessionStorage.getItem(storageKey) || '1', 10);
        let currentPage = clampPage(Number.isFinite(savedPage) ? savedPage : 1, totalPages);

        function applyPage(page) {
            currentPage = clampPage(page, totalPages);
            sessionStorage.setItem(storageKey, String(currentPage));

            items.forEach((item, index) => {
                const start = (currentPage - 1) * pageSize;
                const end = start + pageSize;
                item.hidden = index < start || index >= end;
            });

            controls.innerHTML = '';

            if (totalPages <= 1) {
                controls.hidden = true;
                return;
            }

            controls.hidden = false;

            controls.appendChild(createButton('‹ Trước', currentPage - 1, currentPage === 1, false));

            const info = document.createElement('strong');
            info.className = 'room-management-pagination__info';
            info.textContent = `Trang ${currentPage} / ${totalPages}`;
            controls.appendChild(info);

            controls.appendChild(createButton('Sau ›', currentPage + 1, currentPage === totalPages, false));
        }

        controls.addEventListener('click', (event) => {
            const button = event.target.closest('button[data-page]');
            if (!button || button.disabled) return;
            applyPage(Number.parseInt(button.dataset.page, 10));
        });

        applyPage(currentPage);
    }

    function init() {
        document.querySelectorAll('[data-pagination-root]').forEach(renderPagination);
    }

    document.addEventListener('DOMContentLoaded', init);
})();
