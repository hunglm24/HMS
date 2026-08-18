package service;

import model.Booking;
import model.Invoice;
import model.InvoiceItem;
import util.MailUtil;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.math.BigDecimal;
import dao.InvoiceItemDao;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class EmailService {
    private final InvoiceItemDao invoiceItemDao = new InvoiceItemDao();
    private static final DecimalFormat df = new DecimalFormat("#,###.##");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public void sendBookingConfirmationAsync(Booking booking, String recipientEmail, String customerName) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                String subject = "Xác nhận đặt phòng thành công - HMS";
                String checkIn = booking.getCheckInDatetime() != null ? sdf.format(booking.getCheckInDatetime()) : sdf.format(booking.getCheckInDate());
                String checkOut = booking.getCheckOutDatetime() != null ? sdf.format(booking.getCheckOutDatetime()) : sdf.format(booking.getCheckOutDate());
                
                String body = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                        + "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;'>"
                        + "<div style='background-color: #007bff; color: #fff; padding: 20px; text-align: center;'>"
                        + "<h2>Xác nhận đặt phòng</h2>"
                        + "</div>"
                        + "<div style='padding: 20px;'>"
                        + "<p>Xin chào <strong>" + (customerName != null ? customerName : "Quý khách") + "</strong>,</p>"
                        + "<p>Cảm ơn bạn đã lựa chọn dịch vụ của HMS. Đặt phòng của bạn đã được xác nhận thành công. Dưới đây là thông tin chi tiết:</p>"
                        + "<table style='width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 15px;'>"
                        + "<tr><td style='padding: 8px; border: 1px solid #ddd; font-weight: bold;'>Mã đặt phòng:</td><td style='padding: 8px; border: 1px solid #ddd;'>" + booking.getBookingCode() + "</td></tr>"
                        + "<tr><td style='padding: 8px; border: 1px solid #ddd; font-weight: bold;'>Ngày nhận phòng:</td><td style='padding: 8px; border: 1px solid #ddd;'>" + checkIn + "</td></tr>"
                        + "<tr><td style='padding: 8px; border: 1px solid #ddd; font-weight: bold;'>Ngày trả phòng:</td><td style='padding: 8px; border: 1px solid #ddd;'>" + checkOut + "</td></tr>"
                        + "<tr><td style='padding: 8px; border: 1px solid #ddd; font-weight: bold;'>Tổng tiền:</td><td style='padding: 8px; border: 1px solid #ddd; color: #d9534f; font-weight: bold;'>" + df.format(booking.getTotalAmount()) + " VND</td></tr>"
                        + "</table>"
                        + "<p>Vui lòng mang theo giấy tờ tùy thân (CMND/CCCD/Hộ chiếu) khi đến nhận phòng.</p>"
                        + "<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                        + "</div>"
                        + "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777;'>"
                        + "<p>&copy; 2026 HMS. All rights reserved.</p>"
                        + "</div>"
                        + "</div></body></html>";
                
                MailUtil.sendHtmlEmail(recipientEmail, subject, body);
                System.out.println("Booking confirmation email sent to " + recipientEmail);
            } catch (Exception e) {
                System.err.println("Failed to send booking confirmation email to " + recipientEmail);
                e.printStackTrace();
            }
        });
    }

    public void sendInvoiceAsync(Invoice invoice, Booking booking, String recipientEmail, String customerName) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            try {
                String subject = "Hóa đơn thanh toán - HMS";
                List<InvoiceItem> items = invoiceItemDao.findByInvoiceId(invoice.getId());
                
                StringBuilder itemsHtml = new StringBuilder();
                for (InvoiceItem item : items) {
                    itemsHtml.append("<tr>")
                            .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getDescription() != null ? item.getDescription() : item.getItemType()).append("</td>")
                            .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(item.getQuantity()).append("</td>")
                            .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(df.format(item.getUnitPrice())).append("</td>")
                            .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(df.format(item.getTotalPrice())).append("</td>")
                            .append("</tr>");
                }

                String body = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
                        + "<div style='max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;'>"
                        + "<div style='background-color: #28a745; color: #fff; padding: 20px; text-align: center;'>"
                        + "<h2>Hóa đơn thanh toán</h2>"
                        + "</div>"
                        + "<div style='padding: 20px;'>"
                        + "<p>Xin chào <strong>" + (customerName != null ? customerName : "Quý khách") + "</strong>,</p>"
                        + "<p>Cảm ơn bạn đã sử dụng dịch vụ của HMS. Dưới đây là chi tiết hóa đơn thanh toán của bạn:</p>"
                        + "<p><strong>Mã hóa đơn:</strong> " + invoice.getInvoiceCode() + "<br/>"
                        + "<strong>Mã đặt phòng:</strong> " + booking.getBookingCode() + "<br/>"
                        + "<strong>Ngày lập hóa đơn:</strong> " + (invoice.getCreatedAt() != null ? sdf.format(invoice.getCreatedAt()) : sdf.format(new java.util.Date())) + "</p>"
                        + "<table style='width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 15px;'>"
                        + "<thead style='background-color: #f8f9fa;'>"
                        + "<tr><th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Hạng mục</th>"
                        + "<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Số lượng</th>"
                        + "<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Đơn giá (VND)</th>"
                        + "<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Thành tiền (VND)</th></tr>"
                        + "</thead>"
                        + "<tbody>"
                        + itemsHtml.toString()
                        + "</tbody>"
                        + "<tfoot>"
                        + "<tr><td colspan='3' style='padding: 8px; border: 1px solid #ddd; text-align: right; font-weight: bold;'>Tổng tiền dịch vụ/phòng:</td><td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>" + df.format(invoice.getRoomAmount().add(invoice.getServiceAmount()).add(invoice.getDamageAmount())) + "</td></tr>"
                        + "<tr><td colspan='3' style='padding: 8px; border: 1px solid #ddd; text-align: right; font-weight: bold;'>Thuế/Phí:</td><td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>" + df.format(invoice.getTaxAmount()) + "</td></tr>"
                        + "<tr><td colspan='3' style='padding: 8px; border: 1px solid #ddd; text-align: right; font-weight: bold;'>Chiết khấu:</td><td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>-" + df.format(invoice.getDiscountAmount()) + "</td></tr>"
                        + "<tr><td colspan='3' style='padding: 8px; border: 1px solid #ddd; text-align: right; font-weight: bold; font-size: 16px;'>TỔNG THANH TOÁN:</td><td style='padding: 8px; border: 1px solid #ddd; text-align: right; font-weight: bold; color: #d9534f; font-size: 16px;'>" + df.format(invoice.getTotalAmount()) + " VND</td></tr>"
                        + "</tfoot>"
                        + "</table>"
                        + "<p style='text-align: center; font-size: 18px; font-weight: bold; color: #28a745;'>TRẠNG THÁI: ĐÃ THANH TOÁN</p>"
                        + "</div>"
                        + "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777;'>"
                        + "<p>Hẹn gặp lại quý khách lần sau!</p>"
                        + "<p>&copy; 2026 HMS. All rights reserved.</p>"
                        + "</div>"
                        + "</div></body></html>";

                MailUtil.sendHtmlEmail(recipientEmail, subject, body);
                System.out.println("Invoice email sent to " + recipientEmail);
            } catch (Exception e) {
                System.err.println("Failed to send invoice email to " + recipientEmail);
                e.printStackTrace();
            }
        });
    }
}
