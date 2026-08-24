(function () {
    const form = document.querySelector('.room-form');
    if (!form) {
        return;
    }

    const roomNumberInput = form.querySelector('input[name="roomNumber"]');
    const floorNumberInput = form.querySelector('input[name="floorNumber"]');
    const roomTypeInput = form.querySelector('select[name="roomTypeId"]');
    const statusInput = form.querySelector('select[name="status"]');
    const descriptionInput = form.querySelector('textarea[name="description"]');
    const quickActionButtons = Array.from(form.querySelectorAll('[data-room-quick-action]'));
    const copySourceSelect = form.querySelector('[data-room-copy-source]');
    const contextPath = document.body.getAttribute('data-context-path') || '';
    const draftStorageKey = `room-form-draft:${window.location.pathname}:${form.querySelector('input[name="id"]')?.value || 'new'}`;

    let initialSnapshot = null;
    let isApplyingSnapshot = false;

    function getSelectedEquipmentRows() {
        if (window.RoomFormEquipment && typeof window.RoomFormEquipment.getSelectedEquipmentRows === 'function') {
            return window.RoomFormEquipment.getSelectedEquipmentRows();
        }

        return Array.from(form.querySelectorAll('[data-room-equipment-row]')).map((row) => {
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

    function clearValidationState() {
        form.querySelectorAll('[aria-invalid="true"]').forEach((input) => {
            input.removeAttribute('aria-invalid');
            input.setCustomValidity('');
            const field = input.closest('.room-form-field');
            if (field) {
                field.classList.remove('is-error');
            }
        });

        form.querySelectorAll('.room-form-field__error').forEach((node) => {
            node.textContent = '';
        });
    }

    function captureSnapshot() {
        return {
            roomNumber: roomNumberInput ? roomNumberInput.value : '',
            floorNumber: floorNumberInput ? floorNumberInput.value : '',
            roomTypeId: roomTypeInput ? roomTypeInput.value : '',
            status: statusInput ? statusInput.value : '',
            description: descriptionInput ? descriptionInput.value : '',
            equipments: getSelectedEquipmentRows()
        };
    }

    function applySnapshot(snapshot) {
        if (!snapshot) {
            return;
        }

        isApplyingSnapshot = true;

        if (roomNumberInput) {
            roomNumberInput.value = snapshot.roomNumber || '';
        }
        if (floorNumberInput) {
            floorNumberInput.value = snapshot.floorNumber || '';
        }
        if (roomTypeInput) {
            roomTypeInput.value = snapshot.roomTypeId || '';
        }
        if (statusInput) {
            statusInput.value = snapshot.status || '';
        }
        if (descriptionInput) {
            descriptionInput.value = snapshot.description || '';
        }
        if (window.RoomFormEquipment && typeof window.RoomFormEquipment.replaceEquipmentRows === 'function') {
            window.RoomFormEquipment.replaceEquipmentRows(snapshot.equipments || []);
        }

        clearValidationState();
        if (window.RoomFormEquipment && typeof window.RoomFormEquipment.syncCatalogButtons === 'function') {
            window.RoomFormEquipment.syncCatalogButtons();
        }
        if (window.RoomFormEquipment && typeof window.RoomFormEquipment.refreshPagination === 'function') {
            window.RoomFormEquipment.refreshPagination();
        }

        isApplyingSnapshot = false;
    }

    function saveDraft() {
        if (isApplyingSnapshot) {
            return;
        }

        try {
            sessionStorage.setItem(draftStorageKey, JSON.stringify(captureSnapshot()));
        } catch (error) {
            // Ignore storage quota or privacy errors.
        }
    }

    function loadDraft() {
        try {
            const raw = sessionStorage.getItem(draftStorageKey);
            if (!raw) {
                return false;
            }

            applySnapshot(JSON.parse(raw));
            return true;
        } catch (error) {
            return false;
        }
    }

    function resetForm() {
        if (!initialSnapshot) {
            return;
        }

        applySnapshot(initialSnapshot);
        saveDraft();
    }

    async function copyEquipmentFromRoom() {
        if (!copySourceSelect || !copySourceSelect.value) {
            window.alert('Vui lòng chọn phòng nguồn trước khi sao chép.');
            return;
        }

        try {
            const response = await fetch(`${contextPath}/api/manager/room-equipment/copy-from-room?sourceRoomId=${encodeURIComponent(copySourceSelect.value)}`, {
                headers: {
                    Accept: 'application/json'
                }
            });

            if (!response.ok) {
                window.alert('Không thể sao chép thiết bị từ phòng đã chọn.');
                return;
            }

            const payload = await response.json();
            if (!Array.isArray(payload)) {
                window.alert('Dữ liệu phòng nguồn không hợp lệ.');
                return;
            }

            const assignments = payload.map((item) => ({
                id: item.id,
                name: item.name,
                quantity: item.quantity,
                status: item.status,
                note: item.note
            }));

            if (window.RoomFormEquipment && typeof window.RoomFormEquipment.replaceEquipmentRows === 'function') {
                window.RoomFormEquipment.replaceEquipmentRows(assignments);
            }
        } catch (error) {
            window.alert('Không thể sao chép thiết bị từ phòng đã chọn.');
        }
    }

    function bindQuickActions() {
        quickActionButtons.forEach((button) => {
            button.addEventListener('click', async () => {
                const action = button.dataset.roomQuickAction;
                if (action === 'reset') {
                    resetForm();
                    return;
                }
                if (action === 'copy-equipment') {
                    await copyEquipmentFromRoom();
                }
            });
        });
    }

    function wireFormAutosave() {
        form.addEventListener('input', (event) => {
            if (event.target && event.target.closest('[data-room-quick-action]')) {
                return;
            }
            saveDraft();
        });

        form.addEventListener('change', (event) => {
            if (event.target && event.target.closest('[data-room-quick-action]')) {
                return;
            }
            saveDraft();
        });
    }

    function bootstrap() {
        initialSnapshot = captureSnapshot();
        bindQuickActions();
        wireFormAutosave();
        loadDraft();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', bootstrap);
    } else {
        bootstrap();
    }

    window.RoomFormCore = {
        saveDraft,
        loadDraft,
        resetForm,
        copyEquipmentFromRoom
    };
})();
