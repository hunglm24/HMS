package dto;

import model.Room;
import model.RoomType;

import java.util.List;

public class RoomManagementPageData {
    private List<RoomType> roomTypes;
    private List<Room> rooms;
    private String activeTab;
    private String keyword;
    private Long roomTypeId;
    private Integer floor;
    private String roomTypeStatus;
    private String roomStatus;
    private String equipmentFilter;

    // Shared list of room types for both tabs and modal dropdowns.
    public List<RoomType> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(List<RoomType> roomTypes) {
        this.roomTypes = roomTypes;
    }

    // Current room list shown in the Rooms tab.
    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    // Tracks which tab the UI should display as active.
    public String getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(String activeTab) {
        this.activeTab = activeTab;
    }

    // Search keyword shared by the page.
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    // Optional room type filter for the Rooms tab.
    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    // Optional floor filter for the Rooms tab.
    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    // Optional status filter for the Room Types tab.
    public String getRoomTypeStatus() {
        return roomTypeStatus;
    }

    public void setRoomTypeStatus(String roomTypeStatus) {
        this.roomTypeStatus = roomTypeStatus;
    }

    // Optional status filter for the Rooms tab.
    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    // Optional filter to show rooms with or without assigned equipment.
    public String getEquipmentFilter() {
        return equipmentFilter;
    }

    public void setEquipmentFilter(String equipmentFilter) {
        this.equipmentFilter = equipmentFilter;
    }
}
