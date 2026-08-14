package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String backendUrl = "http://localhost:8080/Backend/resources/auth/login";
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        java.net.URL url = new java.net.URL(backendUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(json.getBytes());

        int status = conn.getResponseCode();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
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

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.html?error=Invalid+email+or+password");
        }
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
