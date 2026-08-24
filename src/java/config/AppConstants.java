package config;

import java.util.regex.Pattern;

public final class AppConstants {
    private AppConstants() {
    }

    public static final String HOTEL_CONFIG_ATTRIBUTE = "hotelConfig";
    public static final String CURRENT_USER_SESSION_ATTRIBUTE = "currentUser";
    public static final String TOAST_MESSAGE_SESSION_ATTRIBUTE = "toastMessage";
    public static final String TOAST_TYPE_SESSION_ATTRIBUTE = "toastType";

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9+() .-]{8,20}$"
    );
}
