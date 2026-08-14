package com.mycompany.backend.resources;

import Model.Appointment;
import DAO.AppointmentDAO;
import Validation.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AppointmentResource {
    private AppointmentDAO dao = new AppointmentDAO();

    @GET
    public Response getAll() {
        List<Appointment> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Appointment appt = dao.findByAppointmentNo(String.valueOf(id));
        if (appt == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }
        return Response.ok(appt).build();
    }

    @GET
    @Path("/no/{appointmentNo}")
    public Response getByAppointmentNo(@PathParam("appointmentNo") String appointmentNo) {
        Appointment appt = dao.findByAppointmentNo(appointmentNo);
        if (appt == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }
        return Response.ok(appt).build();
    }

    @POST
    public Response create(Appointment appointment) {
        String error = validate(appointment);
        if (error != null) {
            return Response.status(400).entity(error(error)).build();
        }
        appointment.setAppointmentNo(dao.getNextAppointmentNo());
        appointment.setStatus("Scheduled");
        if (dao.insert(appointment)) {
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to create appointment")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Appointment appointment) {
        appointment.setAppointmentId(id);
        String error = validate(appointment);
        if (error != null) {
            return Response.status(400).entity(error(error)).build();
        }
        if (dao.update(appointment)) {
            return Response.ok(appointment).build();
        }
        return Response.status(500).entity(error("Failed to update appointment")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        if (dao.delete(id)) {
            return Response.ok().entity(success("Appointment deleted")).build();
        }
        return Response.status(500).entity(error("Failed to delete appointment")).build();
    }

    private String validate(Appointment appointment) {
        ContactNumberValidator contactValidator = new ContactNumberValidator();
        AppointmentDateValidator dateValidator = new AppointmentDateValidator();
        ClinicHoursValidator hoursValidator = new ClinicHoursValidator();
        DoubleBookingValidator bookingValidator = new DoubleBookingValidator();

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
