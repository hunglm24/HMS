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

    function getPaginationKey(root) {
        return root.dataset.paginationKey || root.id || root.className || 'default';
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

    function getControlsForGroup(key) {
        return document.querySelector(`[data-pagination-controls][data-pagination-target="${key}"]`);
    }

    function renderPaginationGroup(roots) {
        if (!roots.length) {
            return;
        }

        const key = getPaginationKey(roots[0]);
        const controls = getControlsForGroup(key);
        if (!controls) {
            return;
        }

        const pageSize = getPageSize(roots[0]);
        const itemsByRoot = roots.map((root) => getItems(root));
        const totalItems = itemsByRoot[0].length;
        const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
        const storageKey = getStorageKey(roots[0]);
        const savedPage = Number.parseInt(sessionStorage.getItem(storageKey) || '1', 10);
        let currentPage = clampPage(Number.isFinite(savedPage) ? savedPage : 1, totalPages);

        function applyPage(page) {
            currentPage = clampPage(page, totalPages);
            sessionStorage.setItem(storageKey, String(currentPage));

            itemsByRoot.forEach((items) => {
                items.forEach((item, index) => {
                    const start = (currentPage - 1) * pageSize;
                    const end = start + pageSize;
                    const isHidden = index < start || index >= end;
                    item.hidden = isHidden;
                    if (isHidden) {
                        item.style.setProperty('display', 'none', 'important');
                    } else {
                        item.style.removeProperty('display');
                    }
                });
            });

            controls.innerHTML = '';

            if (totalPages <= 1) {
                controls.hidden = true;
                return;
            }

            controls.hidden = false;
            controls.appendChild(createButton('Trước', currentPage - 1, currentPage === 1, false));

            const info = document.createElement('strong');
            info.className = 'room-management-pagination__info';
            info.textContent = `Trang ${currentPage} / ${totalPages}`;
            controls.appendChild(info);

            controls.appendChild(createButton('Sau', currentPage + 1, currentPage === totalPages, false));
        }

        controls.__paginationApplyPage = applyPage;
        if (!controls.__paginationBound) {
            controls.addEventListener('click', (event) => {
                const button = event.target.closest('button[data-page]');
                const apply = controls.__paginationApplyPage;
                if (!button || button.disabled || typeof apply !== 'function') {
                    return;
                }
                apply(Number.parseInt(button.dataset.page, 10));
            });
            controls.__paginationBound = true;
        }

        applyPage(currentPage);
    }

    function init() {
        const groupedRoots = new Map();

        document.querySelectorAll('[data-pagination-root]').forEach((root) => {
            const key = getPaginationKey(root);
            if (!groupedRoots.has(key)) {
                groupedRoots.set(key, []);
            }
            groupedRoots.get(key).push(root);
        });

        groupedRoots.forEach((roots) => renderPaginationGroup(roots));
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.RoomPagination = window.RoomPagination || {};
    window.RoomPagination.refresh = init;
})();
