package com.mycompany.backend.resources;

import Model.Staff;
import Model.Patient;
import DAO.StaffDAO;
import DAO.PatientDAO;
import Service.TokenManager;
import Service.AuditService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private StaffDAO staffDao = new StaffDAO();
    private PatientDAO patientDao = new PatientDAO();

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

        // 1) Try staff accounts (ADMIN / RECEPTIONIST / DENTIST)
        Staff staff = staffDao.findByEmail(email);
        if (staff != null) {
            String hash = staff.getPasswordHash();
            if (hash != null && hash.startsWith("$2b$")) {
                hash = "$2a$" + hash.substring(4);
            }
            if (hash == null || !org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(error("Invalid email or password")).build();
            }
            String token = TokenManager.getInstance().createStaff(staff.getStaffId(), staff.getRole());
            AuditService.getInstance().log("LOGIN", "staff", staff.getStaffId(),
                    staff.getStaffId(), staff.getRole(), "Staff login: " + staff.getEmail());
            return buildTokenResponse(token, staff.getRole(), staff.getName(),
                    staff.getStaffId(), email, 0);
        }

        // 2) Try patient self-service accounts
        Patient patient = patientDao.findByEmail(email);
        if (patient != null) {
            if (!patient.isActive()) {
                return Response.status(403)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(error("Your account is deactivated. Please contact the clinic.")).build();
            }
            String hash = patient.getPasswordHash();
            if (hash != null && hash.startsWith("$2b$")) {
                hash = "$2a$" + hash.substring(4);
            }
            if (hash == null || !org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) {
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(error("Invalid email or password")).build();
            }
            String token = TokenManager.getInstance().createPatient(patient.getPatientId());
            AuditService.getInstance().log("LOGIN", "patients", patient.getPatientId(),
                    patient.getPatientId(), "PATIENT", "Patient login: " + patient.getEmail());
            return buildTokenResponse(token, "PATIENT", patient.getName(),
                    0, email, patient.getPatientId());
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
                AuditService.getInstance().log("LOGOUT", "system", session.id,
                        session.id, session.role, "Logout");
            }
            TokenManager.getInstance().remove(token);
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(java.util.Collections.singletonMap("message", "Logged out successfully"))
                .build();
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
}
