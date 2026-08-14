package com.mycompany.backend.resources;

import Model.Bill;
import DAO.BillDAO;
import Service.BillFactory;
import Service.BillCalculationStrategy;
import DAO.PatientDAO;
import DAO.TreatmentDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/bills")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BillResource {
    private BillDAO billDao = new BillDAO();
    private PatientDAO patientDao = new PatientDAO();
    private TreatmentDAO treatmentDao = new TreatmentDAO();

    @POST
    @Path("/generate")
    public Response generateBill(Map<String, Integer> request) {
        int appointmentId = request.get("appointment_id");
        int patientId = request.get("patient_id");

        BillCalculationStrategy strategy = BillFactory.createBill(patientId, patientDao);
        Model.Treatment treatment = treatmentDao.findByCode("CHKUP");
        if (treatment == null) {
            return Response.status(500).entity("{\"error\":\"Treatment not found\"}").build();
        }

        int patientAge = 0;
        Model.Patient patient = patientDao.findById(patientId);
        if (patient != null && patient.getDateOfBirth() != null) {
            patientAge = java.time.Period.between(
                    java.time.LocalDate.parse(patient.getDateOfBirth()),
                    java.time.LocalDate.now()
            ).getYears();
        }

        double treatmentFee = strategy.calculateTreatmentFee(treatment.getBasePrice(), patientAge);
        double consultationFee = treatment.getConsultationFee();
        double discount = 0.0;
        double tax = Math.round((treatmentFee + consultationFee) * 0.05 * 100.0) / 100.0;
        double total = Math.round((consultationFee + treatmentFee - discount + tax) * 100.0) / 100.0;

        Bill bill = new Bill();
        bill.setAppointmentId(appointmentId);
        bill.setConsultationFee(consultationFee);
        bill.setTreatmentFee(treatmentFee);
        bill.setDiscount(discount);
        bill.setTax(tax);
        bill.setTotalAmount(total);
        bill.setPaymentMethod("CASH");
        bill.setPaymentStatus("PENDING");
        bill.setIssuedBy(1);

        if (billDao.insert(bill)) {
            return Response.ok(bill).build();
        }
        return Response.status(500).entity("{\"error\":\"Failed to generate bill\"}").build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        Bill bill = billDao.findByAppointmentId(id);
        if (bill == null) {
            return Response.status(404).entity("{\"error\":\"Bill not found\"}").build();
        }
        return Response.ok(bill).build();
    }
}
