package com.mycompany.backend.resources;

import model.DentistSchedule;
import dao.DentistScheduleDAO;
import service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Dentist Schedule REST Resource.
 * Admin manages every dentist's weekly working hours; a dentist may manage only
 * their own. Used by "Manage Dentist Working Hours" (admin) and "My Schedule"
 * (dentist).
 */
@Path("/schedule")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduleResource {

    private final DentistScheduleDAO dao = new DentistScheduleDAO();

    @GET
    @Path("/dentist/{dentistId}")
    public Response getByDentist(@PathParam("dentistId") int dentistId) {
        SecurityUtil.requireStaff();
        return Response.ok(dao.findByDentist(dentistId)).build();
    }

    @POST
    public Response upsert(Map<String, String> body) {
        int dentistId = parseInt(body.get("dentist_id"));
        if (dentistId <= 0) return Response.status(400).entity(error("dentist_id is required")).build();

        // Admin may manage anyone; a dentist may only manage their own schedule.
        if ("DENTIST".equals(SecurityUtil.currentRole())) {
            if (dentistId != SecurityUtil.currentId()) {
                return Response.status(403).entity(error("You can only manage your own schedule")).build();
            }
        } else {
            SecurityUtil.requireAdmin();
        }

        String day = body.get("day_of_week");
        if (day == null || day.isEmpty()) return Response.status(400).entity(error("day_of_week is required")).build();

        DentistSchedule s = new DentistSchedule();
        s.setDentistId(dentistId);
        s.setDayOfWeek(day);
        s.setStartTime(body.getOrDefault("start_time", "09:00:00"));
        s.setEndTime(body.getOrDefault("end_time", "17:00:00"));
        s.setAvailabilityStatus(body.getOrDefault("availability_status", "Available"));
        s.setUnavailableDate(body.get("unavailable_date"));

        if (dao.upsert(s)) return Response.ok(success("Schedule saved")).build();
        return Response.status(500).entity(error("Failed to save schedule")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        if (dao.delete(id)) return Response.ok(success("Schedule removed")).build();
        return Response.status(500).entity(error("Failed to remove schedule")).build();
    }

    private int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s); } catch (Exception e) { return 0; }
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
