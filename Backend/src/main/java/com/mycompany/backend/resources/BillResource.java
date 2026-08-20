package com.mycompany.backend.resources;

import Model.Bill;
import Model.Appointment;
import Model.Patient;
import Model.Dentist;
import Model.Treatment;
import DAO.BillDAO;
import DAO.AppointmentDAO;
import DAO.PatientDAO;
import DAO.TreatmentDAO;
import DAO.DentistDAO;
import Service.BillFactory;
import Service.BillCalculationStrategy;
import Service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Bill REST Resource.
 * Design Patterns used:
 *   - Factory (BillFactory) to select billing strategy
 *   - Strategy (BillCalculationStrategy) for senior/standard pricing
 *
 * RBAC:
 *   - Listing / receipt viewing: admin, receptionist, dentist (patient views own only).
 *   - Generating a bill: admin, receptionist, dentist (dentist generates after treatment).
 *   - Recording payment: admin / receptionist ONLY (dentists do not collect payment).
 */
@Path("/bills")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BillResource {

    private final BillDAO        billDao        = new BillDAO();
    private final AppointmentDAO appointmentDao = new AppointmentDAO();
    private final PatientDAO     patientDao     = new PatientDAO();
    private final TreatmentDAO   treatmentDao   = new TreatmentDAO();
    private final DentistDAO     dentistDao     = new DentistDAO();

    // ── GET /bills  ────────────────────────────────────────────────────────
    @GET
    public Response getAll() {
        SecurityUtil.requireReceptionOrAdmin();
        List<Bill> bills = billDao.findAll();
        return Response.ok(bills).build();
    }

    // ── GET /bills/appointment/{appointmentId}  ────────────────────────────
    @GET
    @Path("/appointment/{appointmentId}")
    public Response getByAppointmentId(@PathParam("appointmentId") int appointmentId) {
        enforceBillAccess(appointmentId);
        Bill bill = billDao.findByAppointmentId(appointmentId);
        if (bill == null) {
            return Response.status(404).entity(error("Bill not found for appointment " + appointmentId)).build();
        }
        return Response.ok(bill).build();
    }

    // ── GET /bills/{billId}  ───────────────────────────────────────────────
    @GET
    @Path("/{billId}")
    public Response getByBillId(@PathParam("billId") int billId) {
        SecurityUtil.requireReceptionOrAdmin();
        Bill bill = billDao.findAll().stream()
                .filter(b -> b.getBillId() == billId)
                .findFirst().orElse(null);
        if (bill == null) {
            return Response.status(404).entity(error("Bill not found")).build();
        }
        return Response.ok(bill).build();
    }

    // ── GET /bills/{billId}/receipt  ──────────────────────────────────────
    @GET
    @Path("/{billId}/receipt")
    public Response getReceipt(@PathParam("billId") int billId) {
        SecurityUtil.requireReceptionOrAdmin();
        Bill bill = billDao.findAll().stream()
                .filter(b -> b.getBillId() == billId)
                .findFirst().orElse(null);
        if (bill == null) {
            return Response.status(404).entity(error("Bill not found")).build();
        }
        Map<String, Object> receipt = buildReceipt(bill);
        return Response.ok(receipt).build();
    }

    // ── GET /bills/appointment/{appointmentId}/receipt  ───────────────────
    @GET
    @Path("/appointment/{appointmentId}/receipt")
    public Response getReceiptByAppointment(@PathParam("appointmentId") int appointmentId) {
        enforceBillAccess(appointmentId);
        Bill bill = billDao.findByAppointmentId(appointmentId);
        if (bill == null) {
            return Response.status(404).entity(error("Bill not found for this appointment")).build();
        }
        return Response.ok(buildReceipt(bill)).build();
    }

    // ── POST /bills/generate  ─────────────────────────────────────────────
    @POST
    @Path("/generate")
    public Response generateBill(Map<String, Integer> request) {
        SecurityUtil.requireReceptionOrAdmin();
        Integer appointmentId = request.get("appointment_id");
        if (appointmentId == null) {
            return Response.status(400).entity(error("appointment_id is required")).build();
        }

        // Check if bill already exists
        Bill existing = billDao.findByAppointmentId(appointmentId);
        if (existing != null) {
            return Response.ok(buildReceipt(existing)).build();
        }

        Appointment appointment = appointmentDao.findById(appointmentId);
        if (appointment == null) {
            return Response.status(404).entity(error("Appointment not found")).build();
        }

        String treatmentCode = appointment.getTreatmentType();
        Treatment treatment  = treatmentDao.findByCode(treatmentCode);
        if (treatment == null) {
            return Response.status(400).entity(error("Treatment not found: " + treatmentCode)).build();
        }

        Patient patient = patientDao.findById(appointment.getPatientId());
        int patientAge = 0;
        if (patient != null && patient.getDateOfBirth() != null && !patient.getDateOfBirth().isEmpty()) {
            try {
                patientAge = java.time.Period.between(
                        java.time.LocalDate.parse(patient.getDateOfBirth()),
                        java.time.LocalDate.now()).getYears();
            } catch (Exception ignored) {}
        }

        // The Strategy returns the treatment fee ALREADY adjusted for the senior
        // discount (StandardBillStrategy = full price, SeniorBillStrategy = 10% off).
        // Therefore the discount column is purely informational (base - charged fee)
        // and must NOT be subtracted again from the total.
        double basePrice        = treatment.getBasePrice();
        BillCalculationStrategy strategy = BillFactory.createBill(appointment.getPatientId(), patientDao);
        double treatmentFee    = strategy.calculateTreatmentFee(basePrice, patientAge);
        double consultationFee = treatment.getConsultationFee();
        double discount = Math.round((basePrice - treatmentFee) * 100.0) / 100.0;
        double tax      = Math.round((treatmentFee + consultationFee) * 0.05 * 100.0) / 100.0;
        double total    = Math.round((consultationFee + treatmentFee + tax) * 100.0) / 100.0;

        String issuedByStr = request.containsKey("issued_by") ? String.valueOf(request.get("issued_by")) : "1";

        Bill bill = new Bill();
        bill.setAppointmentId(appointmentId);
        bill.setConsultationFee(consultationFee);
        bill.setTreatmentFee(treatmentFee);
        bill.setDiscount(discount);
        bill.setTax(tax);
        bill.setTotalAmount(total);
        bill.setPaymentMethod("CASH");
        bill.setPaymentStatus("PENDING");
        bill.setIssuedBy(Integer.parseInt(issuedByStr));

        if (billDao.insert(bill)) {
            Bill saved = billDao.findByAppointmentId(appointmentId);
            return Response.ok(buildReceipt(saved != null ? saved : bill)).build();
        }
        return Response.status(500).entity(error("Failed to generate bill")).build();
    }

    // ── PUT /bills/{billId}/payment  ──────────────────────────────────────
    @PUT
    @Path("/{billId}/payment")
    public Response updatePayment(@PathParam("billId") int billId, Map<String, String> body) {
        // Only receptionist / admin collect payments (dentist does NOT).
        SecurityUtil.requireReceptionOrAdmin();
        String paymentMethod = body.getOrDefault("payment_method", "CASH");
        String paymentStatus = body.getOrDefault("payment_status", "PAID");

        boolean ok = billDao.updatePaymentDetails(billId, paymentMethod, paymentStatus);
        if (ok) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Payment updated");
            res.put("billId", billId);
            res.put("paymentStatus", paymentStatus);
            res.put("paymentMethod", paymentMethod);
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to update payment")).build();
    }

    // ── RBAC helper: patients may only access bills for their own appointments ─
    private void enforceBillAccess(int appointmentId) {
        if (!SecurityUtil.isPatient()) {
            SecurityUtil.requireReceptionOrAdmin();
            return;
        }
        Appointment appt = appointmentDao.findById(appointmentId);
        if (appt == null || appt.getPatientId() != SecurityUtil.currentId()) {
            throw new jakarta.ws.rs.ForbiddenException("You can only view your own bills");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private Map<String, Object> buildReceipt(Bill bill) {
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("billId",          bill.getBillId());
        receipt.put("appointmentId",   bill.getAppointmentId());
        receipt.put("consultationFee", bill.getConsultationFee());
        receipt.put("treatmentFee",    bill.getTreatmentFee());
        receipt.put("discount",        bill.getDiscount());
        receipt.put("tax",             bill.getTax());
        receipt.put("totalAmount",     bill.getTotalAmount());
        receipt.put("paymentMethod",   bill.getPaymentMethod());
        receipt.put("paymentStatus",   bill.getPaymentStatus());
        receipt.put("issuedAt",        bill.getIssuedAt());
        receipt.put("issuedBy",        bill.getIssuedBy());

        Appointment appt = appointmentDao.findById(bill.getAppointmentId());
        if (appt != null) {
            receipt.put("appointmentNo",   appt.getAppointmentNo());
            receipt.put("appointmentDate", appt.getAppointmentDate());
            receipt.put("appointmentTime", appt.getAppointmentTime());
            receipt.put("treatmentCode",   appt.getTreatmentType());

            Patient p = patientDao.findById(appt.getPatientId());
            if (p != null) {
                receipt.put("patientName",    p.getName());
                receipt.put("patientContact", p.getContact());
                receipt.put("patientAddress", p.getAddress());
            }

            Dentist d = dentistDao.findById(appt.getDentistId());
            if (d != null) {
                receipt.put("dentistName",           d.getName());
                receipt.put("dentistSpecialization", d.getSpecialization());
            }

            Treatment t = treatmentDao.findByCode(appt.getTreatmentType());
            if (t != null) {
                receipt.put("treatmentName", t.getTreatmentName());
            }
        }
        return receipt;
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}
