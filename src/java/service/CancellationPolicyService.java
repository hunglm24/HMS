package service;

import dao.HotelPolicyDao;
import dao.PaymentDao;
import model.Booking;
import model.HotelPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CancellationPolicyService {
    public static final String POLICY_CATEGORY = "Hủy phòng";
    public static final String RULE_MARKER = "--- POLICY_RULE:CANCELLATION_REFUND ---";

    // Regex encodes the refund rule metadata stored at the end of policy content.
    private static final Pattern RULE_PATTERN = Pattern.compile(
            "FULL_DAYS=(\\d+);FULL_RATE=(\\d+);PARTIAL_DAYS=(\\d+);PARTIAL_RATE=(\\d+);SAME_DAY_RATE=(\\d+)");

    private final HotelPolicyDao policyDao;
    private final PaymentDao paymentDao;

    public CancellationPolicyService() {
        this(new HotelPolicyDao(), new PaymentDao());
    }

    public CancellationPolicyService(HotelPolicyDao policyDao) {
        this(policyDao, new PaymentDao());
    }

    public CancellationPolicyService(HotelPolicyDao policyDao, PaymentDao paymentDao) {
        this.policyDao = policyDao;
        this.paymentDao = paymentDao;
    }

    public RefundResult calculateRefund(Booking booking, LocalDate cancelDate) throws SQLException {
        BigDecimal paidAmount = resolvePaidAmount(booking);
        long daysUntilCheckIn = ChronoUnit.DAYS.between(cancelDate, booking.getCheckInDate().toLocalDate());
        CancellationRule rule = loadActiveRule().orElse(CancellationRule.defaultRule());
        BigDecimal refundRate = rule.rateFor(daysUntilCheckIn);
        BigDecimal refundAmount = paidAmount.multiply(refundRate)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal cancellationFee = paidAmount.subtract(refundAmount).max(BigDecimal.ZERO);
        return new RefundResult(daysUntilCheckIn, refundRate, refundAmount, cancellationFee, rule.fromPolicy);
    }

    private BigDecimal resolvePaidAmount(Booking booking) {
        BigDecimal paidAmount = booking.getId() == null
                ? BigDecimal.ZERO
                : paymentDao.getTotalPaidAmount(booking.getId());
        if (paidAmount.signum() > 0 || "PENDING_PAYMENT".equalsIgnoreCase(booking.getStatus())) {
            return paidAmount;
        }
        return booking.getTotalAmount() == null ? BigDecimal.ZERO : booking.getTotalAmount();
    }

    private Optional<CancellationRule> loadActiveRule() throws SQLException {
        Optional<HotelPolicy> policy = policyDao.findActiveCancellationPolicy();
        // Fall back to the default rule when no matching policy is active.
        if (policy.isEmpty()) {
            return Optional.empty();
        }
        String content = policy.get().getContent();
        // Ignore policies that do not carry the cancellation rule marker.
        if (content == null || !content.contains(RULE_MARKER)) {
            return Optional.empty();
        }
        Matcher matcher = RULE_PATTERN.matcher(content);
        // Return the default rule if the persisted payload does not match the regex.
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new CancellationRule(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                true));
    }

    public static String buildCancellationContent(int fullRefundDays, int fullRefundRate,
                                                  int partialRefundDays, int partialRefundRate,
                                                  int sameDayRefundRate) {
        return "Khách hủy phòng trước ngày check-in từ " + fullRefundDays
                + " ngày trở lên sẽ được hoàn " + fullRefundRate + "% số tiền đã thanh toán.\n"
                + "Khách hủy phòng trước ngày check-in từ " + partialRefundDays
                + " ngày trở lên sẽ được hoàn " + partialRefundRate + "% số tiền đã thanh toán.\n"
                + "Khách hủy phòng trong ngày check-in hoặc sau thời điểm check-in sẽ được hoàn "
                + sameDayRefundRate + "% số tiền đã thanh toán.\n\n"
                + RULE_MARKER + "\n"
                + "FULL_DAYS=" + fullRefundDays
                + ";FULL_RATE=" + fullRefundRate
                + ";PARTIAL_DAYS=" + partialRefundDays
                + ";PARTIAL_RATE=" + partialRefundRate
                + ";SAME_DAY_RATE=" + sameDayRefundRate;
    }

    public static String displayContent(String content) {
        if (content == null) {
            return "";
        }
        int markerIndex = content.indexOf(RULE_MARKER);
        // Strip the embedded rule metadata before showing the policy text.
        return markerIndex >= 0 ? content.substring(0, markerIndex).trim() : content;
    }

    public static CancellationRule parseRuleOrDefault(String content) {
        if (content == null) {
            return CancellationRule.defaultRule();
        }
        Matcher matcher = RULE_PATTERN.matcher(content);
        if (!matcher.find()) {
            return CancellationRule.defaultRule();
        }
        return new CancellationRule(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Integer.parseInt(matcher.group(4)),
                Integer.parseInt(matcher.group(5)),
                true);
    }

    public static final class RefundResult {
        private final long daysUntilCheckIn;
        private final BigDecimal refundRate;
        private final BigDecimal refundAmount;
        private final BigDecimal cancellationFee;
        private final boolean fromPolicy;

        private RefundResult(long daysUntilCheckIn, BigDecimal refundRate,
                             BigDecimal refundAmount, BigDecimal cancellationFee,
                             boolean fromPolicy) {
            this.daysUntilCheckIn = daysUntilCheckIn;
            this.refundRate = refundRate;
            this.refundAmount = refundAmount;
            this.cancellationFee = cancellationFee;
            this.fromPolicy = fromPolicy;
        }

        public long getDaysUntilCheckIn() { return daysUntilCheckIn; }
        public BigDecimal getRefundRate() { return refundRate; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public BigDecimal getCancellationFee() { return cancellationFee; }
        public boolean isFromPolicy() { return fromPolicy; }
    }

    public static final class CancellationRule {
        private final int fullRefundDays;
        private final int fullRefundRate;
        private final int partialRefundDays;
        private final int partialRefundRate;
        private final int sameDayRefundRate;
        private final boolean fromPolicy;

        private CancellationRule(int fullRefundDays, int fullRefundRate,
                                 int partialRefundDays, int partialRefundRate,
                                 int sameDayRefundRate, boolean fromPolicy) {
            this.fullRefundDays = fullRefundDays;
            this.fullRefundRate = fullRefundRate;
            this.partialRefundDays = partialRefundDays;
            this.partialRefundRate = partialRefundRate;
            this.sameDayRefundRate = sameDayRefundRate;
            this.fromPolicy = fromPolicy;
        }

        private static CancellationRule defaultRule() {
            return new CancellationRule(3, 100, 1, 50, 0, false);
        }

    private BigDecimal rateFor(long daysUntilCheckIn) {
            // Use the most generous refund first, then step down by deadline.
            if (daysUntilCheckIn >= fullRefundDays) {
                return BigDecimal.valueOf(fullRefundRate);
            }
            if (daysUntilCheckIn >= partialRefundDays) {
                return BigDecimal.valueOf(partialRefundRate);
            }
            return BigDecimal.valueOf(sameDayRefundRate);
        }

        public int getFullRefundDays() { return fullRefundDays; }
        public int getFullRefundRate() { return fullRefundRate; }
        public int getPartialRefundDays() { return partialRefundDays; }
        public int getPartialRefundRate() { return partialRefundRate; }
        public int getSameDayRefundRate() { return sameDayRefundRate; }
        public boolean isFromPolicy() { return fromPolicy; }
    }
}
