package com.mycompany.backend.resources;

import Service.ReportFacade;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportResource {
    private ReportFacade facade = new ReportFacade();

    @GET
    @Path("/daily/{date}")
    public Response getDaily(@PathParam("date") String date) {
        Map<String, Object> report = facade.getDailyAppointmentReport(date);
        return Response.ok(report).build();
    }

    @GET
    @Path("/revenue/{period}")
    public Response getRevenue(@PathParam("period") String period) {
        Map<String, Object> report = facade.getRevenueReport(period);
        return Response.ok(report).build();
    }

    @GET
    @Path("/treatment-popularity")
    public Response getTreatmentPopularity() {
        Map<String, Object> report = facade.getTreatmentPopularityReport();
        return Response.ok(report).build();
    }

    @GET
    @Path("/dentist-workload")
    public Response getDentistWorkload() {
        Map<String, Object> report = facade.getDentistWorkloadReport();
        return Response.ok(report).build();
    }
}
