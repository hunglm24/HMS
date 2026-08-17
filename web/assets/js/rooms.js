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

    function openModal(kind) {
        const modal = modalState[normalizeModalKind(kind)];
        if (!modal) return;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }

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

    function closeModal(modal) {
        if (!modal) return;
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }

    function setValue(id, value) {
        const input = document.getElementById(id);
        if (input) {
            input.value = value ?? '';
        }
    }

    function setReadonly(id, readonly) {
        const input = document.getElementById(id);
        if (input) {
            input.readOnly = readonly;
        }
    }

    function setDigitsOnly(id) {
        const input = document.getElementById(id);
        if (!input || input.dataset.digitsBound === 'true') {
            return;
        }
        input.dataset.digitsBound = 'true';
        input.addEventListener('input', () => {
            input.value = (input.value || '').replace(/[^\d]/g, '');
        });
    }

    // Update headings that are not form fields.
    function setText(id, value) {
        const node = document.getElementById(id);
        if (node) {
            node.textContent = value ?? '';
        }
    }

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

    function bindCloseButtons() {
        document.querySelectorAll('[data-room-mgmt-close="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                const modal = button.closest('.room-management-modal');
                closeModal(modal);
            });
        });
    }

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
