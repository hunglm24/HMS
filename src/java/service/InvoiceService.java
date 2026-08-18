package service;

import java.math.BigDecimal;
import dao.InvoiceDao;
import dao.InvoiceItemDao;
import dao.PaymentDao;
import model.Invoice;
import model.InvoiceItem;

public class InvoiceService {

    private final InvoiceDao invoiceDao = new InvoiceDao();
    private final InvoiceItemDao invoiceItemDao = new InvoiceItemDao();
    private final PaymentDao paymentDao = new PaymentDao();

    /**
     * Initializes a basic invoice for a booking with room amount.
     * Taxes and other services are skipped.
     */
    public boolean generateBaseInvoice(long bookingId, BigDecimal roomAmount, String invoiceCode) {
        Invoice existing = invoiceDao.findByBookingId(bookingId);
        if (existing != null) {
            return false; // Invoice already exists
        }

        Invoice invoice = new Invoice();
        invoice.setBookingId(bookingId);
        invoice.setInvoiceCode(invoiceCode);
        invoice.setRoomAmount(roomAmount);
        invoice.setServiceAmount(BigDecimal.ZERO);
        invoice.setDamageAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(roomAmount); // Total = Room (ignoring tax/discount for basic model)
        invoice.setStatus("UNPAID");

        boolean created = invoiceDao.createInvoice(invoice);
        if (created) {
            InvoiceItem roomItem = new InvoiceItem();
            roomItem.setInvoiceId(invoice.getId());
            roomItem.setItemType("ROOM");
            roomItem.setDescription("Tiền phòng");
            roomItem.setQuantity(1);
            roomItem.setUnitPrice(roomAmount);
            roomItem.setTotalPrice(roomAmount);
            invoiceItemDao.addInvoiceItem(roomItem);
        }
        return created;
    }

    /**
     * Calculates the exact remaining amount the guest needs to pay based on current invoice and successful payments.
     */
    public BigDecimal calculateRemainingAmount(long bookingId) {
        Invoice invoice = invoiceDao.findByBookingId(bookingId);
        if (invoice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = paymentDao.getTotalPaidAmount(bookingId);
        BigDecimal remaining = invoice.getTotalAmount().subtract(totalPaid);
        return remaining;
    }

    /**
     * Records a damage fee after checkout inspection.
     * Reopens the invoice if it was fully paid.
     */
    public boolean recordDamageFee(long bookingId, Long damageReportId, String description, BigDecimal amount) {
        Invoice invoice = invoiceDao.findByBookingId(bookingId);
        if (invoice == null) {
            return false;
        }

        // Add damage to line items
        InvoiceItem item = new InvoiceItem();
        item.setInvoiceId(invoice.getId());
        item.setDamageReportId(damageReportId);
        item.setItemType("DAMAGE");
        item.setDescription(description);
        item.setQuantity(1);
        item.setUnitPrice(amount);
        item.setTotalPrice(amount);
        
        boolean itemAdded = invoiceItemDao.addInvoiceItem(item);
        if (itemAdded) {
            // Safe SQL update: damage_amount += amount, total_amount += amount
            return invoiceDao.addDamageAmount(bookingId, amount);
        }
        return false;
    }

    /**
     * Verifies if the invoice is fully paid and updates its status.
     * Should be called after any successful payment.
     */
    public void verifyAndUpdateInvoiceStatus(long bookingId) {
        BigDecimal remaining = calculateRemainingAmount(bookingId);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            invoiceDao.updateStatus(bookingId, "PAID");
        } else {
            Invoice invoice = invoiceDao.findByBookingId(bookingId);
            if (invoice != null && !"PARTIALLY_PAID".equals(invoice.getStatus())) {
                invoiceDao.updateStatus(bookingId, "PARTIALLY_PAID");
            }
        }
    }
}
