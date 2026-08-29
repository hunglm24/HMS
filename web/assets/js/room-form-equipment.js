(function () {
    const EQUIPMENT_STATUS_OPTIONS = [
        { value: 'NORMAL', label: 'Binh thuong' },
        { value: 'DAMAGED', label: 'Hu hong' },
        { value: 'MISSING', label: 'Thieu / that lac' },
        { value: 'WAITING_REPAIR', label: 'Cho sua chua' },
        { value: 'WAITING_REPLACEMENT', label: 'Cho thay the' },
        { value: 'MAINTENANCE', label: 'Bao tri' }
    ];
    const EQUIPMENT_NOTE_MAX_LENGTH = 50;

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
                        <strong>Chua co thiet bi nao duoc gan.</strong>
                        <span>Hay chon thiet bi tu danh muc ben duoi.</span>
                    </div>
                </td>
            </tr>
        `;
    }

    function renderEquipmentRow({ id, name, quantity, status, note }) {
        const normalizedId = String(id || '');
        const safeName = escapeHtml(name || `Thiet bi #${normalizedId}`);
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
                    <textarea name="equipmentNote" rows="2" maxlength="${EQUIPMENT_NOTE_MAX_LENGTH}" placeholder="Ghi chu tuy chon">${safeNote}</textarea>
                </td>
                <td class="room-equipment-row__actions">
                    <button type="button" class="btn btn-secondary btn-sm" data-room-equipment-remove>Xoa</button>
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
                name: row.querySelector('.room-equipment-name strong')?.textContent || `Thiet bi #${equipmentId}`,
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
            button.textContent = isSelected ? 'Da them' : 'Them';
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

    function clampEquipmentNote(target) {
        if (!target || target.name !== 'equipmentNote' || typeof target.value !== 'string') {
            return;
        }
        if (target.value.length > EQUIPMENT_NOTE_MAX_LENGTH) {
            target.value = target.value.slice(0, EQUIPMENT_NOTE_MAX_LENGTH);
        }
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

        selectedBody.addEventListener('input', (event) => {
            clampEquipmentNote(event.target);
            notifyChanged();
        });
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
