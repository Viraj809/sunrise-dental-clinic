package service;

import dao.AppointmentDAO;
import dao.BillDAO;
import dao.DentistDAO;
import dao.PatientDAO;
import dao.TreatmentDAO;
import dbutil.DatabaseUtil;
import model.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReportFacade {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private BillDAO billDAO = new BillDAO();
    private TreatmentDAO treatmentDAO = new TreatmentDAO();
    private DentistDAO dentistDAO = new DentistDAO();
    private PatientDAO patientDAO = new PatientDAO();

    public Map<String, Object> getDailyAppointmentReport(String date) {
        HashMap<String, Object> report = new HashMap<String, Object>();
        ArrayList<Appointment> appts = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE appointment_date = ?";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appts.add(this.mapAppointment(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        report.put("date", date);
        report.put("appointments", appts);
        report.put("total", appts.size());
        return report;
    }

    public Map<String, Object> getRevenueReport(String period) {
        Object sql;
        HashMap<String, Object> report = new HashMap<String, Object>();
        if ("daily".equalsIgnoreCase(period)) {
            String today = LocalDate.now().toString();
            sql = "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE DATE(issued_at) = '" + today + "'";
        } else {
            sql = "monthly".equalsIgnoreCase(period) ? "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE MONTH(issued_at) = MONTH(CURDATE()) AND YEAR(issued_at) = YEAR(CURDATE())" : "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE YEAR(issued_at) = YEAR(CURDATE())";
        }
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery((String)sql);){
            if (rs.next()) {
                report.put("revenue", rs.getDouble("revenue"));
                report.put("count", rs.getInt("count"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        report.put("period", period);
        return report;
    }

    public Map<String, Object> getTreatmentPopularityReport() {
        HashMap<String, Object> report = new HashMap<String, Object>();
        String sql = "SELECT treatment_type, COUNT(*) AS count FROM appointments GROUP BY treatment_type ORDER BY count DESC";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                report.put(rs.getString("treatment_type"), rs.getInt("count"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getDentistWorkloadReport() {
        HashMap<String, Object> report = new HashMap<String, Object>();
        String sql = "SELECT d.name, COUNT(a.appointment_id) AS appt_count FROM dentists d LEFT JOIN appointments a ON d.dentist_id = a.dentist_id AND a.appointment_date >= CURDATE() - INTERVAL 30 DAY GROUP BY d.dentist_id, d.name ORDER BY appt_count DESC";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                report.put(rs.getString("name"), rs.getInt("appt_count"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getAppointmentStatusReport() {
        HashMap<String, Object> report = new HashMap<String, Object>();
        String sql = "SELECT status, COUNT(*) AS count FROM appointments GROUP BY status";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                report.put(rs.getString("status"), rs.getInt("count"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getPaymentReport() {
        HashMap<String, Object> report = new HashMap<String, Object>();
        String sql = "SELECT payment_method, COUNT(*) AS count, SUM(total_amount) AS total FROM bills GROUP BY payment_method";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                report.put(rs.getString("payment_method"), rs.getInt("count"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setAppointmentNo(rs.getString("appointment_no"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDentistId(rs.getInt("dentist_id"));
        a.setTreatmentType(rs.getString("treatment_type"));
        a.setAppointmentDate(rs.getString("appointment_date"));
        a.setAppointmentTime(rs.getString("appointment_time"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedBy(rs.getInt("created_by"));
        a.setCreatedAt(rs.getString("created_at"));
        a.setUpdatedAt(rs.getString("updated_at"));
        return a;
    }
}

