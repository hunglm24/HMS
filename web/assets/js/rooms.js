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

    // Parse Housekeeper workload data
    function getHkWorkloads() {
        const dataEl = document.getElementById('housekeeperWorkloadData');
        if (!dataEl) return [];
        try {
            return JSON.parse(dataEl.textContent || '[]');
        } catch (e) {
            return [];
        }
    }

    let currentTaskFloor = 0;

    function getDefaultHkForFloor(floor) {
        const list = getHkWorkloads();
        if (!list || list.length === 0) return null;
        if (list.length === 1) return list[0];
        const fl = Number(floor) || 0;
        if (fl <= 2) return list[0];
        return list[1] || list[0];
    }

    function updateTaskAssigneeFeedback() {
        const select = document.getElementById('taskAssigneeSelect');
        const noticeEl = document.getElementById('taskQueueNotice');
        const noticeText = document.getElementById('taskQueueNoticeText');
        if (!noticeEl || !noticeText) return;

        const workloads = getHkWorkloads();
        const selectedId = select ? select.value : '';

        let targetHk = null;
        let isDefault = false;

        if (!selectedId) {
            targetHk = getDefaultHkForFloor(currentTaskFloor);
            isDefault = true;
        } else {
            targetHk = workloads.find(function(h) { return String(h.userId) === String(selectedId); });
        }

        if (!targetHk) {
            noticeEl.style.display = 'none';
            return;
        }

        if (targetHk.inProgressCount > 0) {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-warning';
            noticeText.textContent = (isDefault ? 'Nhân viên mặc định theo tầng (' : '') + targetHk.fullName + (isDefault ? ')' : '') +
                ' đang bận dọn phòng ' + (targetHk.currentRoomNumber || 'khác') +
                (targetHk.pendingCount > 0 ? ' (có ' + targetHk.pendingCount + ' việc chờ).' : '.') +
                ' Công việc này sẽ được xếp vào hàng đợi chờ xử lý.';
        } else if (targetHk.pendingCount > 0) {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-info';
            noticeText.textContent = (isDefault ? 'Nhân viên mặc định theo tầng (' : '') + targetHk.fullName + (isDefault ? ')' : '') +
                ' đang có ' + targetHk.pendingCount + ' việc chờ trong hàng đợi.';
        } else {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-success';
            noticeText.textContent = 'Nhân viên ' + targetHk.fullName + ' đang rảnh và sẵn sàng thực hiện ngay.';
        }
    }

    // Toggle cleaning tasks group visibility based on selected task type
    function syncTaskModalFields() {
        const typeSelect = document.getElementById('taskTypeSelect');
        const cleaningGroup = document.getElementById('cleaningTasksGroup');
        if (typeSelect && cleaningGroup) {
            if (typeSelect.value === 'CHECKOUT_INSPECTION') {
                cleaningGroup.style.display = 'none';
            } else {
                cleaningGroup.style.display = 'block';
            }
        }
    }
    function openTaskModal(roomId, roomNumber, floorNumber) {
        currentTaskFloor = Number(floorNumber) || 0;
        if (!currentTaskFloor && roomNumber) {
            const match = String(roomNumber).trim().match(/^(\d)/);
            if (match) {
                currentTaskFloor = Number.parseInt(match[1], 10) || 0;
            }
        }
        setValue('taskRoomId', roomId);
        const floorText = currentTaskFloor > 0 ? ' (Tầng ' + currentTaskFloor + ')' : '';
        setValue('taskRoomNumber', 'Phòng ' + (roomNumber || '') + floorText);
        setValue('taskTypeSelect', 'CHECKOUT_INSPECTION');
        setValue('taskPriority', 'NORMAL');
        setValue('taskCleaningTasks', '');
        setValue('taskNote', '');
        setValue('taskAssigneeSelect', '');

        // Update default HK card
        const defaultHk = getDefaultHkForFloor(currentTaskFloor);
        const nameEl = document.getElementById('taskDefaultHkName');
        const badgeEl = document.getElementById('taskDefaultHkBadge');
        const subEl = document.getElementById('taskDefaultHkSub');

        if (defaultHk) {
            if (nameEl) {
                const floorRoleText = currentTaskFloor <= 2 ? 'Phụ trách Tầng 1 - 2' : 'Phụ trách Tầng 3+';
                nameEl.textContent = defaultHk.fullName + ' (' + floorRoleText + ')';
            }
            if (badgeEl) {
                badgeEl.className = 'hk-status-badge ' + (defaultHk.badgeClass || 'badge-available');
                badgeEl.textContent = defaultHk.badgeText || 'Đang rảnh';
            }
            if (subEl) {
                subEl.innerHTML = '<span>Hôm nay: <strong>' + (defaultHk.completedToday || 0) + '</strong> phòng đã hoàn thành</span>';
            }
        } else {
            if (nameEl) nameEl.textContent = 'Chưa có nhân viên Housekeeping';
            if (badgeEl) {
                badgeEl.className = 'hk-status-badge badge-pending';
                badgeEl.textContent = 'Chưa phân công';
            }
        }

        updateTaskAssigneeFeedback();
        syncTaskModalFields();
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

    const taskAssigneeEl = document.getElementById('taskAssigneeSelect');
    if (taskAssigneeEl) {
        taskAssigneeEl.addEventListener('change', updateTaskAssigneeFeedback);
    }

    window.RoomManagement = {
        openModal,
        openRoomModal,
        openTaskModal
    };
})();

