<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>VNPay Demo | HMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260819-1">
    <style>
        .vnpay-demo { max-width: 720px; margin: 3rem auto; }
        .vnpay-demo__head { background: #005baa; color: white; padding: 1.25rem 1.5rem; border-radius: 14px 14px 0 0; }
        .vnpay-demo__head strong { font-size: 1.5rem; }
        .vnpay-demo__body { background: white; padding: 1.5rem; border: 1px solid #dbe3ea; border-top: 0; border-radius: 0 0 14px 14px; }
        .vnpay-demo__notice { color: #9a6700; background: #fff8c5; padding: .75rem 1rem; border-radius: 8px; }
        .vnpay-demo__row { display: flex; justify-content: space-between; gap: 1rem; padding: .75rem 0; border-bottom: 1px solid #edf1f4; }
        .vnpay-demo__banks { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .75rem; margin: 1.25rem 0; }
        .vnpay-demo__bank { border: 1px solid #ccd7e0; border-radius: 8px; padding: .8rem; cursor: pointer; }
        .vnpay-demo__actions { display: flex; gap: .75rem; }
    </style>
</head>
<body>
<main class="public-page">
    <section class="vnpay-demo">
        <div class="vnpay-demo__head"><strong>VNPAY</strong><br><small>Cổng thanh toán mô phỏng</small></div>
        <form class="vnpay-demo__body" method="post" action="${pageContext.request.contextPath}/vnpay-demo">
            <p class="vnpay-demo__notice">DEMO LOCAL — không phát sinh giao dịch hoặc trừ tiền thật.</p>
            <div class="vnpay-demo__row"><span>Mã đơn hàng</span><strong><c:out value="${bookingCode}"/></strong></div>
            <div class="vnpay-demo__row"><span>Số tiền</span><strong><fmt:formatNumber value="${amount}" pattern="#,##0"/> ₫</strong></div>
            <h3>Chọn ngân hàng demo</h3>
            <div class="vnpay-demo__banks">
                <label class="vnpay-demo__bank"><input type="radio" name="bankCode" value="NCB" checked> NCB</label>
                <label class="vnpay-demo__bank"><input type="radio" name="bankCode" value="VCB"> Vietcombank</label>
                <label class="vnpay-demo__bank"><input type="radio" name="bankCode" value="TCB"> Techcombank</label>
                <label class="vnpay-demo__bank"><input type="radio" name="bankCode" value="VNPAYQR"> VNPAY QR</label>
            </div>
            <div class="vnpay-demo__actions">
                <button class="btn" type="submit" name="action" value="pay">Thanh toán demo</button>
                <button class="btn btn-secondary" type="submit" name="action" value="cancel">Hủy giao dịch</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
