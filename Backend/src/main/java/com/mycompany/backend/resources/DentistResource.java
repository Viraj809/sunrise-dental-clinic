package com.mycompany.backend.resources;

import Model.Dentist;
import Model.Staff;
import DAO.DentistDAO;
import DAO.StaffDAO;
import Service.SecurityUtil;
import Service.AuditService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/dentists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DentistResource {

    private final DentistDAO dao = new DentistDAO();
    private final StaffDAO   staffDao = new StaffDAO();

    @GET
    public Response getAll() {
        List<Dentist> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Dentist d = dao.findById(id);
        if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
        return Response.ok(d).build();
    }

    @POST
    public Response create(Dentist dentist) {
        SecurityUtil.requireAdmin();
        if (dentist.getName() == null || dentist.getName().isEmpty()) {
            return Response.status(400).entity(error("Name is required")).build();
        }
        dentist.setActive(true);
        if (dao.insert(dentist)) {
            AuditService.getInstance().logCurrent("INSERT", "dentists", dentist.getDentistId(), "Dentist added: " + dentist.getName());
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to add dentist")).build();
    }

    /**
     * Register a dentist together with a login (staff) account so the new dentist
     * can immediately sign in and access their dashboard. The staff email is kept
     * identical to the dentist email so the dentist dashboard can be scoped by email.
     */
    @POST
    @Path("/register")
    public Response register(Map<String, String> body) {
        SecurityUtil.requireAdmin();
        String name          = body.get("name");
        String email         = body.get("email");
        String nic           = body.get("nic");
        String contact       = body.get("contact");
        String specialization= body.get("specialization");
        String availableDays = body.get("available_days");
        String rawPassword   = body.get("password");

        if (name == null || name.isEmpty() || email == null || email.isEmpty()
                || nic == null || nic.isEmpty()) {
            return Response.status(400)
                    .entity(error("Name, email and NIC are required to create a dentist with login")).build();
        }
        // Prevent duplicate login accounts.
        if (staffDao.findByEmail(email) != null) {
            return Response.status(400)
                    .entity(error("A login account with email " + email + " already exists")).build();
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = "dentist123";
        }

        // 1) Save the dentist profile (dentists table).
        Dentist d = new Dentist();
        d.setName(name);
        d.setSpecialization(specialization != null ? specialization : "");
        d.setContact(contact != null ? contact : "");
        d.setEmail(email);
        d.setAvailableDays(availableDays != null ? availableDays : "");
        d.setActive(true);
        if (!dao.insert(d)) {
            return Response.status(500).entity(error("Failed to save dentist")).build();
        }

        // 2) Create the staff login account (staff table, role DENTIST).
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        Staff s = new Staff();
        s.setName(name);
        s.setEmail(email);
        s.setContact(contact != null ? contact : "");
        s.setNic(nic);
        s.setPasswordHash(hash);
        s.setRole("DENTIST");
        s.setShiftHours("09:00 - 17:00");
        s.setActive(true);
        if (!staffDao.insert(s)) {
            return Response.status(500)
                    .entity(error("Dentist saved, but failed to create the login account")).build();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Dentist and login account created successfully");
        res.put("email", email);
        res.put("password", rawPassword);
        return Response.ok(res).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Dentist dentist) {
        SecurityUtil.requireAdmin();
        dentist.setDentistId(id);
        if (dao.update(dentist)) {
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to update dentist")).build();
    }

    @PUT
    @Path("/{id}/toggle")
    public Response toggleActive(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Dentist d = dao.findById(id);
        if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
        d.setActive(!d.isActive());
        if (dao.update(d)) {
            Map<String, Object> res = new HashMap<>();
            res.put("dentistId", id);
            res.put("isActive", d.isActive());
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
