<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><c:out value="${roomPageTitle}" /></title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260824-2" />
    <link rel="stylesheet" href="${cp}/assets/css/rooms.css?v=20260824-2" />
    <link rel="stylesheet" href="${cp}/assets/css/room-form.css?v=20260824-2" />
    <link rel="stylesheet" href="${cp}/assets/css/room-equipment.css?v=20260824-2" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
  </head>
  <body class="room-management-body room-form-body" data-context-path="${cp}">
    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="page-container room-form-page">
      <form class="room-form" action="${cp}${roomFormAction}" method="post" novalidate>
        <c:if test="${isEditMode}">
          <input type="hidden" name="id" value="${roomId}" />
        </c:if>

        <section class="room-form-hero panel">
          <div class="room-form-hero__copy">
            <a class="room-form-back" href="${cp}${roomBackUrl}">Quay lại danh sách phòng</a>
            <p class="room-management-kicker">Quản lý phòng</p>
            <h1><c:out value="${roomPageHeading}" /></h1>
            <p><c:out value="${roomPageSubtitle}" /></p>
          </div>
          <div class="room-form-hero__actions">
            <a class="btn btn-secondary" href="${cp}${roomBackUrl}">Hủy</a>
            <button class="btn" type="submit"><c:out value="${roomSubmitLabel}" /></button>
          </div>
        </section>

        <section class="room-form-layout">
          <article class="panel room-form-card">
            <header class="room-form-card__head">
              <div>
                <h2>Thông tin phòng</h2>
                <p>Các trường thông tin cốt lõi của phòng.</p>
              </div>
            </header>

            <div class="room-form-grid-2">
              <label class="room-form-field${not empty errors.roomNumber ? ' is-error' : ''}">
                <span>Số phòng *</span>
                <input name="roomNumber" type="text" value="<c:out value='${form.roomNumber}' />" maxlength="20" required placeholder="101" />
                <c:if test="${not empty errors.roomNumber}">
                  <div class="room-form-field__error"><c:out value="${errors.roomNumber}" /></div>
                </c:if>
              </label>

              <label class="room-form-field${not empty errors.floorNumber ? ' is-error' : ''}">
                <span>Tầng</span>
                <input name="floorNumber" type="number" min="1" max="4" step="1" value="${form.floorNumber}" placeholder="1" />
                <c:if test="${not empty errors.floorNumber}">
                  <div class="room-form-field__error"><c:out value="${errors.floorNumber}" /></div>
                </c:if>
              </label>
            </div>

            <div class="room-form-grid-2">
              <label class="room-form-field${not empty errors.roomTypeId ? ' is-error' : ''}">
                <span>Loại phòng *</span>
                <select name="roomTypeId" required>
                  <option value="">Chọn loại phòng</option>
                  <c:forEach var="option" items="${roomTypeOptions}">
                    <option value="${option.id}" ${form.roomTypeId eq option.id ? 'selected' : ''}>
                      <c:out value="${option.name}" />
                    </option>
                  </c:forEach>
                </select>
                <c:if test="${not empty errors.roomTypeId}">
                  <div class="room-form-field__error"><c:out value="${errors.roomTypeId}" /></div>
                </c:if>
              </label>

              <label class="room-form-field${not empty errors.status ? ' is-error' : ''}">
                <span>Trạng thái *</span>
                <c:choose>
                  <c:when test="${form.status eq 'INACTIVE'}">
                    <input type="hidden" name="status" value="INACTIVE" />
                    <select disabled>
                      <option value="AVAILABLE">Trống</option>
                      <option value="CLEANING">Đang dọn</option>
                      <option value="MAINTENANCE">Bảo trì</option>
                      <option value="INSPECTION">Chờ kiểm tra</option>
                      <option value="OCCUPIED">Đang có khách</option>
                      <option value="NOT_READY">Chưa sẵn sàng</option>
                      <option value="INACTIVE" selected>Ngừng khai thác</option>
                    </select>
                  </c:when>
                  <c:otherwise>
                    <select name="status" required>
                      <option value="AVAILABLE" ${form.status eq 'AVAILABLE' or empty form.status ? 'selected' : ''}>Trống</option>
                      <option value="CLEANING" ${form.status eq 'CLEANING' ? 'selected' : ''}>Đang dọn</option>
                      <option value="MAINTENANCE" ${form.status eq 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                      <option value="INSPECTION" ${form.status eq 'INSPECTION' ? 'selected' : ''}>Chờ kiểm tra</option>
                      <option value="OCCUPIED" ${form.status eq 'OCCUPIED' ? 'selected' : ''}>Đang có khách</option>
                      <option value="NOT_READY" ${form.status eq 'NOT_READY' ? 'selected' : ''}>Chưa sẵn sàng</option>
                    </select>
                  </c:otherwise>
                </c:choose>
                <c:if test="${not empty errors.status}">
                  <div class="room-form-field__error"><c:out value="${errors.status}" /></div>
                </c:if>
              </label>
            </div>

            <label class="room-form-field${not empty errors.description ? ' is-error' : ''}">
              <span>Mô tả</span>
              <textarea name="description" rows="5" maxlength="500" placeholder="Ghi chú tùy chọn"><c:out value="${form.description}" /></textarea>
              <c:if test="${not empty errors.description}">
                <div class="room-form-field__error"><c:out value="${errors.description}" /></div>
              </c:if>
            </label>

            <div class="room-form-summary">
              <div>
                <strong>Thiết bị của phòng</strong>
                <span><c:out value="${roomEquipmentCount}" /> thiết bị đã gán</span>
              </div>
              <div>
                <strong>Lưu ý</strong>
                <span>Lưu phòng và thiết bị cùng lúc từ trang này.</span>
              </div>
            </div>

            <div class="room-form-quick-actions">
              <div class="room-form-quick-actions__copy">
                <strong>Sao chép thiết bị từ phòng khác</strong>
                <p>Chọn một phòng nguồn để lấy toàn bộ số lượng, trạng thái và ghi chú thiết bị.</p>
                <div class="room-form-quick-actions__source">
                  <select data-room-copy-source>
                    <option value="">Chọn phòng nguồn</option>
                    <c:forEach var="roomOption" items="${roomOptions}">
                      <option value="${roomOption.id}">
                        <c:out value="${roomOption.roomNumber}" /> - <c:out value="${roomOption.roomTypeName}" />
                        <c:if test="${not empty roomOption.floorNumber}">
                          (Tầng <c:out value="${roomOption.floorNumber}" />)
                        </c:if>
                      </option>
                    </c:forEach>
                  </select>
                  <button type="button" class="btn btn-warning" data-room-quick-action="copy-equipment">Sao chép thiết lập</button>
                </div>
              </div>
              <div class="room-form-quick-actions__actions">
                <button type="button" class="btn btn-secondary" data-room-quick-action="reset">Xóa form</button>
              </div>
            </div>
          </article>

          <aside class="room-form-sidebar">
            <article class="panel room-form-card room-equipment-panel">
              <header class="room-form-card__head">
                <div>
                  <h2>Gán thiết bị</h2>
                  <p>Thêm thiết bị khi đang tạo hoặc sửa phòng.</p>
                </div>
              </header>

              <div class="room-equipment-toolbar">
                <div class="room-form-field">
                  <span>Danh mục thiết bị</span>
                  <div class="room-form-field__hint">Chọn thiết bị từ danh sách bên dưới.</div>
                </div>
              </div>

              <section class="room-equipment-block">
                <div class="room-equipment-block__head">
                  <div>
                    <h3>Thiết bị đã chọn</h3>
                    <p>Các dòng này sẽ được lưu cùng phòng.</p>
                  </div>
                </div>
                <div class="room-management-table-wrap" data-pagination-root data-pagination-key="room-equipment-selected" data-pagination-size="5">
                  <table class="room-management-table room-equipment-table">
                    <thead>
                      <tr>
                        <th>Thiết bị</th>
                        <th>Số lượng</th>
                        <th>Trạng thái</th>
                        <th>Ghi chú</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody data-room-equipment-selected-body>
                      <c:choose>
                        <c:when test="${empty roomEquipments}">
                          <tr class="room-equipment-empty-row" data-pagination-item>
                            <td colspan="5">
                              <div class="room-equipment-empty">
                                <strong>Chưa có thiết bị nào được gán.</strong>
                                <span>Hãy chọn thiết bị từ danh mục bên dưới.</span>
                              </div>
                            </td>
                          </tr>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="equip" items="${roomEquipments}">
                            <tr data-pagination-item data-room-equipment-row data-equipment-id="${equip.equipmentId}">
                              <td>
                                <div class="room-equipment-name">
                                  <strong><c:out value="${equip.equipmentName}" /></strong>
                                  <small>#<c:out value="${equip.equipmentId}" /></small>
                                </div>
                                <input type="hidden" name="equipmentId" value="${equip.equipmentId}" />
                              </td>
                              <td>
                                <input type="number" name="equipmentQuantity" min="1" required value="${equip.quantity}" />
                              </td>
                              <td>
                                <select name="equipmentStatus" class="room-equipment-status-select" required>
                                  <c:forEach var="status" items="${roomEquipmentStatuses}">
                                    <option value="${status}" ${equip.status eq status ? 'selected' : ''}>
                                      <c:choose>
                                        <c:when test="${status eq 'NORMAL'}">Bình thường</c:when>
                                        <c:when test="${status eq 'DAMAGED'}">Hư hỏng</c:when>
                                        <c:when test="${status eq 'MISSING'}">Thiếu / thất lạc</c:when>
                                        <c:when test="${status eq 'WAITING_REPAIR'}">Chờ sửa chữa</c:when>
                                        <c:when test="${status eq 'WAITING_REPLACEMENT'}">Chờ thay thế</c:when>
                                        <c:otherwise>Bảo trì</c:otherwise>
                                      </c:choose>
                                    </option>
                                  </c:forEach>
                                </select>
                              </td>
                              <td>
                                <textarea name="equipmentNote" rows="2" maxlength="500"><c:out value="${equip.note}" /></textarea>
                              </td>
                              <td class="room-equipment-row__actions">
                                <button type="button" class="btn btn-secondary btn-sm" data-room-equipment-remove>Xóa</button>
                              </td>
                            </tr>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
                <div class="room-management-pagination" data-pagination-controls data-pagination-target="room-equipment-selected"></div>
              </section>

              <section class="room-equipment-block">
                <div class="room-equipment-block__head">
                  <div>
                    <h3>Danh mục thiết bị</h3>
                    <p>Chọn các thiết bị đang hoạt động từ danh sách.</p>
                  </div>
                </div>
                <div class="room-management-table-wrap" data-pagination-root data-pagination-key="room-equipment-catalog" data-pagination-size="5">
                  <table class="room-management-table room-equipment-table room-equipment-table--catalog">
                    <thead>
                      <tr>
                        <th>Thiết bị</th>
                        <th>Trạng thái</th>
                        <th>Mô tả</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody data-room-equipment-catalog-body>
                      <c:choose>
                        <c:when test="${empty equipmentCatalog}">
                          <tr data-pagination-item>
                            <td colspan="4">
                              <div class="room-equipment-empty">
                                <strong>Chưa có thiết bị đang hoạt động.</strong>
                                <span>Hãy tạo thiết bị trước rồi quay lại đây.</span>
                              </div>
                            </td>
                          </tr>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="equipment" items="${equipmentCatalog}">
                            <tr data-pagination-item data-search-text="${fn:escapeXml(equipment.name)} ${fn:escapeXml(equipment.description)} ${equipment.id}">
                              <td>
                                <div class="room-equipment-name">
                                  <strong><c:out value="${equipment.name}" /></strong>
                                  <small>#<c:out value="${equipment.id}" /></small>
                                </div>
                              </td>
                              <td>
                                <span class="status-chip status-available">Đang hoạt động</span>
                              </td>
                              <td class="room-equipment-description">
                                <c:out value="${empty equipment.description ? '-' : equipment.description}" />
                              </td>
                              <td class="room-equipment-row__actions">
                                <button
                                  type="button"
                                  class="btn btn-secondary btn-sm"
                                  data-room-equipment-add="true"
                                  data-equipment-id="${equipment.id}"
                                  data-equipment-name="${fn:escapeXml(equipment.name)}">
                                  Thêm
                                </button>
                              </td>
                            </tr>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
                <div class="room-management-pagination" data-pagination-controls data-pagination-target="room-equipment-catalog"></div>
              </section>
            </article>
          </aside>
        </section>
      </form>
    </main>

    <script src="${cp}/assets/js/pagination.js?v=20260824-2"></script>
    <script src="${cp}/assets/js/room-form-equipment.js?v=20260824-2"></script>
    <script src="${cp}/assets/js/room-form-core.js?v=20260824-2"></script>
  </body>
</html>
