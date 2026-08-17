package com.mycompany.backend.resources;

import Model.Staff;
import DAO.StaffDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private StaffDAO dao = new StaffDAO();

    // Handle CORS preflight (OPTIONS) for /auth/login
    @OPTIONS
    @Path("/login")
    public Response loginOptions() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        if (email == null || password == null) {
            return Response.status(400)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(error("Email and password are required")).build();
        }
        Staff staff = dao.findByEmail(email);
        if (staff == null) {
            return Response.status(401)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(error("Invalid email or password")).build();
        }
        String hash = staff.getPasswordHash();
        if (hash == null || !org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
            return Response.status(401)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(error("Invalid email or password")).build();
        }
        // Issue a real, server-side tracked session token.
        String token = Service.TokenManager.getInstance().create(staff.getStaffId());
        Map<String, Object> result = new HashMap<>();
        result.put("token",   token);
        result.put("role",    staff.getRole());
        result.put("name",    staff.getName());
        result.put("staffId", staff.getStaffId());
        result.put("email",   staff.getEmail());
        return Response.ok(result)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    // Handle CORS preflight (OPTIONS) for /auth/logout
    @OPTIONS
    @Path("/logout")
    public Response logoutOptions() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }

    // Invalidate the current session token.
    @POST
    @Path("/logout")
    public Response logout(@jakarta.ws.rs.HeaderParam("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            Service.TokenManager.getInstance().remove(token);
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(java.util.Collections.singletonMap("message", "Logged out successfully"))
                .build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}

