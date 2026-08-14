package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/dentists")
public class DentistServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String action = req.getParameter("action");
        if ("schedule".equals(action)) {
            req.setAttribute("dentistId", req.getParameter("dentist_id"));
            forward(req, resp, "/dentist-schedule.html");
        } else {
            forward(req, resp, "/manage-dentists.html");
        }
    }
}
