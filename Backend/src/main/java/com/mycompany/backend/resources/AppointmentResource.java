package com.mycompany.backend.resources;

import model.Appointment;
import model.Dentist;
import model.Notification;
import model.Staff;
import dao.AppointmentDAO;
import dao.DentistDAO;
import dao.NotificationDAO;
import dao.StaffDAO;
import validation.*;
import service.AppointmentSubject;
import service.EmailNotificationObserver;
import service.SmsNotificationObserver;
import service.StaffNotificationObserver;
import service.SecurityUtil;
import service.TokenManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Appointment REST Resource.
 * Design Patterns used:
 *   - Observer (AppointmentSubject + Observers) for notifications on create/update/cancel
 *   - Chain of Responsibility (Validators) for input validation
 *
 * Role-Based Access Control:
 *   - Admin / Receptionist: full management.
 *   - Dentist: only appointments assigned to them, and only status transitions.
 *   - Patient: only their own appointments; may create for themselves and cancel.
 */
@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppointmentResource {

    private final AppointmentDAO dao = new AppointmentDAO();
    private final NotificationDAO notificationDao = new NotificationDAO();
    private final AppointmentSubject subject = new AppointmentSubject();

    // System staff id used when a patient self-books (created_by FK -> staff).
    private static final int SYSTEM_STAFF_ID = 1;

    public AppointmentResource() {
        subject.attach(new EmailNotificationObserver());
        subject.attach(new SmsNotificationObserver());
        subject.attach(new StaffNotificationObserver());
    }

    @GET
    public Response getAll() {
        SecurityUtil.requireStaff();
        List<Appointment> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Appointment appt = dao.findById(id);
        if (appt == null) return Response.status(404).entity(error("Appointment not found")).build();
        enforceViewOwnership(appt);
        return Response.ok(appt).build();
    }

    @GET
    @Path("/no/{appointmentNo}")
    public Response getByAppointmentNo(@PathParam("appointmentNo") String appointmentNo) {
        Appointment appt = dao.findByAppointmentNo(appointmentNo);
        if (appt == null) return Response.status(404).entity(error("Appointment not found")).build();
        enforceViewOwnership(appt);
        return Response.ok(appt).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getByPatient(@PathParam("patientId") int patientId) {
        if (SecurityUtil.isPatient()) {
            if (patientId != SecurityUtil.currentId()) {
                return Response.status(403).entity(error("You can only view your own appointments")).build();
            }
        } else {
            SecurityUtil.requireStaff();
        }
        List<Appointment> list = dao.findByPatientId(patientId);
        return Response.ok(list).build();
    }

    @GET
    @Path("/dentist/{dentistId}/date/{date}")
    public Response getByDentistAndDate(@PathParam("dentistId") int dentistId,
                                        @PathParam("date") String date) {
        if ("DENTIST".equals(SecurityUtil.currentRole())) {
            int myDentistId = resolveDentistId(SecurityUtil.session());
            if (myDentistId > 0 && dentistId != myDentistId) {
                return Response.status(403).entity(error("You can only view your own schedule")).build();
            }
        } else {
            SecurityUtil.requireStaff();
        }
        List<Appointment> list = dao.findByDentistIdAndDate(dentistId, date);
        return Response.ok(list).build();
    }

    @GET
    @Path("/mine")
    public Response getMine() {
        SecurityUtil.requireStaff();
        TokenManager.Session session = SecurityUtil.session();
        int dentistId = resolveDentistId(session);
        if (dentistId <= 0) {
            return Response.status(403).entity(error("No dentist profile linked to this account")).build();
        }
        List<Appointment> list = dao.findByDentistId(dentistId);
        return Response.ok(list).build();
    }

    private int resolveDentistId(TokenManager.Session session) {
        if (!"DENTIST".equals(session.role)) return session.id;
        try {
            StaffDAO staffDao = new StaffDAO();
            model.Staff staff = staffDao.findById(session.id);
            if (staff == null || staff.getEmail() == null) return -1;
            DentistDAO dentistDao = new DentistDAO();
            model.Dentist d = dentistDao.findByEmail(staff.getEmail());
            if (d != null) return d.getDentistId();
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    @POST
    public Response create(Appointment appointment) {
        boolean isPatient = SecurityUtil.isPatient();
        if (isPatient) {
            appointment.setPatientId(SecurityUtil.currentId());
            appointment.setCreatedBy(SYSTEM_STAFF_ID);
        } else {
            SecurityUtil.requireReceptionOrAdmin();
            if (appointment.getCreatedBy() <= 0) appointment.setCreatedBy(SYSTEM_STAFF_ID);
        }

        String err = validate(appointment);
        if (err != null) return Response.status(400).entity(error(err)).build();

        appointment.setAppointmentNo(dao.getNextAppointmentNo());
        appointment.setStatus("Pending");
        if (appointment.getAppointmentType() == null || appointment.getAppointmentType().isEmpty()) {
            appointment.setAppointmentType("Consultation");
        }
        if (dao.insert(appointment)) {
            notifyObservers("CREATED", appointment);
            notifyPatient(appointment, "Appointment Booked",
                    "Your appointment " + appointment.getAppointmentNo() + " has been booked. Status: Pending.");
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to create appointment")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Appointment appointment) {
        Appointment existing = dao.findById(id);
        if (existing == null) return Response.status(404).entity(error("Appointment not found")).build();
        enforceViewOwnership(existing);

        appointment.setAppointmentId(id);
        String err = validate(appointment);
        if (err != null) return Response.status(400).entity(error(err)).build();

        if (dao.update(appointment)) {
            notifyObservers("UPDATED", appointment);
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to update appointment")).build();
    }

    @PUT
    @Path("/{id}/checkin")
    public Response checkIn(@PathParam("id") int id) {
        SecurityUtil.requireReceptionOrAdmin();
        Appointment appt = dao.findById(id);
        if (appt == null) return Response.status(404).entity(error("Appointment not found")).build();
        if (!"Scheduled".equals(appt.getStatus()) && !"Confirmed".equals(appt.getStatus()) && !"Pending".equals(appt.getStatus())) {
            return Response.status(400).entity(error("Cannot check in appointment with status: " + appt.getStatus())).build();
        }
        appt.setStatus("Checked In");
        if (dao.update(appt)) {
            notifyObservers("CHECKED_IN", appt);
            Map<String, Object> res = new HashMap<>();
            res.put("appointmentId", id);
            res.put("status", "Checked In");
            res.put("message", "Patient checked in successfully");
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to check in appointment")).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || !newStatus.matches(
                "Pending|Confirmed|Waiting|With Dentist|Treatment In Progress|Completed|Cancelled|Checked In")) {
            return Response.status(400).entity(error(
                    "Invalid status. Use: Pending, Scheduled, Confirmed, Waiting, With Dentist, Treatment In Progress, Completed, Cancelled, Checked In")).build();
        }
        Appointment appt = dao.findById(id);
        if (appt == null) return Response.status(404).entity(error("Appointment not found")).build();

        String role = SecurityUtil.currentRole();
        if ("PATIENT".equals(role)) {
            if (appt.getPatientId() != SecurityUtil.currentId()) {
                return Response.status(403).entity(error("You can only manage your own appointments")).build();
            }
            if (!"Cancelled".equals(newStatus)) {
                return Response.status(403).entity(error("Patients may only cancel their appointments")).build();
            }
        } else if ("DENTIST".equals(role)) {
            // à¶¸à·™à¶±à·Šà¶± à¶¸à·™à¶­à¶± à¶­à¶¸à¶ºà·’ à·€à·à¶»à·à¶¯à·Šà¶¯ à¶­à·’à¶¶à·”à¶«à·š. à¶¯à·à¶±à·Š à¶’à¶š resolveDentistId() à·„à¶»à·„à· à¶±à·’à·€à·à¶»à¶¯à·’ à¶šà¶»à¶½à· à¶­à·’à¶ºà·™à¶±à·Šà¶±à·š.
            int myDentistId = resolveDentistId(SecurityUtil.session());
            if (appt.getDentistId() != myDentistId) {
                return Response.status(403).entity(error("You can only manage your own appointments")).build();
            }
        } else {
            SecurityUtil.requireStaff();
        }

        appt.setStatus(newStatus);
        if (dao.update(appt)) {
            if ("Cancelled".equals(newStatus)) {
                notifyObservers("CANCELLED", appt);
                notifyPatient(appt, "Appointment Cancelled",
                        "Your appointment " + appt.getAppointmentNo() + " has been cancelled.");
            }
            Map<String, Object> res = new HashMap<>();
            res.put("appointmentId", id);
            res.put("status", newStatus);
            res.put("message", "Status updated successfully");
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to update status")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Appointment existing = dao.findById(id);
        if (dao.delete(id)) {
            if (existing != null) notifyObservers("CANCELLED", existing);
            return Response.ok(success("Appointment deleted")).build();
        }
        return Response.status(500).entity(error("Failed to delete appointment")).build();
    }

    // â”€â”€ Private helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void enforceViewOwnership(Appointment appt) {
        if (SecurityUtil.isPatient()) {
            if (appt.getPatientId() != SecurityUtil.currentId()) {
                throw new jakarta.ws.rs.ForbiddenException("You can only view your own appointments");
            }
        } else {
            SecurityUtil.requireStaff();
        }
    }

    private void notifyPatient(Appointment appointment, String title, String message) {
        try {
            Notification n = new Notification();
            n.setUserId(appointment.getPatientId());
            n.setTitle(title);
            n.setChannel("IN_APP");
            n.setRecipient("");
            n.setNotificationType("APPOINTMENT");
            n.setMessage(message);
            n.setRead(false);
            n.setStatus("SENT");
            notificationDao.insert(n);
        } catch (Exception ignored) {}
    }

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