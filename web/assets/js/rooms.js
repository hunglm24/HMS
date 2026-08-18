(function () {
    // Room management uses a tiny modal controller and confirm hooks.
    const modalState = {
        roomType: document.getElementById('roomTypeModal'),
        room: document.getElementById('roomModal')
    };

    function normalizeModalKind(kind) {
        if (kind === 'room-type') {
            return 'roomType';
        }
        return kind;
    }

    // Open the requested modal and update its accessibility state.
    function openModal(kind) {
        const modal = modalState[normalizeModalKind(kind)];
        if (!modal) return;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }

    // Reset the room type modal into create mode.
    function openRoomTypeModal() {
        setText('roomTypeModalTitle', 'Thêm loại phòng');
        setValue('roomTypeIdField', '');
        setValue('roomTypeName', '');
        setValue('roomTypePrice', '');
        setReadonly('roomTypePrice', false);
        setText('roomTypePriceHint', 'Giá sẽ được nhập khi tạo mới.');
        setDigitsOnly('roomTypePrice');
        setValue('roomTypeCapacity', '');
        setValue('roomTypeDescription', '');
        setValue('roomTypeStatus', 'ACTIVE');
        openModal('room-type');
    }

    // Reset the room modal into create mode.
    function openRoomModal() {
        setText('roomModalTitle', 'Thêm phòng');
        setValue('roomId', '');
        setValue('roomNumber', '');
        setValue('roomFloor', '');
        setValue('roomTypeSelect', '');
        setValue('roomStatus', 'AVAILABLE');
        setValue('roomDescription', '');
        openModal('room');
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
                if (kind === 'room-type') {
                    openRoomTypeModal();
                }
                if (kind === 'room') {
                    openRoomModal();
                }
            });
        });
    }

    // Bind edit actions so each modal opens with existing values.
    function bindEditButtons() {
        document.querySelectorAll('[data-room-mgmt-edit-room-type="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                setText('roomTypeModalTitle', 'Sửa loại phòng');
                setValue('roomTypeIdField', button.dataset.roomTypeId);
                setValue('roomTypeName', button.dataset.roomTypeName);
                setValue('roomTypePrice', button.dataset.roomTypeBasePrice);
                setReadonly('roomTypePrice', true);
                setText('roomTypePriceHint', 'Giá hiện tại sẽ được giữ nguyên khi sửa.');
                setDigitsOnly('roomTypePrice');
                setValue('roomTypeCapacity', button.dataset.roomTypeCapacity);
                setValue('roomTypeDescription', button.dataset.roomTypeDescription);
                setValue('roomTypeStatus', button.dataset.roomTypeStatus || 'ACTIVE');
                openModal('roomType');
            });
        });

        document.querySelectorAll('[data-room-mgmt-edit-room="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                setText('roomModalTitle', 'Sửa phòng');
                setValue('roomId', button.dataset.roomId);
                setValue('roomNumber', button.dataset.roomNumber);
                setValue('roomFloor', button.dataset.roomFloor);
                setValue('roomTypeSelect', button.dataset.roomTypeId);
                setValue('roomStatus', button.dataset.roomStatus || 'AVAILABLE');
                setValue('roomDescription', button.dataset.roomDescription);
                openModal('room');
            });
        });
    }

    // Bind the shared close buttons used by both modals.
    function bindCloseButtons() {
        document.querySelectorAll('[data-room-mgmt-close="true"]').forEach((button) => {
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

    window.RoomManagement = {
        openModal,
        openRoomTypeModal,
        openRoomModal
    };
})();
