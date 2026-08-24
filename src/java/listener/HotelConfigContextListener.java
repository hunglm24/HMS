package listener;

import dao.HotelConfigDao;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import model.HotelConfig;

@WebListener
public class HotelConfigContextListener implements ServletContextListener {
    private static final String ATTR_NAME = "hotelConfig";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        reloadHotelConfig(sce.getServletContext());
    }

    public static void reloadHotelConfig(ServletContext context) {
        HotelConfigDao dao = new HotelConfigDao();
        HotelConfig config;
        try {
            config = dao.loadForEdit();
        } catch (Exception ex) {
            config = dao.createDefaultConfig();
        }
        context.setAttribute(ATTR_NAME, config);
    }
}
