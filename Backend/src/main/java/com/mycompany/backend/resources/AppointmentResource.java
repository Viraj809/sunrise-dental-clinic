package com.mycompany.backend.resources;

import Model.Appointment;
import DAO.AppointmentDAO;
import Validation.*;
import Service.AppointmentSubject;
import Service.EmailNotificationObserver;
import Service.SmsNotificationObserver;
import Service.StaffNotificationObserver;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Appointment REST Resource.
 * Design Patterns used:
 *   - Observer (AppointmentSubject + Observers) for notifications on create/update/cancel
 *   - Chain of Responsibility (Validators) for input validation
 */
@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppointmentResource {

    private final AppointmentDAO   dao     = new AppointmentDAO();
    private final AppointmentSubject subject = new AppointmentSubject();

    public AppointmentResource() {
        subject.attach(new EmailNotificationObserver());
        subject.attach(new SmsNotificationObserver());
        subject.attach(new StaffNotificationObserver());
    }

    // ── GET /appointments  ─────────────────────────────────────────────────
    @GET
    public Response getAll() {
        List<Appointment> list = dao.findAll();
        return Response.ok(list).build();
    }

    // ── GET /appointments/{id}  (by integer PK)  ──────────────────────────
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Appointment appt = dao.findById(id);
        if (appt == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }
        return Response.ok(appt).build();
    }

    // ── GET /appointments/no/{appointmentNo}  (by SDC-YYYY-NNNN) ──────────
    @GET
    @Path("/no/{appointmentNo}")
    public Response getByAppointmentNo(@PathParam("appointmentNo") String appointmentNo) {
        Appointment appt = dao.findByAppointmentNo(appointmentNo);
        if (appt == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }
        return Response.ok(appt).build();
    }

    // ── GET /appointments/patient/{patientId}  ────────────────────────────
    @GET
    @Path("/patient/{patientId}")
    public Response getByPatient(@PathParam("patientId") int patientId) {
        List<Appointment> list = dao.findByPatientId(patientId);
        return Response.ok(list).build();
    }

    // ── GET /appointments/dentist/{dentistId}/date/{date}  ────────────────
    @GET
    @Path("/dentist/{dentistId}/date/{date}")
    public Response getByDentistAndDate(@PathParam("dentistId") int dentistId,
                                        @PathParam("date") String date) {
        List<Appointment> list = dao.findByDentistIdAndDate(dentistId, date);
        return Response.ok(list).build();
    }

    // ── POST /appointments  ───────────────────────────────────────────────
    @POST
    public Response create(Appointment appointment) {
        String err = validate(appointment);
        if (err != null) {
            return Response.status(400).entity(error(err)).build();
        }
        appointment.setAppointmentNo(dao.getNextAppointmentNo());
        appointment.setStatus("Scheduled");
        if (dao.insert(appointment)) {
            notifyObservers("CREATED", appointment);
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to create appointment")).build();
    }

    // ── PUT /appointments/{id}  ───────────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Appointment appointment) {
        appointment.setAppointmentId(id);
        String err = validate(appointment);
        if (err != null) {
            return Response.status(400).entity(error(err)).build();
        }
        if (dao.update(appointment)) {
            notifyObservers("UPDATED", appointment);
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to update appointment")).build();
    }

    // ── PUT /appointments/{id}/status  ────────────────────────────────────
    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || !newStatus.matches("Scheduled|Confirmed|Completed|Cancelled")) {
            return Response.status(400).entity(error("Invalid status. Use: Scheduled, Confirmed, Completed, Cancelled")).build();
        }
        Appointment appt = dao.findById(id);
        if (appt == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }
        appt.setStatus(newStatus);
        if (dao.update(appt)) {
            if ("Cancelled".equals(newStatus)) {
                notifyObservers("CANCELLED", appt);
            }
            Map<String, Object> res = new HashMap<>();
            res.put("appointmentId", id);
            res.put("status", newStatus);
            res.put("message", "Status updated successfully");
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to update status")).build();
    }

    // ── DELETE /appointments/{id}  ────────────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        Appointment existing = dao.findById(id);
        if (dao.delete(id)) {
            if (existing != null) notifyObservers("CANCELLED", existing);
            return Response.ok(success("Appointment deleted")).build();
        }
        return Response.status(500).entity(error("Failed to delete appointment")).build();
    }

    // ── Private helpers ────────────────────────────────────────────────────
    private void notifyObservers(String eventType, Appointment appointment) {
        String message = "Appointment " + appointment.getAppointmentNo() + " - " + eventType;
        subject.notifyObservers(eventType, appointment.getAppointmentId(),
                "clinic@sunrisedental.com", message);
    }

    private String validate(Appointment appointment) {
        ContactNumberValidator contactValidator = new ContactNumberValidator();
        AppointmentDateValidator dateValidator  = new AppointmentDateValidator();
        ClinicHoursValidator hoursValidator     = new ClinicHoursValidator();
        DoubleBookingValidator bookingValidator  = new DoubleBookingValidator();

        contactValidator.setNext(dateValidator);
        dateValidator.setNext(hoursValidator);
        hoursValidator.setNext(bookingValidator);

        return contactValidator.validate(appointment, dao);
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
