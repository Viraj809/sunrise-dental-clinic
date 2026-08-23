package com.mycompany.backend.resources;

import Model.Staff;
import Model.Dentist;
import DAO.StaffDAO;
import DAO.DentistDAO;
import Service.SecurityUtil;
import Service.AuditService;
import Validation.NICValidator;
import Validation.ContactNumberValidator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

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
    @Path("/search/{query}")
    public Response search(@PathParam("query") String query) {
        SecurityUtil.requireAdmin();
        if (query == null || query.trim().isEmpty()) {
            return Response.ok(Collections.emptyList()).build();
        }
        List<Staff> list = dao.search(query.trim());
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

    @GET
    @Path("/detect/{id}")
    public Response detect(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("staffId", s.getStaffId());
        res.put("name", s.getName());
        res.put("email", s.getEmail());
        res.put("role", s.getRole());
        res.put("contact", s.getContact());
        res.put("nic", s.getNic());
        res.put("isActive", s.isActive());
        res.put("shiftHours", s.getShiftHours());
        res.put("address", s.getAddress());
        res.put("createdAt", s.getCreatedAt());
        return Response.ok(res).build();
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
            return Response.status(400).entity(error("Role must be ADMIN, RECEPTIONIST or DENTIST. Dentists must be created through the Dentists page.")).build();
        }

        String nicErr = NICValidator.validate(nic);
        if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        String contactErr = ContactNumberValidator.validate(contact != null ? contact : "");
        if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();

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
        if ("SYSTEM_ADMIN".equals(s.getRole())) {
            return Response.status(403).entity(error("System Admin account cannot be modified")).build();
        }

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

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();
        if ("SYSTEM_ADMIN".equals(s.getRole())) {
            return Response.status(403).entity(error("System Admin account cannot be deleted")).build();
        }

        // Staff කෙනෙක් DENTIST කෙනෙක් නම්, Dentist table එකෙන් ඒ record එකත් මකන්න
        if ("DENTIST".equals(s.getRole()) && s.getEmail() != null) {
            try {
                DentistDAO dentistDao = new DentistDAO();
                Dentist dentist = dentistDao.findByEmail(s.getEmail());
                if (dentist != null) {
                    dentistDao.delete(dentist.getDentistId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (dao.delete(id)) {
            AuditService.getInstance().logCurrent("DELETE", "staff", id, "Staff deleted: " + s.getEmail());
            return Response.ok(success("Staff deleted successfully")).build();
        }
        return Response.status(500).entity(error("Failed to delete staff")).build();
    }

    @PUT
    @Path("/{id}/toggle")
    public Response toggleActive(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Staff s = dao.findById(id);
        if (s == null) return Response.status(404).entity(error("Staff not found")).build();
        if ("SYSTEM_ADMIN".equals(s.getRole())) {
            return Response.status(403).entity(error("System Admin account cannot be deactivated")).build();
        }
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

    private Map<String, String> success(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}