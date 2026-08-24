(function () {
    const EQUIPMENT_STATUS_OPTIONS = [
        { value: 'NORMAL', label: 'Bình thường' },
        { value: 'DAMAGED', label: 'Hư hỏng' },
        { value: 'MISSING', label: 'Thiếu / thất lạc' },
        { value: 'WAITING_REPAIR', label: 'Chờ sửa chữa' },
        { value: 'WAITING_REPLACEMENT', label: 'Chờ thay thế' },
        { value: 'MAINTENANCE', label: 'Bảo trì' }
    ];

    const form = document.querySelector('.room-form');
    if (!form) {
        return;
    }

    const selectedBody = form.querySelector('[data-room-equipment-selected-body]');
    const catalogBody = form.querySelector('[data-room-equipment-catalog-body]');

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function renderStatusOptions(selectedValue) {
        return EQUIPMENT_STATUS_OPTIONS.map((option) => {
            const selected = option.value === selectedValue ? ' selected' : '';
            return `<option value="${option.value}"${selected}>${option.label}</option>`;
        }).join('');
    }

    function renderEmptyRow() {
        return `
            <tr class="room-equipment-empty-row" data-pagination-item>
                <td colspan="5">
                    <div class="room-equipment-empty">
                        <strong>Chưa có thiết bị nào được gán.</strong>
                        <span>Hãy chọn thiết bị từ danh mục bên dưới.</span>
                    </div>
                </td>
            </tr>
        `;
    }

    function renderEquipmentRow({ id, name, quantity, status, note }) {
        const normalizedId = String(id || '');
        const safeName = escapeHtml(name || `Thiết bị #${normalizedId}`);
        const safeQuantity = Number.isFinite(Number(quantity)) && Number(quantity) > 0 ? String(quantity) : '1';
        const safeStatus = EQUIPMENT_STATUS_OPTIONS.some((option) => option.value === status) ? status : 'NORMAL';
        const safeNote = escapeHtml(note || '');

        return `
            <tr data-pagination-item data-room-equipment-row data-equipment-id="${normalizedId}">
                <td>
                    <div class="room-equipment-name">
                        <strong>${safeName}</strong>
                        <small>#${normalizedId}</small>
                    </div>
                    <input type="hidden" name="equipmentId" value="${normalizedId}" />
                </td>
                <td>
                    <input type="number" name="equipmentQuantity" min="1" value="${safeQuantity}" required />
                </td>
                <td>
                    <select name="equipmentStatus" class="room-equipment-status-select" required>
                        ${renderStatusOptions(safeStatus)}
                    </select>
                </td>
                <td>
                    <textarea name="equipmentNote" rows="2" maxlength="500" placeholder="Ghi chú tùy chọn">${safeNote}</textarea>
                </td>
                <td class="room-equipment-row__actions">
                    <button type="button" class="btn btn-secondary btn-sm" data-room-equipment-remove>Xóa</button>
                </td>
            </tr>
        `;
    }

    function getSelectedEquipmentRows() {
        if (!selectedBody) {
            return [];
        }

        return Array.from(selectedBody.querySelectorAll('[data-room-equipment-row]')).map((row) => {
            const equipmentId = row.dataset.equipmentId || '';
            const quantityInput = row.querySelector('input[name="equipmentQuantity"]');
            const statusSelect = row.querySelector('select[name="equipmentStatus"]');
            const noteInput = row.querySelector('textarea[name="equipmentNote"]');

            return {
                id: equipmentId,
                name: row.querySelector('.room-equipment-name strong')?.textContent || `Thiết bị #${equipmentId}`,
                quantity: quantityInput ? quantityInput.value : '1',
                status: statusSelect ? statusSelect.value : 'NORMAL',
                note: noteInput ? noteInput.value : ''
            };
        });
    }

    function notifyChanged() {
        if (window.RoomFormCore && typeof window.RoomFormCore.saveDraft === 'function') {
            window.RoomFormCore.saveDraft();
        }
    }

    function syncCatalogButtons() {
        const selectedIds = new Set(getSelectedEquipmentRows().map((row) => row.id));
        form.querySelectorAll('[data-room-equipment-add]').forEach((button) => {
            const equipmentId = String(button.dataset.equipmentId || '');
            const isSelected = selectedIds.has(equipmentId);
            button.disabled = isSelected;
            button.textContent = isSelected ? 'Đã thêm' : 'Thêm';
        });
    }

    function refreshPagination() {
        if (window.RoomPagination && typeof window.RoomPagination.refresh === 'function') {
            window.RoomPagination.refresh();
        }
    }

    function renderEquipmentRows(assignments) {
        if (!selectedBody) {
            return;
        }

        if (!assignments || !assignments.length) {
            selectedBody.innerHTML = renderEmptyRow();
            return;
        }

        selectedBody.innerHTML = assignments.map((assignment) => renderEquipmentRow(assignment)).join('');
    }

    function replaceEquipmentRows(assignments) {
        renderEquipmentRows(assignments);
        syncCatalogButtons();
        refreshPagination();
        notifyChanged();
    }

    function addEquipmentRow(data) {
        if (!selectedBody) {
            return;
        }

        const emptyRow = selectedBody.querySelector('.room-equipment-empty-row');
        if (emptyRow) {
            emptyRow.remove();
        }

        selectedBody.insertAdjacentHTML('beforeend', renderEquipmentRow(data));
        syncCatalogButtons();
        refreshPagination();
        notifyChanged();
    }

    function bindCatalogButtons() {
        if (!catalogBody) {
            return;
        }

        catalogBody.addEventListener('click', (event) => {
            const button = event.target.closest('[data-room-equipment-add]');
            if (!button || button.disabled) {
                return;
            }

            addEquipmentRow({
                id: button.dataset.equipmentId,
                name: button.dataset.equipmentName,
                quantity: 1,
                status: 'NORMAL',
                note: ''
            });
        });
    }

    function bindSelectedRows() {
        if (!selectedBody) {
            return;
        }

        selectedBody.addEventListener('click', (event) => {
            const button = event.target.closest('[data-room-equipment-remove]');
            if (!button) {
                return;
            }

            const row = button.closest('[data-room-equipment-row]');
            if (!row) {
                return;
            }

            row.remove();
            if (!selectedBody.querySelector('[data-room-equipment-row]')) {
                selectedBody.insertAdjacentHTML('afterbegin', renderEmptyRow());
            }
            syncCatalogButtons();
            refreshPagination();
            notifyChanged();
        });

        selectedBody.addEventListener('input', notifyChanged);
        selectedBody.addEventListener('change', notifyChanged);
    }

    function bootstrap() {
        bindCatalogButtons();
        bindSelectedRows();
        syncCatalogButtons();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', bootstrap);
    } else {
        bootstrap();
    }

    window.RoomFormEquipment = {
        addEquipmentRow,
        replaceEquipmentRows,
        getSelectedEquipmentRows,
        syncCatalogButtons,
        refreshPagination,
        renderEmptyRow
    };
})();
