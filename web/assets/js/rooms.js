(function () {
    const modalState = {
        task: document.getElementById('taskModal')
    };

    function getTaskForm() {
        return modalState.task ? modalState.task.querySelector('form') : null;
    }

    function setValue(id, value) {
        const input = document.getElementById(id);
        if (input) {
            input.value = value ?? '';
        }
    }

    function closeModal(modal) {
        if (!modal) return;
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }

    function openModal(kind) {
        const modal = modalState[kind];
        if (!modal) return;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }

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
            targetHk = workloads.find(function (h) { return String(h.userId) === String(selectedId); });
        }

        if (!targetHk) {
            noticeEl.style.display = 'none';
            return;
        }

        if (targetHk.inProgressCount > 0) {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-warning';
            noticeText.textContent = (isDefault ? 'Nhan vien mac dinh theo tang (' : '') + targetHk.fullName + (isDefault ? ')' : '') +
                ' dang ban don phong ' + (targetHk.currentRoomNumber || 'khac') +
                (targetHk.pendingCount > 0 ? ' (co ' + targetHk.pendingCount + ' viec cho).' : '.') +
                ' Cong viec nay se duoc xep vao hang doi cho xu ly.';
        } else if (targetHk.pendingCount > 0) {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-info';
            noticeText.textContent = (isDefault ? 'Nhan vien mac dinh theo tang (' : '') + targetHk.fullName + (isDefault ? ')' : '') +
                ' dang co ' + targetHk.pendingCount + ' viec cho trong hang doi.';
        } else {
            noticeEl.style.display = 'flex';
            noticeEl.className = 'task-queue-notice is-success';
            noticeText.textContent = 'Nhan vien ' + targetHk.fullName + ' dang ranh va san sang thuc hien ngay.';
        }
    }

    function syncTaskModalFields() {
        const typeSelect = document.getElementById('taskTypeSelect');
        const cleaningGroup = document.getElementById('cleaningTasksGroup');
        if (typeSelect && cleaningGroup) {
            cleaningGroup.style.display = typeSelect.value === 'CHECKOUT_INSPECTION' ? 'none' : 'block';
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
        const floorText = currentTaskFloor > 0 ? ' (Tang ' + currentTaskFloor + ')' : '';
        setValue('taskRoomNumber', 'Phong ' + (roomNumber || '') + floorText);
        setValue('taskTypeSelect', 'CHECKOUT_INSPECTION');
        setValue('taskPriority', 'NORMAL');
        setValue('taskCleaningTasks', '');
        setValue('taskNote', '');
        setValue('taskAssigneeSelect', '');

        const defaultHk = getDefaultHkForFloor(currentTaskFloor);
        const nameEl = document.getElementById('taskDefaultHkName');
        const badgeEl = document.getElementById('taskDefaultHkBadge');
        const subEl = document.getElementById('taskDefaultHkSub');

        if (defaultHk) {
            if (nameEl) {
                const floorRoleText = currentTaskFloor <= 2 ? 'Phu trach tang 1 - 2' : 'Phu trach tang 3+';
                nameEl.textContent = defaultHk.fullName + ' (' + floorRoleText + ')';
            }
            if (badgeEl) {
                badgeEl.className = 'hk-status-badge ' + (defaultHk.badgeClass || 'badge-available');
                badgeEl.textContent = defaultHk.badgeText || 'Dang ranh';
            }
            if (subEl) {
                subEl.innerHTML = '<span>Hom nay: <strong>' + (defaultHk.completedToday || 0) + '</strong> phong da hoan thanh</span>';
            }
        } else {
            if (nameEl) nameEl.textContent = 'Chua co nhan vien Housekeeping';
            if (badgeEl) {
                badgeEl.className = 'hk-status-badge badge-pending';
                badgeEl.textContent = 'Chua phan cong';
            }
        }

        updateTaskAssigneeFeedback();
        syncTaskModalFields();
        openModal('task');
    }

    function bindCloseButtons() {
        document.querySelectorAll('[data-task-mgmt-close="true"]').forEach((button) => {
            button.addEventListener('click', () => {
                const modal = button.closest('.room-management-modal');
                closeModal(modal);
            });
        });
    }

    function bindConfirmLinks() {
        document.querySelectorAll('[data-room-mgmt-confirm="true"]').forEach((link) => {
            link.addEventListener('click', (event) => {
                const message = link.getAttribute('data-room-mgmt-confirm-message') || 'Ban co chac chan khong?';
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

    bindCloseButtons();
    bindConfirmLinks();

    const taskAssigneeEl = document.getElementById('taskAssigneeSelect');
    if (taskAssigneeEl) {
        taskAssigneeEl.addEventListener('change', updateTaskAssigneeFeedback);
    }

    window.RoomManagement = {
        openModal,
        openTaskModal
    };
})();
