package service;

import dao.UserDao;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class AdminUserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+() .-]{8,20}$");
    private final UserDao userDao;

    public AdminUserService() {
        this(new UserDao());
    }

    public AdminUserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> listUsers(String keyword) throws SQLException {
        return userDao.listUsers(keyword);
    }

    public Optional<User> findById(long id) throws SQLException {
        return userDao.findById(id);
    }

    public void createUser(String fullName, String email, String phone, String password,
                           long roleId, String status) throws SQLException {
        String normalizedName = validateName(fullName);
        String normalizedEmail = validateEmail(email);
        String normalizedPhone = validatePhone(phone);
        String normalizedStatus = validateStatus(status);
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (roleId <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn vai trò.");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email này đã được sử dụng.");
        }
        userDao.createUser(normalizedName, normalizedEmail, normalizedPhone,
                PasswordUtil.hash(password), roleId, normalizedStatus);
    }

    public void updateUser(long id, String fullName, String phone, long roleId, String status)
            throws SQLException {
        if (id <= 0 || userDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }
        if (roleId <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn vai trò.");
        }
        userDao.updateUser(id, validateName(fullName), validatePhone(phone), roleId, validateStatus(status));
    }

    private String validateName(String fullName) {
        String value = fullName == null ? "" : fullName.trim();
        if (value.length() < 2 || value.length() > 100) {
            throw new IllegalArgumentException("Họ tên phải có từ 2 đến 100 ký tự.");
        }
        return value;
    }

    private String validateEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        return value;
    }

    private String validatePhone(String phone) {
        String value = phone == null ? "" : phone.trim();
        if (!value.isEmpty() && !PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng.");
        }
        return value.isEmpty() ? null : value;
    }

    private String validateStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(value) && !"INACTIVE".equals(value) && !"LOCKED".equals(value)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }
        return value;
    }
}
