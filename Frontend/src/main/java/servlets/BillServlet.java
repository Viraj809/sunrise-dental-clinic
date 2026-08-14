package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/bills")
public class BillServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String action = req.getParameter("action");
        if ("view".equals(action)) {
            req.setAttribute("billId", req.getParameter("bill_id"));
            forward(req, resp, "/view-bill.html");
        } else {
            forward(req, resp, "/billing.html");
        }
    }
}
