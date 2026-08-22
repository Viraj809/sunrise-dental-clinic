package com.mycompany.backend.resources;

import Model.Dentist;
import Model.Staff;
import DAO.DentistDAO;
import DAO.StaffDAO;
import Service.SecurityUtil;
import Service.AuditService;
import Validation.NICValidator;
import Validation.ContactNumberValidator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

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

    @GET
    @Path("/{id}/available-days")
    public Response getAvailableDays(@PathParam("id") int id) {
        Dentist d = dao.findById(id);
        if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
        List<String> days = dao.getAvailableDays(id);
        Map<String, Object> res = new HashMap<>();
        res.put("dentistId", id);
        res.put("availableDays", days);
        return Response.ok(res).build();
    }

    @PUT
    @Path("/{id}/available-days")
    public Response updateAvailableDays(@PathParam("id") int id, Map<String, List<String>> body) {
        SecurityUtil.requireAdmin();
        Dentist d = dao.findById(id);
        if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
        List<String> days = body.getOrDefault("availableDays", Collections.emptyList());
        if (days == null) days = Collections.emptyList();
        days = days.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());
        if (dao.saveAvailableDays(id, days)) {
            Map<String, Object> res = new HashMap<>();
            res.put("dentistId", id);
            res.put("availableDays", days);
            res.put("message", "Available days updated");
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to update available days")).build();
    }

    @POST
    public Response create(Dentist dentist) {
        SecurityUtil.requireAdmin();
        if (dentist.getName() == null || dentist.getName().trim().isEmpty()) {
            return Response.status(400).entity(error("Name is required")).build();
        }
        String nicErr = NICValidator.validate(dentist.getNic() != null ? dentist.getNic() : "");
        if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        if (dentist.getNic() != null && !dentist.getNic().trim().isEmpty() && dao.findByNic(dentist.getNic()) != null) {
            return Response.status(400).entity(error("This NIC number is already registered")).build();
        }
        String contactErr = ContactNumberValidator.validate(dentist.getContact());
        if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();
        if (dentist.getContact() != null && !dentist.getContact().trim().isEmpty() && dao.findByContact(dentist.getContact()) != null) {
            return Response.status(400).entity(error("This contact number is already registered")).build();
        }
        if (dentist.getEmail() != null && !dentist.getEmail().trim().isEmpty() && dao.findByEmail(dentist.getEmail()) != null) {
            return Response.status(400).entity(error("This email is already registered")).build();
        }
        dentist.setActive(true);
        if (dao.insert(dentist)) {
            AuditService.getInstance().logCurrent("INSERT", "dentists", dentist.getDentistId(), "Dentist added: " + dentist.getName());
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to add dentist")).build();
    }

    @POST
    @Path("/register")
    public Response register(Map<String, String> body) {
        SecurityUtil.requireAdmin();
        String name           = body.get("name");
        String email          = body.get("email");
        String nic            = body.get("nic");
        String contact        = body.get("contact");
        String specialization = body.get("specialization");
        String availableDays  = body.get("available_days");
        String rawPassword    = body.get("password");

        if (name == null || name.isEmpty() || email == null || email.isEmpty()
                || nic == null || nic.isEmpty()) {
            return Response.status(400)
                    .entity(error("Name, email and NIC are required to create a dentist with login")).build();
        }
        String nicErr = NICValidator.validate(nic);
        if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        if (staffDao.findByNic(nic) != null) {
            return Response.status(400).entity(error("This NIC number is already registered")).build();
        }
        String contactErr = ContactNumberValidator.validate(contact != null ? contact : "");
        if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();
        if (contact != null && !contact.trim().isEmpty() && staffDao.findByContact(contact) != null) {
            return Response.status(400).entity(error("This contact number is already registered")).build();
        }
        if (staffDao.findByEmail(email) != null) {
            return Response.status(400)
                    .entity(error("A login account with email " + email + " already exists")).build();
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = "dentist123";
        }

        Dentist d = new Dentist();
        d.setName(name);
        d.setSpecialization(specialization != null ? specialization : "");
        d.setContact(contact != null ? contact : "");
        d.setEmail(email);
        d.setNic(nic);
        d.setAvailableDays(availableDays != null ? availableDays : "");
        d.setActive(true);
        if (!dao.insert(d)) {
            return Response.status(500).entity(error("Failed to save dentist")).build();
        }
        List<String> days = new ArrayList<>();
        if (availableDays != null && !availableDays.trim().isEmpty()) {
            for (String s : availableDays.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) days.add(t);
            }
        }
        dao.saveAvailableDays(d.getDentistId(), days);

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
        if (dentist.getNic() != null && !dentist.getNic().trim().isEmpty()) {
            String nicErr = NICValidator.validate(dentist.getNic());
            if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        }
        if (dentist.getContact() != null && !dentist.getContact().trim().isEmpty()) {
            String contactErr = ContactNumberValidator.validate(dentist.getContact());
            if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();
        }
        if (dao.update(dentist)) {
            List<String> days = new ArrayList<>();
            if (dentist.getAvailableDays() != null && !dentist.getAvailableDays().trim().isEmpty()) {
                for (String s : dentist.getAvailableDays().split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) days.add(t);
                }
            }
            dao.saveAvailableDays(id, days);
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to update dentist")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Dentist d = dao.findById(id);
        if (d == null) return Response.status(404).entity(error("Dentist not found")).build();
        if (dao.hasAppointments(id)) {
            return Response.status(400).entity(error("Cannot delete dentist with existing appointments. Cancel or reassign appointments first.")).build();
        }
        if (dao.delete(id)) {
            AuditService.getInstance().logCurrent("DELETE", "dentists", id, "Dentist deleted: " + d.getName());
            return Response.ok(success("Dentist deleted successfully")).build();
        }
        return Response.status(500).entity(error("Failed to delete dentist")).build();
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

    private Map<String, String> success(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}

