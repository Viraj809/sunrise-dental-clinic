package com.mycompany.backend.resources;

import Model.Staff;
import DAO.StaffDAO;
import Service.SecurityUtil;
import Service.AuditService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff REST Resource – full CRUD for admin management.
 */
@Path("/staff")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffResource {

    private final StaffDAO dao = new StaffDAO();

    @GET
    public Response getAll() {
        SecurityUtil.requireAdmin();
        List<Staff> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();
        return Response.ok(s).build();
    }

    @POST
    public Response create(Map<String, String> body) {
        SecurityUtil.requireAdmin();
        String name      = body.get("name");
        String email     = body.get("email");
        String contact   = body.get("contact");
        String address   = body.get("address");
        String nic       = body.get("nic");
        String password  = body.get("password");
        String role      = body.get("role");
        String shift     = body.get("shift_hours");

        if (name == null || email == null || password == null || role == null || nic == null) {
            return Response.status(400).entity(error("name, email, nic, password and role are required")).build();
        }
        if (!role.matches("ADMIN|RECEPTIONIST|DENTIST")) {
            return Response.status(400).entity(error("Role must be ADMIN, RECEPTIONIST or DENTIST")).build();
        }

        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

        Staff s = new Staff();
        s.setName(name);
        s.setEmail(email);
        s.setContact(contact != null ? contact : "");
        s.setAddress(address != null ? address : "");
        s.setNic(nic);
        s.setPasswordHash(hash);
        s.setRole(role);
        s.setShiftHours(shift != null ? shift : "");
        s.setActive(true);

        if (dao.insert(s)) {
            AuditService.getInstance().logCurrent("INSERT", "staff", s.getStaffId(), "Staff created: " + email);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Staff created successfully");
            res.put("email", email);
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to create staff")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Map<String, String> body) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();

        if (body.containsKey("name"))        s.setName(body.get("name"));
        if (body.containsKey("email"))       s.setEmail(body.get("email"));
        if (body.containsKey("contact"))     s.setContact(body.get("contact"));
        if (body.containsKey("address"))     s.setAddress(body.get("address"));
        if (body.containsKey("role"))        s.setRole(body.get("role"));
        if (body.containsKey("shift_hours")) s.setShiftHours(body.get("shift_hours"));
        if (body.containsKey("password")) {
            s.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(
                    body.get("password"), org.mindrot.jbcrypt.BCrypt.gensalt()));
        }

        if (dao.update(s)) return Response.ok(s).build();
        return Response.status(500).entity(error("Failed to update staff")).build();
    }

    @PUT
    @Path("/{id}/toggle")
    public Response toggleActive(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();
        s.setActive(!s.isActive());
        if (dao.update(s)) {
            Map<String, Object> res = new HashMap<>();
            res.put("staffId", id);
            res.put("isActive", s.isActive());
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to toggle status")).build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}
