(function () {
    const modal = document.getElementById('roomChangeModal');
    const backdrop = document.getElementById('roomChangeBackdrop');
    if (!modal || !backdrop) return;

    // Cache modal fields once so we only touch the DOM when needed.
    const elements = {
        bookingId: document.getElementById('roomChangeBookingId'),
        currentRoomId: document.getElementById('roomChangeCurrentRoomId'),
        currentRoomNumber: document.getElementById('roomChangeCurrentRoomNumber'),
        bookingCode: document.getElementById('roomChangeBookingCode'),
        guestName: document.getElementById('roomChangeGuestName'),
        currentRoomLabel: document.getElementById('roomChangeCurrentRoomLabel'),
        currentStatus: document.getElementById('roomChangeCurrentStatus'),
        currentRoomSelect: document.getElementById('roomChangeCurrentRoomSelect'),
        newRoomSelect: document.getElementById('roomChangeNewRoomId'),
        reason: document.getElementById('roomChangeReason'),
        priceDiff: document.getElementById('roomChangePriceDiff'),
        hint: document.getElementById('roomChangeHint'),
        confirmBtn: document.getElementById('roomChangeConfirmBtn')
    };

    let activeCard = null;

    function statusTone(status) {
        // Map backend room status to the matching UI tone.
        switch ((status || '').toUpperCase()) {
            case 'OCCUPIED':
                return 'occupied';
            case 'AVAILABLE':
                return 'available';
            case 'CLEANING':
                return 'cleaning';
            case 'MAINTENANCE':
                return 'maintenance';
            default:
                return 'neutral';
        }
    }

    function setConfirmEnabled(enabled) {
        if (elements.confirmBtn) {
            elements.confirmBtn.disabled = !enabled;
        }
    }

    function syncConfirmState() {
        // The confirm button stays disabled until the modal is fully valid.
        const hasBooking = !!(elements.bookingId && elements.bookingId.value);
        const hasCurrentRoom = !!(elements.currentRoomId && elements.currentRoomId.value);
        const hasNewRoom = !!(elements.newRoomSelect && elements.newRoomSelect.value);
        const hasReason = !!(elements.reason && elements.reason.value.trim());
        setConfirmEnabled(hasBooking && hasCurrentRoom && hasNewRoom && hasReason);
    }

    function updatePriceDiff() {
        // The final price difference is only hinted here; backend decides the real value.
        if (!elements.newRoomSelect || !elements.priceDiff) {
            return;
        }
        const option = elements.newRoomSelect.selectedOptions[0];
        const currentRoomType = activeCard ? (activeCard.dataset.roomType || '') : '';
        const newRoomType = option ? (option.dataset.roomType || '') : '';

        if (!option || !option.value) {
            elements.priceDiff.textContent = 'Sẽ tính tự động';
            return;
        }

        if (currentRoomType && newRoomType && currentRoomType !== newRoomType) {
            elements.priceDiff.textContent = 'Sẽ tính theo loại phòng mới';
            return;
        }

        elements.priceDiff.textContent = 'Không phát sinh chênh lệch';
    }

    function openModal() {
        // Open both overlay and dialog together so the modal behaves like one unit.
        modal.classList.add('is-open');
        backdrop.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        backdrop.setAttribute('aria-hidden', 'false');
    }

    function closeModal() {
        // Close and reset aria state for accessibility.
        modal.classList.remove('is-open');
        backdrop.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        backdrop.setAttribute('aria-hidden', 'true');
    }

    window.openRoomChangeModal = function (card) {
        // Pull the current room context from the clicked room card.
        activeCard = card || null;

        const bookingId = card && card.dataset.bookingId ? card.dataset.bookingId : '';
        const bookingCode = card && card.dataset.bookingCode ? card.dataset.bookingCode : '--';
        const guestName = card && card.dataset.guestName ? card.dataset.guestName : '--';
        const roomId = card && card.dataset.roomId ? card.dataset.roomId : '';
        const roomNumber = card && card.dataset.roomNumber ? card.dataset.roomNumber : '--';
        const roomType = card && card.dataset.roomType ? card.dataset.roomType : '--';
        const roomStatus = card && card.dataset.roomStatusLabel ? card.dataset.roomStatusLabel : '--';
        const roomStatusTone = statusTone(card ? card.dataset.roomStatus : '');

        if (elements.bookingId) elements.bookingId.value = bookingId;
        if (elements.currentRoomId) elements.currentRoomId.value = roomId;
        if (elements.currentRoomNumber) elements.currentRoomNumber.value = roomNumber;
        if (elements.bookingCode) elements.bookingCode.textContent = bookingCode;
        if (elements.guestName) elements.guestName.textContent = guestName;
        if (elements.currentRoomLabel) elements.currentRoomLabel.textContent = `${roomNumber} ${roomType !== '--' ? '(' + roomType + ')' : ''}`.trim();
        if (elements.currentStatus) {
            elements.currentStatus.textContent = roomStatus;
            elements.currentStatus.className = 'status-badge status-' + roomStatusTone;
        }
        if (elements.currentRoomSelect) {
            elements.currentRoomSelect.value = `${roomNumber} ${roomType !== '--' ? '(' + roomType + ')' : ''}`.trim();
        }
        if (elements.reason) {
            elements.reason.value = '';
        }
        if (elements.newRoomSelect) {
            elements.newRoomSelect.value = '';
        }
        updatePriceDiff();

        const occupied = (card && (card.dataset.roomStatus || '').toUpperCase() === 'OCCUPIED');
        if (elements.hint) {
            elements.hint.textContent = occupied
                ? 'Chọn một phòng trống phù hợp. Giá chênh lệch sẽ được xử lý theo quy trình receptionist.'
                : 'Chức năng đổi phòng chỉ khả dụng khi phòng đang có khách.';
        }
        setConfirmEnabled(false);
        openModal();
    };

    window.closeRoomChangeModal = closeModal;

    const closeBtn = document.getElementById('roomChangeCloseBtn');
    const cancelBtn = document.getElementById('roomChangeCancelBtn');
    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);
    backdrop.addEventListener('click', closeModal);

    if (elements.newRoomSelect) {
        elements.newRoomSelect.addEventListener('change', function () {
            // Recompute hint text whenever the user picks a new room.
            updatePriceDiff();
            syncConfirmState();
        });
    }

    if (elements.reason) {
        elements.reason.addEventListener('input', syncConfirmState);
    }

    const form = document.getElementById('roomChangeForm');
    if (form) {
        form.addEventListener('submit', function () {
            // Prevent double submit while the request is in flight.
            if (elements.confirmBtn) {
                elements.confirmBtn.disabled = true;
                elements.confirmBtn.textContent = 'Processing...';
            }
        });
    }

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('is-open')) {
            closeModal();
        }
    });
})();
