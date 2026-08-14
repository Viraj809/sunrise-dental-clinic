package com.mycompany.backend.resources;

import Model.Treatment;
import DAO.TreatmentDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
}
