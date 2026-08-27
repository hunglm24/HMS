package listener;

import dao.BookingRefundDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class BookingRefundSchemaListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            new BookingRefundDao().initializeSchema();
        } catch (Exception ex) {
            event.getServletContext().log("Không thể khởi tạo workflow hoàn tiền.", ex);
        }
    }
}
