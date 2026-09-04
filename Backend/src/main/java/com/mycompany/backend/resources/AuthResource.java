package com.mycompany.backend.resources;

import model.Staff;
import model.Patient;
import model.Dentist;
import dao.StaffDAO;
import dao.PatientDAO;
import dao.DentistDAO;
import service.TokenManager;
import service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private StaffDAO staffDao = new StaffDAO();
    private PatientDAO patientDao = new PatientDAO();
    private DentistDAO dentistDao = new DentistDAO();

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
    public Response login(LoginRequest credentials) {
        String email = credentials.getEmail();
        String password = credentials.getPassword();
        if (email == null || password == null) {
            return Response.status(400)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(error("Email and password are required")).build();
        }

        Staff staff = staffDao.findByEmail(email);
        if (staff != null) {
            String stored = staff.getPassword();
            if (stored == null || !stored.equals(password)) {
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(error("Invalid email or password")).build();
            }
            String token = TokenManager.getInstance().createStaff(staff.getStaffId(), staff.getRole());
            return buildTokenResponse(token, staff.getRole(), staff.getName(),
                    staff.getStaffId(), email, 0);
        }

        Dentist dentist = dentistDao.findByEmail(email);
        if (dentist != null) {
            String stored = dentist.getPassword();
            if (stored == null || !stored.equals(password)) {
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(error("Invalid email or password")).build();
            }
            String token = TokenManager.getInstance().createStaff(dentist.getDentistId(), "DENTIST");
            return buildTokenResponse(token, "DENTIST", dentist.getName(),
                    dentist.getDentistId(), email, 0);
        }

        return Response.status(401)
                .header("Access-Control-Allow-Origin", "*")
                .entity(error("Invalid email or password")).build();
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

    @POST
    @Path("/logout")
    public Response logout(@jakarta.ws.rs.HeaderParam("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            TokenManager.Session session = TokenManager.getInstance().resolve(token);
            if (session != null) {
            }
            TokenManager.getInstance().remove(token);
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(java.util.Collections.singletonMap("message", "Logged out successfully"))
                .build();
    }

    @GET
    @Path("/me")
    public Response getMyProfile(@jakarta.ws.rs.HeaderParam("Authorization") String authHeader) {
        TokenManager.Session session = resolveSession(authHeader);
        if (session == null) {
            return Response.status(401).entity(error("Missing or invalid token")).build();
        }

        String role = session.role;
        if ("DENTIST".equals(role)) {
            Dentist d = dentistDao.findById(session.id);
            if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("type", "DENTIST");
            res.put("id", d.getDentistId());
            res.put("name", d.getName());
            res.put("email", d.getEmail());
            res.put("contact", d.getContact());
            res.put("specialization", d.getSpecialization());
            res.put("availableDays", d.getAvailableDays());
            res.put("role", "DENTIST");
            res.put("isActive", d.isActive());
            res.put("createdAt", d.getCreatedAt());
            return Response.ok(res).build();
        } else {
            Staff s = staffDao.findById(session.id);
            if (s == null) return Response.status(404).entity(error("Staff not found")).build();
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("type", "STAFF");
            res.put("id", s.getStaffId());
            res.put("name", s.getName());
            res.put("email", s.getEmail());
            res.put("contact", s.getContact());
            res.put("address", s.getAddress());
            res.put("nic", s.getNic());
            res.put("role", s.getRole());
            res.put("shiftHours", s.getShiftHours());
            res.put("isActive", s.isActive());
            res.put("createdAt", s.getCreatedAt());
            return Response.ok(res).build();
        }
    }

    @PUT
    @Path("/me")
    public Response updateMyProfile(@jakarta.ws.rs.HeaderParam("Authorization") String authHeader, Map<String, String> body) {
        TokenManager.Session session = resolveSession(authHeader);
        if (session == null) {
            return Response.status(401).entity(error("Missing or invalid token")).build();
        }

        String role = session.role;
        if ("DENTIST".equals(role)) {
            Dentist d = dentistDao.findById(session.id);
            if (d == null) return Response.status(404).entity(error("Dentist not found")).build();

            if (body.containsKey("name")) d.setName(body.get("name"));
            if (body.containsKey("email")) d.setEmail(body.get("email"));
            if (body.containsKey("contact")) d.setContact(body.get("contact"));
            if (body.containsKey("specialization")) d.setSpecialization(body.get("specialization"));
            if (body.containsKey("availableDays")) d.setAvailableDays(body.get("availableDays"));

            if (dentistDao.update(d)) {
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("message", "Profile updated successfully");
                res.put("name", d.getName());
                res.put("email", d.getEmail());
                res.put("contact", d.getContact());
                res.put("specialization", d.getSpecialization());
                res.put("availableDays", d.getAvailableDays());
                return Response.ok(res).build();
            }
            return Response.status(500).entity(error("Failed to update profile")).build();
        } else {
            Staff s = staffDao.findById(session.id);
            if (s == null) return Response.status(404).entity(error("Staff not found")).build();
            if ("SYSTEM_ADMIN".equals(s.getRole())) {
                return Response.status(403).entity(error("System Admin account cannot be modified")).build();
            }

            if (body.containsKey("name")) s.setName(body.get("name"));
            if (body.containsKey("email")) s.setEmail(body.get("email"));
            if (body.containsKey("contact")) s.setContact(body.get("contact"));
            if (body.containsKey("address")) s.setAddress(body.get("address"));
            if (body.containsKey("shiftHours")) s.setShiftHours(body.get("shiftHours"));

            if (staffDao.update(s)) {
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("message", "Profile updated successfully");
                res.put("name", s.getName());
                res.put("email", s.getEmail());
                res.put("contact", s.getContact());
                res.put("address", s.getAddress());
                res.put("role", s.getRole());
                return Response.ok(res).build();
            }
            return Response.status(500).entity(error("Failed to update profile")).build();
        }
    }

    @PUT
    @Path("/me/password")
    public Response changePassword(@jakarta.ws.rs.HeaderParam("Authorization") String authHeader, Map<String, String> body) {
        TokenManager.Session session = resolveSession(authHeader);
        if (session == null) {
            return Response.status(401).entity(error("Missing or invalid token")).build();
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            return Response.status(400).entity(error("All password fields are required")).build();
        }
        if (!newPassword.equals(confirmPassword)) {
            return Response.status(400).entity(error("New password and confirm password do not match")).build();
        }
        if (newPassword.length() < 6) {
            return Response.status(400).entity(error("New password must be at least 6 characters")).build();
        }

        String role = session.role;
        if ("DENTIST".equals(role)) {
            Dentist d = dentistDao.findById(session.id);
            if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
            String stored = d.getPassword();
            if (stored == null || !stored.equals(currentPassword)) {
                return Response.status(400).entity(error("Current password is incorrect")).build();
            }
            d.setPassword(newPassword);
            if (dentistDao.update(d)) {
                return Response.ok(success("Password changed successfully")).build();
            }
            return Response.status(500).entity(error("Failed to change password")).build();
        } else {
            Staff s = staffDao.findById(session.id);
            if (s == null) return Response.status(404).entity(error("Staff not found")).build();
            String stored = s.getPassword();
            if (stored == null || !stored.equals(currentPassword)) {
                return Response.status(400).entity(error("Current password is incorrect")).build();
            }
            s.setPassword(newPassword);
            if (staffDao.update(s)) {
                return Response.ok(success("Password changed successfully")).build();
            }
            return Response.status(500).entity(error("Failed to change password")).build();
        }
    }

    private TokenManager.Session resolveSession(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        return TokenManager.getInstance().resolve(token);
    }

    private Response buildTokenResponse(String token, String role, String name,
                                        int staffId, String email, int patientId) {
        Map<String, Object> result = new HashMap<>();
        result.put("token",     token);
        result.put("role",      role);
        result.put("name",      name);
        result.put("staffId",   staffId);
        result.put("patientId", patientId);
        result.put("email",     email);
        return Response.ok(result)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }

    private Map<String, String> success(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}
