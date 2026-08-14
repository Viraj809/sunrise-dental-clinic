package com.mycompany.backend.resources;

import Model.Staff;
import DAO.StaffDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private StaffDAO dao = new StaffDAO();

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        if (email == null || password == null) {
            return Response.status(400).entity(error("Email and password are required")).build();
        }
        Staff staff = dao.findByEmail(email);
        if (staff == null) {
            return Response.status(401).entity(error("Invalid email or password")).build();
        }
        if (!password.equals("admin123") && !password.equals("reception123") && !password.equals("dentist123")) {
            return Response.status(401).entity(error("Invalid email or password")).build();
        }
        String token = java.util.UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", staff.getRole());
        result.put("name", staff.getName());
        result.put("staffId", staff.getStaffId());
        return Response.ok(result).build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}
