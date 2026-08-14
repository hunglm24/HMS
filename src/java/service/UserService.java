package service;

import dao.UserDao;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+() .-]{8,20}$");
    private final UserDao userDao;

    public UserService() {
        this(new UserDao());
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String email, String password) throws SQLException {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> result = userDao.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        if (result.isEmpty()) {
            // Giảm chênh lệch thời gian xử lý giữa email tồn tại và không tồn tại.
            PasswordUtil.verify(password, PasswordUtil.DUMMY_HASH);
            return Optional.empty();
        }

        User user = result.get();
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())
                || !PasswordUtil.verify(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public User register(String fullName, String email, String phone, String password,
                         String confirmPassword) throws SQLException {
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = phone == null ? "" : phone.trim();

        if (normalizedName.length() < 2 || normalizedName.length() > 100) {
            throw new IllegalArgumentException("Họ tên phải có từ 2 đến 100 ký tự.");
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        if (!normalizedPhone.isEmpty() && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email này đã được sử dụng.");
        }

        return userDao.createCustomer(normalizedName, normalizedEmail,
                normalizedPhone.isEmpty() ? null : normalizedPhone, PasswordUtil.hash(password));
    }

    public void resetPassword(String email, String phone, String password, String confirmPassword)
            throws SQLException {
        User user = userDao.findByEmail(email == null ? "" : email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Thông tin xác minh không chính xác."));
        String storedPhone = user.getPhone() == null ? "" : user.getPhone().trim();
        if (phone == null || !storedPhone.equals(phone.trim())) {
            throw new IllegalArgumentException("Thông tin xác minh không chính xác.");
        }
        validateNewPassword(password, confirmPassword);
        userDao.updatePassword(user, PasswordUtil.hash(password));
    }

    public void changePassword(User user, String currentPassword, String newPassword,
                               String confirmPassword) throws SQLException {
        User stored = userDao.findByEmail(user.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (!PasswordUtil.verify(currentPassword, stored.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        validateNewPassword(newPassword, confirmPassword);
        userDao.updatePassword(stored, PasswordUtil.hash(newPassword));
    }

    public void updateProfile(User user, String fullName, String phone) throws SQLException {
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedPhone = phone == null ? "" : phone.trim();
        if (normalizedName.length() < 2 || normalizedName.length() > 100) {
            throw new IllegalArgumentException("Họ tên phải có từ 2 đến 100 ký tự.");
        }
        if (!normalizedPhone.isEmpty() && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng.");
        }
        userDao.updateProfile(user, normalizedName, normalizedPhone.isEmpty() ? null : normalizedPhone);
        user.setFullName(normalizedName);
        user.setPhone(normalizedPhone.isEmpty() ? null : normalizedPhone);
    }

    private void validateNewPassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }
    }
}
