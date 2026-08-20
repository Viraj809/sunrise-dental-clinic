package com.mycompany.backend.resources;

import Model.Treatment;
import DAO.TreatmentDAO;
import Service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/treatments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TreatmentResource {
    private TreatmentDAO dao = new TreatmentDAO();

    @GET
    public Response getAll() {
        List<Treatment> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{code}")
    public Response getByCode(@PathParam("code") String code) {
        Treatment t = dao.findByCode(code);
        if (t == null) {
            return Response.status(404).entity("{\"error\":\"Treatment not found\"}").build();
        }
        return Response.ok(t).build();
    }

    @POST
    public Response create(Treatment treatment) {
        SecurityUtil.requireAdmin();
        if (treatment.getTreatmentCode() == null || treatment.getTreatmentCode().isEmpty()
                || treatment.getTreatmentName() == null || treatment.getTreatmentName().isEmpty()) {
            return Response.status(400).entity(error("treatment_code and treatment_name are required")).build();
        }
        if (treatment.getBasePrice() < 0 || treatment.getConsultationFee() < 0) {
            return Response.status(400).entity(error("Prices cannot be negative")).build();
        }
        if (dao.insert(treatment)) {
            return Response.ok(treatment).build();
        }
        return Response.status(500).entity(error("Failed to add treatment (code may already exist)")).build();
    }

    @PUT
    @Path("/{code}")
    public Response update(@PathParam("code") String code, Treatment treatment) {
        SecurityUtil.requireAdmin();
        treatment.setTreatmentCode(code);
        if (dao.update(treatment)) {
            return Response.ok(treatment).build();
        }
        return Response.status(500).entity(error("Failed to update treatment")).build();
    }

    @DELETE
    @Path("/{code}")
    public Response delete(@PathParam("code") String code) {
        SecurityUtil.requireAdmin();
        if (dao.delete(code)) {
            return Response.ok(success("Treatment deleted")).build();
        }
        return Response.status(500)
                .entity(error("Failed to delete treatment (it may still be referenced by appointments)")).build();
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
