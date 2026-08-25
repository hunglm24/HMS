package listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import service.PendingPaymentExpirationService;

@WebListener
public class PendingPaymentExpirationListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "hms-pending-payment-expiration");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(() -> expire(context), 0, 1, TimeUnit.MINUTES);
    }

    private void expire(ServletContext context) {
        try {
            int count = new PendingPaymentExpirationService().expireOverdueBookings();
            if (count > 0) {
                context.log("Đã hủy " + count
                        + " booking quá hạn thanh toán và giải phóng phòng.");
            }
        } catch (Exception ex) {
            context.log("Không thể xử lý booking quá hạn thanh toán.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) scheduler.shutdownNow();
    }
}
