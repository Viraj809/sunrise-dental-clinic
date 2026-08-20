package com.mycompany.backend.resources;

import DAO.AuditLogDAO;
import Service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditResource {

    private final AuditLogDAO dao = new AuditLogDAO();

    @GET
    public Response list(@QueryParam("limit") @DefaultValue("200") int limit) {
        SecurityUtil.requireAdmin();
        return Response.ok(dao.findAll(limit)).build();
    }
}
