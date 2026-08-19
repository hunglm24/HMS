<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.HousekeepingTask.EquipmentCheck" %>
<%@ page import="model.HousekeepingTask" %>
<%@ page import="java.util.Map" %>
<%
    List<EquipmentCheck> equips = (List<EquipmentCheck>) request.getAttribute("equips");
    Map<String, String> reportableStatuses = (Map<String, String>) request.getAttribute("reportableStatuses");
    
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
                    <select name="status_<%= eqId %>" style="width: 160px; padding: 6px; font-size: 13px; border: 1px solid #d1d5db; border-radius: 4px; background-color: #fff; color: #111827; cursor: pointer;">
                        <% if (reportableStatuses != null) {
                            for (Map.Entry<String, String> entry : reportableStatuses.entrySet()) { 
                                String val = entry.getKey();
                                String lbl = entry.getValue();
                        %>
                            <option value="<%= val %>" <%= val.equals(currentStatus) ? "selected" : "" %>><%= lbl %></option>
                        <%  }
                           } 
                        %>
                    </select>
                <% } %>
            </div>
        </div>
<%
        }
    }
%>
