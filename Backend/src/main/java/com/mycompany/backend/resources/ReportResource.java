package com.mycompany.backend.resources;

import service.ReportFacade;
import service.SecurityUtil;
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
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getDailyAppointmentReport(date);
        return Response.ok(report).build();
    }

    @GET
    @Path("/revenue/{period}")
    public Response getRevenue(@PathParam("period") String period) {
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getRevenueReport(period);
        return Response.ok(report).build();
    }

    @GET
    @Path("/treatment-popularity")
    public Response getTreatmentPopularity() {
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getTreatmentPopularityReport();
        return Response.ok(report).build();
    }

    @GET
    @Path("/dentist-workload")
    public Response getDentistWorkload() {
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getDentistWorkloadReport();
        return Response.ok(report).build();
    }

    @GET
    @Path("/appointment-status")
    public Response getAppointmentStatus() {
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getAppointmentStatusReport();
        return Response.ok(report).build();
    }

    @GET
    @Path("/payment-report")
    public Response getPaymentReport() {
        SecurityUtil.requireAdmin();
        Map<String, Object> report = facade.getPaymentReport();
        return Response.ok(report).build();
    }
}
