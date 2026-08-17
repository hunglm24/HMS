<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.HousekeepingTask.EquipmentCheck" %>
<%@ page import="model.HousekeepingTask" %>
<%
    List<EquipmentCheck> equips = (List<EquipmentCheck>) request.getAttribute("equips");
    if (equips == null || equips.isEmpty()) {
%>
    <div style="padding: 12px; color: var(--color-text-secondary);">Không có thiết bị.</div>
<%
    } else {
        for (EquipmentCheck eq : equips) {
            String statusClass = "status-" + (eq.getCurrentStatus() != null ? eq.getCurrentStatus().toLowerCase() : "");
            boolean isWaiting = "WAITING_REPAIR".equals(eq.getCurrentStatus()) || "WAITING_REPLACEMENT".equals(eq.getCurrentStatus());
            String eqName = HousekeepingTask.esc(eq.getEquipmentName());
            String eqStatusLabel = eq.getCurrentStatusLabel();
            long eqId = eq.getRoomEquipmentId();
            String currentStatus = eq.getCurrentStatus();
%>
        <div class="equipment-list-item" style="display: flex; align-items: center; justify-content: space-between; padding: 12px; border-bottom: 1px solid var(--color-border);">
            <input type="hidden" name="roomEquipmentIds" value="<%= eqId %>">
            <input type="hidden" name="currentStatus_<%= eqId %>" value="<%= currentStatus %>">
            
            <div class="eq-info" style="flex: 1;">
                <span class="eq-name" style="font-weight: 600; display: block; margin-bottom: 4px;"><%= eqName %></span>
                <span class="eq-status <%= statusClass %>"><%= eqStatusLabel %></span>
            </div>
            
            <div class="eq-action">
                <% if (isWaiting) { %>
                    <span style="font-size: 13px; color: var(--color-text-secondary);">Đã báo cáo sự cố</span>
                <% } else { %>
                    <select name="status_<%= eqId %>" class="form-control" style="width: 160px; padding: 6px; font-size: 13px;">
                        <option value="NORMAL" <%= "NORMAL".equals(currentStatus) ? "selected" : "" %>>Bình thường</option>
                        <option value="DAMAGED" <%= "DAMAGED".equals(currentStatus) ? "selected" : "" %>>Hư hỏng</option>
                        <option value="MISSING" <%= "MISSING".equals(currentStatus) ? "selected" : "" %>>Thất lạc</option>
                        <option value="MAINTENANCE" <%= "MAINTENANCE".equals(currentStatus) ? "selected" : "" %>>Bảo trì định kỳ</option>
                    </select>
                <% } %>
            </div>
        </div>
<%
        }
    }
%>
