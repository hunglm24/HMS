package service;

import config.VNPayConfig;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class VNPayService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNPAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    public String createPaymentUrl(long amount, String orderInfo, String transactionRef,
                                   String ipAddress, String returnUrl) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = orderInfo;
        String orderType = "170000"; // Hotel code
        String vnp_TxnRef = transactionRef;
        String vnp_IpAddr = ipAddress;
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        long amountInVND = amount * 100;
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        LocalDateTime createTime = LocalDateTime.now(VIETNAM_ZONE);
        vnp_Params.put("vnp_CreateDate", createTime.format(VNPAY_DATE_FORMAT));
        vnp_Params.put("vnp_ExpireDate", createTime.plusMinutes(15).format(VNPAY_DATE_FORMAT));
        
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    query.append('&');
                    hashData.append('&');
                }
                String encodedName = encode(fieldName);
                String encodedValue = encode(fieldValue);
                hashData.append(encodedName).append('=').append(encodedValue);
                query.append(encodedName).append('=').append(encodedValue);
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    public boolean verifySignature(Map<String, String> fields, String secureHash)
            throws UnsupportedEncodingException {
        if (secureHash == null || secureHash.isBlank()) return false;
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        StringBuilder hashData = new StringBuilder();
        for (String name : names) {
            String value = fields.get(name);
            if (value == null || value.isEmpty()) continue;
            if (hashData.length() > 0) hashData.append('&');
            hashData.append(encode(name)).append('=').append(encode(value));
        }
        String expected = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                secureHash.getBytes(StandardCharsets.US_ASCII));
    }

    private static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
