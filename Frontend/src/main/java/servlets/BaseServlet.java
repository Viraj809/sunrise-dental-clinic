package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class BaseServlet extends HttpServlet {
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String path) throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    protected boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return false;
        }
        return true;
    }

    protected String getRole(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object role = session.getAttribute("role");
            return role != null ? role.toString() : null;
        }
        return null;
    }
}
