<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đổi mật khẩu | HMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=20260816-4">
<style>.account-card{max-width:500px;margin:auto;padding:30px;border:1px solid #e3e8f0;border-radius:14px}label{display:block;margin:14px 0 7px;font-weight:600}input{width:100%;padding:12px;border:1px solid #ccd3df;border-radius:9px;font-size:16px}.submit{width:100%;margin-top:22px;padding:13px;border:0;border-radius:9px;background:#245ec7;color:#fff;font-weight:700}.message{padding:11px;border-radius:8px}.error{background:#fff0f0;color:#a51d27}.success{background:#eaf8ef;color:#176438}</style></head>
<body><jsp:include page="/WEB-INF/views/common/header.jsp"/><main class="page-container"><section class="account-card"><h1>Đổi mật khẩu</h1>
<% if(request.getAttribute("error") != null){ %><div class="message error"><%= request.getAttribute("error") %></div><% } %>
<% if(request.getAttribute("success") != null){ %><div class="message success"><%= request.getAttribute("success") %></div><% } %>
<form method="post" action="${pageContext.request.contextPath}/change-password"><label for="currentPassword">Mật khẩu hiện tại</label><input id="currentPassword" name="currentPassword" type="password" required>
<label for="password">Mật khẩu mới</label><input id="password" name="password" type="password" minlength="8" required>
<label for="confirmPassword">Xác nhận mật khẩu mới</label><input id="confirmPassword" name="confirmPassword" type="password" minlength="8" required>
<button class="submit" type="submit">Đổi mật khẩu</button></form></section></main></body></html>
