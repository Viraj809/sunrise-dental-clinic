package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/api/appointments")
public class AppointmentServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String action = req.getParameter("action");
        String backendBase = "http://localhost:8080/Backend/resources";
        String token = (String) req.getSession().getAttribute("token");

        if ("search".equals(action)) {
            String apptNo = req.getParameter("appointment_no");
            String urlStr = backendBase + "/appointments/no/" + apptNo;
            String result = callGet(urlStr, token);
            req.setAttribute("json", result);
            forward(req, resp, "/view-appointment.html");
        } else if ("register".equals(action)) {
            forward(req, resp, "/register-appointment.html");
        } else {
            String result = callGet(backendBase + "/appointments", token);
            req.setAttribute("json", result);
            forward(req, resp, "/view-appointment.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) return;
        String token = (String) req.getSession().getAttribute("token");
        String backendBase = "http://localhost:8080/Backend/resources";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"patient_id\":").append(req.getParameter("patient_id")).append(",");
        sb.append("\"dentist_id\":").append(req.getParameter("dentist_id")).append(",");
        sb.append("\"treatment_type\":\"").append(escape(req.getParameter("treatment_type"))).append("\",");
        sb.append("\"appointment_date\":\"").append(req.getParameter("appointment_date")).append("\",");
        sb.append("\"appointment_time\":\"").append(req.getParameter("appointment_time")).append("\",");
        sb.append("\"notes\":\"").append(escape(req.getParameter("contact"))).append("\",");
        sb.append("\"created_by\":").append(req.getSession().getAttribute("staffId"));
        sb.append("}");

        String result = callPost(backendBase + "/appointments", sb.toString(), token);
        if (result != null && !result.contains("\"error\"")) {
            resp.sendRedirect(req.getContextPath() + "/appointments?action=search&appointment_no=" + extractValue(result, "appointmentNo"));
        } else {
            resp.sendRedirect(req.getContextPath() + "/appointments?action=register&error=Failed+to+create+appointment");
        }
    }

    private String callGet(String urlStr, String token) throws IOException {
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        return readResponse(conn);
    }

    private String callPost(String urlStr, String json, String token) throws IOException {
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);
        conn.getOutputStream().write(json.getBytes());
        return readResponse(conn);
    }

    private String readResponse(java.net.HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        java.io.InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
