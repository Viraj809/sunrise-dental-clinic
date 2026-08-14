package com.mycompany.backend.resources;

import Model.Dentist;
import DAO.DentistDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/dentists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DentistResource {
    private DentistDAO dao = new DentistDAO();

    @GET
    public Response getAll() {
        List<Dentist> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Dentist d = dao.findById(id);
        if (d == null) {
            return Response.status(404).entity("{\"error\":\"Dentist not found\"}").build();
        }
        return Response.ok(d).build();
    }
}
