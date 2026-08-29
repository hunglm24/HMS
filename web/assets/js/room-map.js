(function () {
    // Wire the room map drawer and its interactions on page load.
    const drawer = document.getElementById('roomDrawer');
    const backdrop = document.getElementById('drawerBackdrop');
    if (!drawer || !backdrop) return;
    let activeRoomCard = null;

    const fields = {
        roomNumber: document.getElementById('drawerRoomNumber'),
        roomType: document.getElementById('drawerRoomType'),
        roomFloor: document.getElementById('drawerRoomFloor'),
        roomStatus: document.getElementById('drawerRoomStatus'),
        roomDescription: document.getElementById('drawerRoomDescription'),
        statusChip: document.getElementById('drawerStatusChip')
    };

    // Map a backend room status to a UI tone class.
    function statusTone(status) {
        switch ((status || '').toUpperCase()) {
            case 'AVAILABLE': return 'available';
            case 'OCCUPIED': return 'occupied';
            case 'CLEANING': return 'cleaning';
            case 'MAINTENANCE': return 'maintenance';
            case 'NOT_READY': return 'not-ready';
            default: return 'neutral';
        }
    }

    // Fill the drawer with data from the clicked room card.
    window.openRoomDrawer = function (card) {
        activeRoomCard = card;
        const roomNumber = card.dataset.roomNumber || '--';
        const roomType = card.dataset.roomType || '--';
        const roomStatus = card.dataset.roomStatusLabel || card.dataset.roomStatus || '--';
        const floor = card.dataset.roomFloor || 'Không rõ';
        const description = card.dataset.roomDescription || 'Không có mô tả';
        const tone = statusTone(card.dataset.roomStatus);

        fields.roomNumber.textContent = roomNumber;
        fields.roomType.textContent = roomType;
        fields.roomFloor.textContent = floor;
        fields.roomStatus.textContent = roomStatus;
        fields.roomDescription.textContent = description;
        fields.statusChip.className = 'drawer-chip ' + tone;
        fields.statusChip.textContent = roomStatus;

        const changeRoomBtn = document.getElementById('changeRoomBtn');
        if (changeRoomBtn) {
            const isOccupied = (card.dataset.roomStatus || '').toUpperCase() === 'OCCUPIED';
            changeRoomBtn.disabled = !isOccupied;
            changeRoomBtn.title = isOccupied
                ? 'Mở modal đổi phòng'
                : 'Chỉ đổi phòng cho phòng đang có khách';
        }

        drawer.classList.add('is-open');
        backdrop.classList.add('is-open');
        drawer.setAttribute('aria-hidden', 'false');
    };

    // Close the room drawer and hide its backdrop.
    window.closeRoomDrawer = function () {
        drawer.classList.remove('is-open');
        backdrop.classList.remove('is-open');
        drawer.setAttribute('aria-hidden', 'true');
    };

    // Open the drawer from the keyboard for accessibility.
    window.handleRoomCardKeydown = function (event, card) {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            window.openRoomDrawer(card);
        }
    };

    const roomCards = document.querySelectorAll('.js-room-card');
    // Attach mouse and keyboard handlers to each room card.
    roomCards.forEach((card) => {
        card.addEventListener('click', () => window.openRoomDrawer(card));
        card.addEventListener('keydown', (event) => window.handleRoomCardKeydown(event, card));
    });

    const drawerCloseBtn = document.getElementById('drawerCloseBtn');
    if (drawerCloseBtn) {
        // Close the drawer from the dedicated button.
        drawerCloseBtn.addEventListener('click', window.closeRoomDrawer);
    }

    const changeRoomBtn = document.getElementById('changeRoomBtn');
    if (changeRoomBtn) {
        // Open the change-room modal only when the current room is occupied.
        changeRoomBtn.addEventListener('click', () => {
            if (activeRoomCard && typeof window.openRoomChangeModal === 'function') {
                window.openRoomChangeModal(activeRoomCard);
            }
        });
    }

    // Clicking the backdrop should close the drawer.
    backdrop.addEventListener('click', window.closeRoomDrawer);

    // Escape should always close the drawer first.
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            window.closeRoomDrawer();
        }
    });
})();
