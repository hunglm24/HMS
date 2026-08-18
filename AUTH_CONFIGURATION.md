# Cấu hình xác thực HMS

Không ghi credential vào source code. Khai báo các biến sau trong cấu hình chạy Tomcat của IntelliJ (`Run/Debug Configurations` → Smart Tomcat/Tomcat → Environment variables).

## Google Sign-In

```text
HMS_GOOGLE_CLIENT_ID=<Google OAuth Web client ID>
HMS_GOOGLE_CLIENT_SECRET=<Google OAuth client secret>
```

Trong Google Cloud Console, tạo OAuth Client loại **Web application** và thêm Authorized redirect URI:

```text
http://localhost:8080/HMS/auth/google/callback
```

Redirect URI phải trùng chính xác với scheme, host, port và context path đang chạy.

## Email đặt lại mật khẩu

Mặc định code dùng Gmail SMTP SSL (`smtp.gmail.com:465`):

```text
HMS_SMTP_USERNAME=your-account@gmail.com
HMS_SMTP_PASSWORD=<Google App Password>
HMS_SMTP_FROM=your-account@gmail.com
```

Tùy chọn khi dùng SMTP khác:

```text
HMS_SMTP_HOST=smtp.example.com
HMS_SMTP_PORT=465
```

`HMS_SMTP_PASSWORD` phải là App Password/token SMTP, không đưa mật khẩu tài khoản Gmail thông thường vào source.

## Bảo mật mật khẩu

- Mật khẩu được băm PBKDF2-HMAC-SHA256 với salt ngẫu nhiên và 120.000 vòng.
- Email quên mật khẩu chứa link dùng một lần, hết hạn sau 15 phút.
- Database chỉ lưu SHA-256 của reset token, không lưu token rõ.
- Mỗi yêu cầu mới vô hiệu hóa reset token cũ của tài khoản.

## VNPay Sandbox

Khai báo trong Environment variables của cấu hình chạy Tomcat:

```text
HMS_VNPAY_TMN_CODE=<mã website sandbox do VNPay cấp>
HMS_VNPAY_HASH_SECRET=<chuỗi bí mật sandbox do VNPay cấp>
```

Sau khi khách xác nhận đặt phòng, hệ thống chuyển đến cổng VNPay để hiển thị phương thức QR.
