package controller.page.manager;

import dao.InvoiceDao;
import dto.InvoiceListDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manager/invoices", "/admin/invoices"})
public class InvoiceListServlet extends HttpServlet {

    private final InvoiceDao invoiceDao = new InvoiceDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        if (keyword == null) keyword = "";
        
        String status = request.getParameter("status");
        if (status == null) status = "";

        int page = 1;
        int pageSize = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        int offset = (page - 1) * pageSize;
        
        List<InvoiceListDto> invoices = invoiceDao.searchInvoices(keyword, status, offset, pageSize);
        int totalItems = invoiceDao.countInvoices(keyword, status);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        request.setAttribute("invoices", invoices);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);

        request.getRequestDispatcher("/WEB-INF/views/manager/invoices.jsp").forward(request, response);
    }
}
