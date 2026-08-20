package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String role = getRole(req);
        if ("ADMIN".equals(role)) {
            forward(req, resp, "/admin-dashboard.html");
        } else if ("RECEPTIONIST".equals(role)) {
            forward(req, resp, "/receptionist-dashboard.html");
        } else if ("DENTIST".equals(role)) {
            forward(req, resp, "/dentist-dashboard.html");
        } else if ("PATIENT".equals(role)) {
            forward(req, resp, "/patient-dashboard.html");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.html");
        }
    }
}
