package com.mycompany.backend.resources;

import Model.PatientQueue;
import DAO.PatientQueueDAO;
import DAO.PatientDAO;
import DAO.DentistDAO;
import Model.Patient;
import Model.Dentist;
import Service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Patient Queue REST Resource (managed by receptionist / admin).
 * Enforces RBAC at the resource level: only ADMIN and RECEPTIONIST may manage
 * the queue. Responses are enriched with patient and dentist names.
 */
@Path("/queue")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueueResource {

    private final PatientQueueDAO dao = new PatientQueueDAO();
    private final PatientDAO patientDao = new PatientDAO();
    private final DentistDAO dentistDao = new DentistDAO();

    @GET
    public Response list() {
        SecurityUtil.requireReceptionOrAdmin();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PatientQueue q : dao.findActive()) {
            result.add(enrich(q));
        }
        return Response.ok(result).build();
    }

    @POST
    public Response add(Map<String, Object> body) {
        SecurityUtil.requireReceptionOrAdmin();
        Object patientObj = body.get("patient_id");
        Object dentistObj = body.get("dentist_id");
        if (patientObj == null || dentistObj == null) {
            return Response.status(400).entity(error("patient_id and dentist_id are required")).build();
        }
        int patientId = Integer.parseInt(patientObj.toString());
        int dentistId = Integer.parseInt(dentistObj.toString());

        PatientQueue q = new PatientQueue();
        q.setQueueNumber(dao.getNextQueueNumber());
        q.setPatientId(patientId);
        q.setDentistId(dentistId);
        q.setAppointmentId(body.get("appointment_id") != null ? Integer.parseInt(body.get("appointment_id").toString()) : 0);
        q.setAppointmentTime(body.get("appointment_time") != null ? body.get("appointment_time").toString() : null);
        q.setStatus("Waiting");

        if (dao.insert(q)) {
            return Response.ok(enrich(dao.findById(q.getQueueId()))).build();
        }
        return Response.status(500).entity(error("Failed to add patient to queue")).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, Map<String, String> body) {
        SecurityUtil.requireReceptionOrAdmin();
        String status = body.get("status");
        if (status == null || !status.matches("Waiting|With Dentist|Completed")) {
            return Response.status(400).entity(error("Invalid status")).build();
        }
        if (dao.updateStatus(id, status)) {
            Map<String, Object> res = new HashMap<>();
            res.put("queueId", id);
            res.put("status", status);
            res.put("message", "Queue status updated");
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to update queue")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") int id) {
        SecurityUtil.requireReceptionOrAdmin();
        if (dao.delete(id)) return Response.ok(success("Removed from queue")).build();
        return Response.status(500).entity(error("Failed to remove from queue")).build();
    }

    private Map<String, Object> enrich(PatientQueue q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("queueId", q.getQueueId());
        m.put("queueNumber", q.getQueueNumber());
        m.put("appointmentId", q.getAppointmentId());
        m.put("patientId", q.getPatientId());
        m.put("dentistId", q.getDentistId());
        m.put("appointmentTime", q.getAppointmentTime());
        m.put("status", q.getStatus());
        m.put("createdAt", q.getCreatedAt());
        Patient p = patientDao.findById(q.getPatientId());
        if (p != null) m.put("patientName", p.getName());
        Dentist d = dentistDao.findById(q.getDentistId());
        if (d != null) m.put("dentistName", d.getName());
        return m;
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
