<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="cp" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Cấu hình khách sạn | HMS</title>
    <link rel="stylesheet" href="${cp}/assets/css/main.css?v=20260821-1" />
    <style>
      .manager-content {
        min-width: 0;
        background: #f6f8fb;
        min-height: calc(100vh - 80px);
        display: flex;
        align-items: flex-start;
        padding: 24px;
      }
      .manager-section {
        background: #fff;
        border: 1px solid #d9e0ea;
        border-radius: 12px;
        padding: 24px;
        width: 100%;
        max-width: 960px;
        margin: 0 auto;
      }
      .section-head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 16px;
      }
      .section-head h1 {
        margin: 0 0 6px;
      }
      .section-head p {
        margin: 0;
        color: #526174;
      }
      .empty-panel {
        margin-top: 20px;
        padding: 28px;
        border: 1px dashed #d9e0ea;
        border-radius: 12px;
        background: #f8fafc;
        display: flex;
        justify-content: center;
      }
      @media (max-width: 700px) {
        .manager-content {
          padding: 16px;
        }
        .section-head {
          align-items: stretch;
          flex-direction: column;
        }
        .empty-panel {
          padding: 20px;
        }
      }
    </style>
  </head>
  <body>
    <jsp:include page="/WEB-INF/views/common/header.jsp" />
    <main class="manager-content">
      <section class="manager-section">
        <div class="section-head">
          <div>
            <p class="section-kicker">Manager</p>
            <h1>Cấu hình khách sạn</h1>
            <p>Quản lý các cấu hình khách sạn nội bộ.</p>
          </div>
          <a class="btn" href="${cp}/manager/hotel-configs/create"
            >Tạo cấu hình</a
          >
        </div>
        <div class="empty-panel" aria-hidden="true"></div>
      </section>
    </main>
  </body>
</html>
