package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/patients")
public class PatientServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            forward(req, resp, "/add-patient.html");
        } else if ("edit".equals(action)) {
            req.setAttribute("patientId", req.getParameter("patient_id"));
            forward(req, resp, "/edit-patient.html");
        } else if ("history".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/patient-history.html?patient_id=" + req.getParameter("patient_id"));
        } else {
            forward(req, resp, "/manage-patients.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String action = req.getParameter("action");
        if ("edit".equals(action)) {
            req.setAttribute("patientId", req.getParameter("patient_id"));
            forward(req, resp, "/edit-patient.html");
        } else {
            resp.sendRedirect(req.getContextPath() + "/patients");
        }
    }
}
