(function () {
    const form = document.getElementById('editBookingForm');
    const idInput = document.querySelector('input[name="id"]');
    const checkInInput = document.getElementById('checkInDate');
    const checkOutInput = document.getElementById('checkOutDate');
    const roomTypeSelect = document.getElementById('roomTypeId');
    const refreshBtn = document.getElementById('refreshRoomPickerBtn');
    const saveBtn = document.getElementById('saveEditBookingBtn');
    const addRoomBtn = document.querySelector('.js-add-room-card');
    const bookingList = document.querySelector('.edit-booking-booking-list');
    const activeKeyInput = document.getElementById('activeBookingRoomKey');
    const roomCountLabel = document.querySelector('.edit-booking-room-count strong');
    const pickerCards = Array.from(document.querySelectorAll('.js-room-picker-card'));

    if (!form || !idInput || !checkInInput || !checkOutInput || !roomTypeSelect || !bookingList) {
        return;
    }

    const bookingId = idInput.value;

    function getBookingCards() {
        return Array.from(document.querySelectorAll('.js-booking-room-card'));
    }

    function getCardKey(card) {
        return card ? (card.dataset.bookingRoomKey || '') : '';
    }

    function getActiveCard() {
        const cards = getBookingCards();
        return cards.find((card) => card.classList.contains('is-active')) || cards[0] || null;
    }

    function getAssignmentInput(card) {
        if (!card) {
            return null;
        }
        const existing = card.querySelector('input[type="hidden"][name^="assignedRoom_"]');
        if (existing) {
            return existing;
        }
        return card.querySelector('input[type="hidden"][name^="newAssignedRoom_"]');
    }

    function getAssignedRoomId(card) {
        const input = getAssignmentInput(card);
        return input ? (input.value || '') : '';
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('vi-VN').format(Number(value || 0)) + ' đ';
    }

    function syncPickerHighlight() {
        const activeCard = getActiveCard();
        const selectedRoomId = getAssignedRoomId(activeCard);

        pickerCards.forEach((card) => {
            card.classList.toggle('is-selected', card.dataset.roomId === selectedRoomId);
        });
    }

    function setActiveCard(card) {
        if (!card) {
            return;
        }
        getBookingCards().forEach((item) => item.classList.toggle('is-active', item === card));
        if (activeKeyInput) {
            activeKeyInput.value = getCardKey(card);
        }
        syncPickerHighlight();
    }

    function syncCardSummary(card) {
        if (!card) {
            return;
        }

        const assignedRoomId = getAssignedRoomId(card);
        const label = card.querySelector('.js-assigned-room-label');
        const meta = card.querySelector('.js-assigned-room-meta');
        if (!label || !meta) {
            return;
        }

        if (!assignedRoomId) {
            label.textContent = 'Chưa chọn';
            meta.textContent = 'Bấm phòng trống bên phải để gán';
            return;
        }

        const roomCard = pickerCards.find((item) => item.dataset.roomId === assignedRoomId);
        if (!roomCard) {
            label.textContent = 'Chưa chọn';
            meta.textContent = 'Bấm phòng trống bên phải để gán';
            return;
        }

        label.textContent = 'Phòng ' + (roomCard.dataset.roomNumber || '--');
        meta.textContent = (roomCard.dataset.roomTypeName || '--') + ' - ' + formatCurrency(roomCard.dataset.roomPrice);
    }

    function syncAllSummaries() {
        getBookingCards().forEach((card) => syncCardSummary(card));
    }

    function syncRoomCount() {
        if (roomCountLabel) {
            roomCountLabel.textContent = String(getBookingCards().length);
        }
    }

    function assignRoom(roomCard) {
        const activeCard = getActiveCard();
        if (!activeCard) {
            return;
        }

        const targetRoomId = roomCard.dataset.roomId || '';
        const currentRoomId = getAssignedRoomId(activeCard);
        if (targetRoomId && targetRoomId === currentRoomId) {
            const input = getAssignmentInput(activeCard);
            if (!input) {
                return;
            }

            input.value = '';
            syncCardSummary(activeCard);
            syncPickerHighlight();
            return;
        }

        const alreadyUsed = getBookingCards().some((card) => card !== activeCard && getAssignedRoomId(card) === targetRoomId);
        if (alreadyUsed) {
            alert('Phòng này đã được gán cho một dòng khác trong booking.');
            return;
        }

        const input = getAssignmentInput(activeCard);
        if (!input) {
            return;
        }

        input.value = targetRoomId;
        syncCardSummary(activeCard);
        syncPickerHighlight();
    }

    function nextTempSlotId() {
        const existing = getBookingCards()
            .map((card) => card.dataset.bookingRoomKey || '')
            .filter((key) => key.startsWith('new-'))
            .map((key) => Number(key.replace('new-', '')))
            .filter((num) => Number.isFinite(num));

        return (existing.length ? Math.max.apply(null, existing) : 0) + 1;
    }

    function createNewRoomCard() {
        const tempId = nextTempSlotId();
        const key = 'new-' + tempId;

        const card = document.createElement('article');
        card.className = 'edit-booking-booking-card edit-booking-booking-card--new js-booking-room-card is-active';
        card.dataset.bookingRoomKey = key;
        card.innerHTML = [
            '<input type="hidden" name="newAssignedRoom_' + tempId + '" id="newAssignedRoom_' + tempId + '" value="">',
            '<div class="edit-booking-booking-card__top">',
            '  <div class="edit-booking-booking-chip">Phòng mới</div>',
            '  <div class="edit-booking-booking-main">',
            '    <strong>Thêm phòng</strong>',
            '    <span>Slot mới trong booking</span>',
            '  </div>',
            '  <div class="edit-booking-booking-status">Chưa gán</div>',
            '</div>',
            '<div class="edit-booking-booking-card__bottom">',
            '  <div>',
            '    <label>Phòng gán hiện tại</label>',
            '    <strong class="js-assigned-room-label">Chưa chọn</strong>',
            '    <small class="js-assigned-room-meta">Bấm phòng trống bên phải để gán</small>',
            '  </div>',
            '  <div>',
            '    <label>Tiền phòng</label>',
            '    <strong>0 đ</strong>',
            '  </div>',
            '</div>'
        ].join('');

        card.addEventListener('click', () => setActiveCard(card));

        const addButton = document.querySelector('.js-add-room-card');
        if (addButton) {
            bookingList.insertBefore(card, addButton);
        } else {
            bookingList.appendChild(card);
        }

        setActiveCard(card);
        syncCardSummary(card);
        if (activeKeyInput) {
            activeKeyInput.value = key;
        }
        syncRoomCount();
    }

    function refreshPicker() {
        const url = new URL(window.location.href);
        url.searchParams.set('id', bookingId);
        url.searchParams.set('checkInDate', checkInInput.value);
        url.searchParams.set('checkOutDate', checkOutInput.value);

        if (roomTypeSelect.value) {
            url.searchParams.set('roomTypeId', roomTypeSelect.value);
        } else {
            url.searchParams.delete('roomTypeId');
        }

        getBookingCards().forEach((card) => {
            const input = getAssignmentInput(card);
            if (input) {
                url.searchParams.set(input.name, input.value || '');
            }
        });

        const activeCard = getActiveCard();
        if (activeCard) {
            url.searchParams.set('activeBookingRoomKey', getCardKey(activeCard));
        }

        window.location.href = url.toString();
    }

    getBookingCards().forEach((card) => {
        card.addEventListener('click', () => setActiveCard(card));
    });

    pickerCards.forEach((card) => {
        card.addEventListener('click', () => assignRoom(card));
    });

    if (addRoomBtn) {
        addRoomBtn.addEventListener('click', createNewRoomCard);
    }

    checkInInput.addEventListener('change', syncPickerHighlight);
    checkOutInput.addEventListener('change', syncPickerHighlight);
    roomTypeSelect.addEventListener('change', syncPickerHighlight);

    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshPicker);
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', (event) => {
            if (!confirm('Bạn có chắc muốn lưu các thay đổi này không?')) {
                event.preventDefault();
            }
        });
    }

    form.addEventListener('submit', function () {
        if (this.dataset.submitted) {
            return false;
        }
        const activeCard = getActiveCard();
        if (activeCard && activeKeyInput) {
            activeKeyInput.value = getCardKey(activeCard);
        }
        this.dataset.submitted = 'true';
        return true;
    });

    syncAllSummaries();
    syncRoomCount();
    if (!getActiveCard() && getBookingCards().length > 0) {
        setActiveCard(getBookingCards()[0]);
    } else {
        syncPickerHighlight();
    }
})();
