(function () {
    // Room management uses a tiny modal controller and confirm hooks.
    const modalState = {
        room: document.getElementById('roomModal'),
        task: document.getElementById('taskModal')
    };

    // Open the requested modal and update its accessibility state.
    function openModal(kind) {
        const modal = modalState[kind];
        if (!modal) return;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }

    // Reset the room modal into create mode.
    function openRoomModal() {
        removeTemporaryRoomTypeOption();
        setText('roomModalTitle', 'Thêm phòng');
        setValue('roomId', '');
        setValue('roomNumber', '');
        setValue('roomFloor', '');
        setValue('roomTypeSelect', '');
        setValue('roomStatus', 'AVAILABLE');
        setValue('roomDescription', '');
        openModal('room');
    }

    function openTaskModal(roomId, roomNumber) {
        setValue('taskRoomId', roomId);
        setValue('taskRoomNumber', roomNumber);
        setValue('taskTypeSelect', 'PERIODIC_INSPECTION');
        setValue('taskAssignee', '');
        setValue('taskPriority', 'NORMAL');
        setValue('taskNote', '');
        openModal('task');
    }

    // Close the modal and restore its hidden state.
    function closeModal(modal) {
        if (!modal) return;
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }

    // Write a value into an input or textarea if it exists.
    function setValue(id, value) {
        const input = document.getElementById(id);
        if (input) {
            input.value = value ?? '';
        }
    }

    // Remove any temporary room type option that was added for edit mode.
    function removeTemporaryRoomTypeOption() {
        const select = document.getElementById('roomTypeSelect');
        if (!select) return;
        select.querySelectorAll('option[data-room-mgmt-temp-room-type="true"]').forEach((option) => option.remove());
    }

    // Make sure an option exists for an inactive room type when editing.
    function ensureRoomTypeOption(roomTypeId, roomTypeName) {
        const select = document.getElementById('roomTypeSelect');
        if (!select || !roomTypeId) return;

        const normalizedId = String(roomTypeId);
        const existingOption = select.querySelector(`option[value="${normalizedId}"]`);
        if (existingOption) {
            return;
        }

        const option = document.createElement('option');
        option.value = normalizedId;
        option.textContent = roomTypeName ? `${roomTypeName} (ngừng hoạt động)` : `Loại phòng #${normalizedId}`;
        option.dataset.roomMgmtTempRoomType = 'true';
        select.appendChild(option);
    }

    // Toggle read-only mode for a specific field.
    function setReadonly(id, readonly) {
        const input = document.getElementById(id);
        if (input) {
            input.readOnly = readonly;
        }
    }

    // Limit the target field to digits only.
    function setDigitsOnly(id) {
        const input = document.getElementById(id);
        if (!input || input.dataset.digitsBound === 'true') {
            return;
        }
        input.dataset.digitsBound = 'true';
        input.addEventListener('input', () => {
            // Strip every non-digit character from the price field.
            input.value = (input.value || '').replace(/[^\d]/g, '');
        });
    }

    // Prevent negative values and enforce a bounded integer range for room floor.
    function setBoundedIntegerOnly(id, min, max) {
        const input = document.getElementById(id);
        if (!input || input.dataset.boundedIntegerBound === 'true') {
            return;
        }
        input.dataset.boundedIntegerBound = 'true';

        const sanitize = () => {
            const digitsOnly = (input.value || '').replace(/[^\d]/g, '');
            if (digitsOnly === '') {
                input.value = '';
                return;
            }

            const parsed = Number.parseInt(digitsOnly, 10);
            if (Number.isNaN(parsed)) {
                input.value = '';
                return;
            }

            const clamped = Math.min(Math.max(parsed, min), max);
            input.value = String(clamped);
        };

        input.addEventListener('beforeinput', (event) => {
            if (event.data && /[^0-9]/.test(event.data)) {
                event.preventDefault();
            }
        });

        input.addEventListener('keydown', (event) => {
            if (event.key === '-' || event.key === 'Minus') {
                event.preventDefault();
            }
        });

        input.addEventListener('input', sanitize);
        input.addEventListener('paste', () => {
            window.setTimeout(sanitize, 0);
        });
    }

    // Update headings that are not form fields.
    // Write plain text into a heading or label node.
    function setText(id, value) {
        const node = document.getElementById(id);
        if (node) {
            node.textContent = value ?? '';
        }
    }

    // Bind the buttons that open each management modal.
    function bindOpenButtons() {
        document.querySelectorAll('[data-room-mgmt-open]').forEach((button) => {
            button.addEventListener('click', () => {
                const kind = button.getAttribute('data-room-mgmt-open');
                if (kind === 'room') {
                    openRoomModal();
                }
            });
        });
    }

    // Bind edit actions so the room modal opens with existing values.
    function bindEditButtons() {
        document.querySelectorAll('[data-room-mgmt-edit-room="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                removeTemporaryRoomTypeOption();
                setText('roomModalTitle', 'Sửa phòng');
                setValue('roomId', button.dataset.roomId);
                setValue('roomNumber', button.dataset.roomNumber);
                setValue('roomFloor', button.dataset.roomFloor);
                ensureRoomTypeOption(button.dataset.roomTypeId, button.dataset.roomTypeName);
                setValue('roomTypeSelect', button.dataset.roomTypeId);
                setValue('roomStatus', button.dataset.roomStatus || 'AVAILABLE');
                setValue('roomDescription', button.dataset.roomDescription);
                openModal('room');
            });
        });
    }

    // Bind the shared close buttons used by both modals.
    function bindCloseButtons() {
        document.querySelectorAll('[data-room-mgmt-close="true"], [data-task-mgmt-close="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                const modal = button.closest('.room-management-modal');
                closeModal(modal);
            });
        });
    }

    // Ask for confirmation before executing destructive actions.
    function bindConfirmLinks() {
        document.querySelectorAll('[data-room-mgmt-confirm="true"]').forEach((link) => {
            link.addEventListener('click', (event) => {
                const message = link.getAttribute('data-room-mgmt-confirm-message') || 'Bạn có chắc chắn không?';
                if (!window.confirm(message)) {
                    event.preventDefault();
                }
            });
        });
    }

    // Close any open modal when Escape is pressed.
    document.addEventListener('keydown', (event) => {
        if (event.key !== 'Escape') return;
        document.querySelectorAll('.room-management-modal.is-open').forEach((modal) => closeModal(modal));
    });

    bindOpenButtons();
    bindEditButtons();
    bindCloseButtons();
    bindConfirmLinks();
    setBoundedIntegerOnly('roomFloor', 0, 7);

    window.RoomManagement = {
        openModal,
        openRoomModal,
        openTaskModal
    };
})();
