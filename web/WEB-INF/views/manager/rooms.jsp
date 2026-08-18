<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="activeTab" value="${pageData.activeTab}" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Quản lý loại phòng và phòng | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/rooms.css">
  </head>
  <body class="room-management-body">
    <%-- Shared header also includes the internal sidebar for manager accounts.
    --%>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-management-page">
      <section class="room-management-hero panel">
        <div class="room-management-hero__copy">
          <p class="room-management-kicker">Quản lý khách sạn</p>
          <h1>Quản lý loại phòng và phòng</h1>
          <p>
            Quản lý loại phòng và phòng vật lý trong cùng một màn, nhưng tách
            riêng theo từng tab.
          </p>
        </div>

        <div class="room-management-hero__actions">
          <button
            class="btn"
            type="button"
            data-room-mgmt-open="room-type"
            onclick="
              window.RoomManagement && window.RoomManagement.openRoomTypeModal()
            "
          >
            + Thêm loại phòng
          </button>
          <button
            class="btn btn-secondary"
            type="button"
            data-room-mgmt-open="room"
            onclick="
              window.RoomManagement && window.RoomManagement.openRoomModal()
            "
          >
            + Thêm phòng
          </button>
        </div>
      </section>

      <nav class="room-management-tabs tabs" aria-label="Các tab quản lý phòng">
        <a
          class="tab ${activeTab eq 'room-types' ? 'active' : ''}"
          href="${pageContext.request.contextPath}/manager/rooms?tab=room-types"
          >Loại phòng</a
        >
        <a
          class="tab ${activeTab eq 'rooms' ? 'active' : ''}"
          href="${pageContext.request.contextPath}/manager/rooms?tab=rooms"
          >Phòng</a
        >
      </nav>

      <c:if test="${not empty sessionScope.toastMessage}">
        <div class="toast ${sessionScope.toastType}">
          <c:out value="${sessionScope.toastMessage}" />
        </div>
        <c:remove var="toastMessage" scope="session" />
        <c:remove var="toastType" scope="session" />
      </c:if>

      <section class="room-management-content">
        <c:choose>
          <c:when test="${activeTab eq 'rooms'}">
            <jsp:include
              page="/WEB-INF/views/manager/fragments/rooms-tab.jsp"
            />
          </c:when>
          <c:otherwise>
            <jsp:include
              page="/WEB-INF/views/manager/fragments/room-types-tab.jsp"
            />
          </c:otherwise>
        </c:choose>
      </section>
    </main>

    <jsp:include page="/WEB-INF/views/manager/modals/room-type-modal.jsp" />
    <jsp:include page="/WEB-INF/views/manager/modals/room-modal.jsp" />

    <script src="${pageContext.request.contextPath}/assets/js/rooms.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/pagination.js"></script>
  </body>
</html>
