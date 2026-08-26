package com.mycompany.backend.resources;

import model.Patient;
import dao.PatientDAO;
import service.SecurityUtil;
import validation.NICValidator;
import validation.ContactNumberValidator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientResource {
    private PatientDAO dao = new PatientDAO();

    @GET
    public Response getAll() {
        SecurityUtil.requireStaff();
        List<Patient> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/search/{query}")
    public Response search(@PathParam("query") String query) {
        SecurityUtil.requireStaff();
        if (query == null || query.trim().isEmpty()) {
            return Response.ok(Collections.emptyList()).build();
        }
        List<Patient> list = dao.searchByName(query.trim());
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        if (SecurityUtil.isPatient()) {
            if (id != SecurityUtil.currentId()) {
                return Response.status(403).entity(error("You can only view your own record")).build();
            }
        } else {
            SecurityUtil.requireStaff();
        }
        Patient p = dao.findById(id);
        if (p == null) {
            return Response.status(404).entity(error("Patient not found")).build();
        }
        return Response.ok(p).build();
    }

    @GET
    @Path("/nic/{nic}")
    public Response getByNic(@PathParam("nic") String nic) {
        SecurityUtil.requireStaff();
        Patient p = dao.findByNic(nic);
        if (p == null) {
            return Response.status(404).entity(error("Patient not found")).build();
        }
        return Response.ok(p).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Patient p = dao.findById(id);
        if (p == null) return Response.status(404).entity(error("Patient not found")).build();
        if (dao.delete(id)) {
            return Response.ok(success("Patient deleted successfully")).build();
        }
        return Response.status(500).entity(error("Failed to delete patient")).build();
    }

    @GET
    @Path("/me")
    public Response getMe() {
        SecurityUtil.requirePatient();
        Patient p = dao.findById(SecurityUtil.currentId());
        if (p == null) return Response.status(404).entity(error("Patient not found")).build();
        return Response.ok(p).build();
    }

    @PUT
    @Path("/me")
    public Response updateMe(Patient patient) {
        SecurityUtil.requirePatient();
        patient.setPatientId(SecurityUtil.currentId());
        if (dao.update(patient)) {
            return Response.ok(patient).build();
        }
        return Response.status(500).entity(error("Failed to update profile")).build();
    }

    @POST
    public Response create(Patient patient) {
        SecurityUtil.requireReceptionOrAdmin();
        if (patient.getNic() == null || patient.getNic().trim().isEmpty()) {
            return Response.status(400).entity(error("NIC is required")).build();
        }
        String nicErr = NICValidator.validate(patient.getNic());
        if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        if (dao.findByNic(patient.getNic()) != null) {
            return Response.status(400).entity(error("This NIC number is already registered")).build();
        }
        if (patient.getContact() == null || patient.getContact().trim().isEmpty()) {
            return Response.status(400).entity(error("Contact number is required")).build();
        }
        String contactErr = ContactNumberValidator.validate(patient.getContact());
        if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();
        if (dao.findByContact(patient.getContact()) != null) {
            return Response.status(400).entity(error("This contact number is already registered")).build();
        }
        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty() && dao.findByEmail(patient.getEmail()) != null) {
            return Response.status(400).entity(error("This email is already registered")).build();
        }
        if (dao.insert(patient)) {
            return Response.ok(patient).build();
        }
        return Response.status(500).entity(error("Failed to create patient")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Patient patient) {
        if (SecurityUtil.isPatient()) {
            if (id != SecurityUtil.currentId()) {
                return Response.status(403).entity(error("You can only update your own record")).build();
            }
        } else {
            SecurityUtil.requireReceptionOrAdmin();
        }
        patient.setPatientId(id);
        if (patient.getNic() != null && !patient.getNic().trim().isEmpty()) {
            String nicErr = NICValidator.validate(patient.getNic());
            if (nicErr != null) return Response.status(400).entity(error(nicErr)).build();
        }
        if (patient.getContact() != null && !patient.getContact().trim().isEmpty()) {
            String contactErr = ContactNumberValidator.validate(patient.getContact());
            if (contactErr != null) return Response.status(400).entity(error(contactErr)).build();
        }
        if (dao.update(patient)) {
            return Response.ok(patient).build();
        }
        return Response.status(500).entity(error("Failed to update patient")).build();
    }

    @PUT
    @Path("/{id}/toggle")
    public Response toggleActive(@PathParam("id") int id) {
        SecurityUtil.requireReceptionOrAdmin();
        Patient p = dao.findById(id);
        if (p == null) return Response.status(404).entity(error("Patient not found")).build();
        p.setActive(!p.isActive());
        if (dao.setActive(id, p.isActive())) {
            Map<String, Object> res = new HashMap<>();
            res.put("patientId", id);
            res.put("isActive", p.isActive());
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

