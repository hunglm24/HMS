package util;

import java.math.BigDecimal;

public final class MoneyUtil {
    // Utility class; do not instantiate.
    private MoneyUtil() {
    }

    // Parse a VND-formatted money string into a positive BigDecimal.
    public static BigDecimal parseVndMoney(String raw, String fieldLabel) {
        String normalized = ValidationUtil.normalizeText(raw).replaceAll("[^0-9]", "");
        ValidationUtil.requireTrue(!normalized.isEmpty(), fieldLabel + " bat buoc.");
        BigDecimal value = new BigDecimal(normalized);
        ValidationUtil.requireTrue(value.signum() > 0, fieldLabel + " phai lon hon 0.");
        return value;
    }
}
