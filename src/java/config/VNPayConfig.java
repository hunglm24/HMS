package config;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** VNPay utilities adapted from the supplied vnpay_jsp 2.1.0 demo. */
public class VNPayConfig {
    private static final Properties LOCAL_PROPERTIES = loadLocalProperties();
    public static final String vnp_PayUrl = env("HMS_VNPAY_PAY_URL",
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
    public static final String vnp_ReturnUrl = env("HMS_VNPAY_RETURN_URL",
            "http://localhost:8080/HMS/payment-return");
    public static final String vnp_TmnCode = env("HMS_VNPAY_TMN_CODE", "4YUP19I4");
    public static final String vnp_HashSecret = env("HMS_VNPAY_HASH_SECRET", "MDUIFDCRAKLNBPOFIAFNEKFRNMFBYEPX");
    public static final String vnp_apiUrl = env("HMS_VNPAY_API_URL",
            "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");

    private static final boolean PAYMENT_TEST_MODE = Boolean.parseBoolean(
            env("HMS_PAYMENT_TEST_MODE", "false"));

    public static String md5(String message) {
        return digest("MD5", message);
    }

    public static String sha256(String message) {
        return digest("SHA-256", message);
    }

    private static String digest(String algorithm, String message) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return toHex(md.digest(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            return "";
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder data = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) continue;
            if (data.length() > 0) data.append('&');
            data.append(fieldName).append('=').append(fieldValue);
        }
        return hmacSHA512(vnp_HashSecret, data.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) throw new NullPointerException();
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            return toHex(hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            } else {
                ipAddress = ipAddress.split(",", 2)[0].trim();
            }
            return ipAddress;
        } catch (Exception ex) {
            return "Invalid IP:" + ex.getMessage();
        }
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean isConfigured() {
        return !vnp_TmnCode.isBlank() && !vnp_HashSecret.isBlank();
    }

    public static boolean isPaymentTestMode() {
        return PAYMENT_TEST_MODE;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) sb.append(String.format("%02x", value & 0xff));
        return sb.toString();
    }

    private static String env(String name, String fallback) {
        String localValue = LOCAL_PROPERTIES.getProperty(name);
        if (localValue != null && !localValue.isBlank()) {
            return localValue.trim();
        }
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static Properties loadLocalProperties() {
        Properties properties = new Properties();
        String configuredPath = System.getProperty("hms.vnpay.config.file", "").trim();
        if (!configuredPath.isEmpty()) {
            try (java.io.InputStream input = Files.newInputStream(Path.of(configuredPath))) {
                properties.load(input);
                return properties;
            } catch (java.io.IOException ignored) {
                // Fall back to a classpath resource or environment variables.
            }
        }
        try (java.io.InputStream input = VNPayConfig.class.getResourceAsStream(
                "/config/vnpay-local.properties")) {
            if (input != null) properties.load(input);
        } catch (java.io.IOException ignored) {
            // Environment variables remain the fallback configuration source.
        }
        return properties;
    }
}
