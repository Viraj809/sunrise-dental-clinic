package com.mycompany.backend.resources;

import Model.Dentist;
import DAO.DentistDAO;
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
        if (dentist.getName() == null || dentist.getName().isEmpty()) {
            return Response.status(400).entity(error("Name is required")).build();
        }
        dentist.setActive(true);
        if (dao.insert(dentist)) {
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to add dentist")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Dentist dentist) {
        dentist.setDentistId(id);
        if (dao.update(dentist)) {
            return Response.ok(dentist).build();
        }
        return Response.status(500).entity(error("Failed to update dentist")).build();
    }

    @PUT
    @Path("/{id}/toggle")
    public Response toggleActive(@PathParam("id") int id) {
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
