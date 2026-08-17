package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/login.html?error=Email+and+password+are+required");
            return;
        }

        String backendBase = getServletContext().getInitParameter("backendUrl");
        if (backendBase == null) backendBase = "http://localhost:8080/Backend/resources";
        String backendUrl = backendBase + "/auth/login";
        String json = "{\"email\":\"" + escapeJson(email) + "\",\"password\":\"" + escapeJson(password) + "\"}";

        try {
            URL url = new URL(backendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(json.getBytes());

            int status = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(status < 400 ? conn.getInputStream() : conn.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            if (status == 200) {
                String response = sb.toString();
                String token = extractValue(response, "token");
                String role = extractValue(response, "role");
                String name = extractValue(response, "name");
                int staffId = Integer.parseInt(extractValue(response, "staffId"));

                HttpSession session = req.getSession();
                session.setAttribute("token", token);
                session.setAttribute("role", role);
                session.setAttribute("name", name);
                session.setAttribute("staffId", staffId);
                session.setAttribute("user", name);
                session.setMaxInactiveInterval(30 * 60);

                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.html?error=" + URLEncoder.encode("Invalid email or password"));
            }
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/login.html?error=" + URLEncoder.encode("Cannot connect to server"));
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
