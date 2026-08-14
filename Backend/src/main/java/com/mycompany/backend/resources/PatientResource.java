package com.mycompany.backend.resources;

import Model.Patient;
import DAO.PatientDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientResource {
    private PatientDAO dao = new PatientDAO();

    @GET
    public Response getAll() {
        List<Patient> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Patient p = dao.findById(id);
        if (p == null) {
            return Response.status(404).entity(error("Patient not found")).build();
        }
        return Response.ok(p).build();
    }

    @GET
    @Path("/nic/{nic}")
    public Response getByNic(@PathParam("nic") String nic) {
        Patient p = dao.findByNic(nic);
        if (p == null) {
            return Response.status(404).entity(error("Patient not found")).build();
        }
        return Response.ok(p).build();
    }

    @POST
    public Response create(Patient patient) {
        if (patient.getNic() == null || patient.getNic().isEmpty()) {
            return Response.status(400).entity(error("NIC is required")).build();
        }
        if (patient.getContact() == null || !patient.getContact().matches("^0\\d{9}$")) {
            return Response.status(400).entity(error("Contact number must be 10 digits and start with 0")).build();
        }
        if (dao.insert(patient)) {
            return Response.ok(patient).build();
        }
        return Response.status(500).entity(error("Failed to create patient")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Patient patient) {
        patient.setPatientId(id);
        if (dao.update(patient)) {
            return Response.ok(patient).build();
        }
        return Response.status(500).entity(error("Failed to update patient")).build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}
