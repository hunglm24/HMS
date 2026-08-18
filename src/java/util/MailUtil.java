package util;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class MailUtil {
    private MailUtil() {}

    public static void sendPasswordReset(String recipient, String resetUrl) throws IOException {
        String subject = "Đặt lại mật khẩu HMS";
        String body = "Bạn đã yêu cầu đặt lại mật khẩu HMS.\r\n\r\n"
                + "Mở liên kết sau trong vòng 15 phút:\r\n" + resetUrl
                + "\r\n\r\nNếu bạn không yêu cầu, hãy bỏ qua email này.";
        sendEmail(recipient, subject, body, false);
    }

    public static void sendHtmlEmail(String recipient, String subjectStr, String bodyHtml) throws IOException {
        sendEmail(recipient, subjectStr, bodyHtml, true);
    }

    private static void sendEmail(String recipient, String subjectStr, String bodyStr, boolean isHtml) throws IOException {
        String host = env("HMS_SMTP_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(env("HMS_SMTP_PORT", "465"));
        String username = required("HMS_SMTP_USERNAME");
        String password = required("HMS_SMTP_PASSWORD");
        String from = env("HMS_SMTP_FROM", username);

        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {
            expect(reader, 220);
            command(writer, reader, "EHLO localhost", 250);
            command(writer, reader, "AUTH LOGIN", 334);
            command(writer, reader, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)), 334);
            command(writer, reader, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)), 235);
            command(writer, reader, "MAIL FROM:<" + from + ">", 250);
            command(writer, reader, "RCPT TO:<" + recipient + ">", 250);
            command(writer, reader, "DATA", 354);
            String subject = Base64.getEncoder().encodeToString(subjectStr.getBytes(StandardCharsets.UTF_8));
            String contentType = isHtml ? "text/html" : "text/plain";
            writer.write("From: HMS <" + from + ">\r\nTo: " + recipient
                    + "\r\nSubject: =?UTF-8?B?" + subject + "?=\r\n"
                    + "MIME-Version: 1.0\r\nContent-Type: " + contentType + "; charset=UTF-8\r\n"
                    + "Content-Transfer-Encoding: base64\r\n\r\n"
                    + Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII))
                    .encodeToString(bodyStr.getBytes(StandardCharsets.UTF_8)) + "\r\n.\r\n");
            writer.flush();
            expect(reader, 250);
            command(writer, reader, "QUIT", 221);
        }
    }

    private static void command(BufferedWriter writer, BufferedReader reader, String value, int code) throws IOException {
        writer.write(value + "\r\n"); writer.flush(); expect(reader, code);
    }

    private static void expect(BufferedReader reader, int expected) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null || line.length() < 3) throw new IOException("SMTP đóng kết nối");
            int code = Integer.parseInt(line.substring(0, 3));
            if (code != expected) throw new IOException("SMTP trả về mã " + code);
        } while (line.length() > 3 && line.charAt(3) == '-');
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Thiếu biến môi trường " + name);
        return value;
    }
    private static String env(String name, String fallback) {
        String value = System.getenv(name); return value == null || value.isBlank() ? fallback : value;
    }
}
