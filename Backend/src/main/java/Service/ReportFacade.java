package service;

import dao.AppointmentDAO;
import dao.BillDAO;
import dao.DentistDAO;
import dao.PatientDAO;
import dbutil.DatabaseUtil;
import model.Appointment;
import model.Bill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFacade {
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private BillDAO billDAO = new BillDAO();
    private DentistDAO dentistDAO = new DentistDAO();
    private PatientDAO patientDAO = new PatientDAO();

    public Map<String, Object> getSummaryCards() {
        HashMap<String, Object> result = new HashMap<>();
        String today = LocalDate.now().toString();
        String currentMonth = today.substring(0, 7);

        try {
            List<Appointment> allAppts = appointmentDAO.findAll();
            List<Bill> allBills = billDAO.findAll();

            long todayAppts = allAppts.stream().filter(a -> today.equals(a.getAppointmentDate())).count();

            double todayRevenue = allBills.stream()
                    .filter(b -> b.getIssuedAt() != null && b.getIssuedAt().startsWith(today))
                    .mapToDouble(b -> b.getTotalAmount())
                    .sum();

            double monthRevenue = allBills.stream()
                    .filter(b -> b.getIssuedAt() != null && b.getIssuedAt().startsWith(currentMonth))
                    .mapToDouble(b -> b.getTotalAmount())
                    .sum();

            long pendingBills = allBills.stream().filter(b -> "PENDING".equals(b.getPaymentStatus())).count();

            double totalAmount = allBills.stream()
                    .mapToDouble(b -> b.getTotalAmount())
                    .sum();

            result.put("todayAppointments", todayAppts);
            result.put("todayRevenue", todayRevenue);
            result.put("monthlyRevenue", monthRevenue);
            result.put("pendingBills", pendingBills);
            result.put("totalAmount", totalAmount);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("todayAppointments", 0);
            result.put("todayRevenue", 0);
            result.put("monthlyRevenue", 0);
            result.put("pendingBills", 0);
            result.put("totalAmount", 0);
        }
        return result;
    }

    public Map<String, Object> getDailyReport(String date) {
        HashMap<String, Object> report = new HashMap<>();
        List<Appointment> appts = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE appointment_date = ?";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appts.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Bill> dayBills = new ArrayList<>();
        String billSql = "SELECT * FROM bills WHERE DATE(issued_at) = ?";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(billSql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bill b = new Bill();
                b.setBillId(rs.getInt("bill_id"));
                b.setAppointmentId(rs.getInt("appointment_id"));
                b.setConsultationFee(rs.getDouble("consultation_fee"));
                b.setTreatmentFee(rs.getDouble("treatment_fee"));
                b.setDiscount(rs.getDouble("discount"));
                b.setTax(rs.getDouble("tax"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setPaymentStatus(rs.getString("payment_status"));
                b.setIssuedBy(rs.getInt("issued_by"));
                b.setIssuedAt(rs.getString("issued_at"));
                dayBills.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int completed = (int) appts.stream().filter(a -> "Completed".equals(a.getStatus())).count();
        int cancelled = (int) appts.stream().filter(a -> "Cancelled".equals(a.getStatus())).count();
        int pending = appts.size() - completed - cancelled;

        double revenue = dayBills.stream().mapToDouble(b -> b.getTotalAmount()).sum();
        double paidAmount = dayBills.stream().filter(b -> "PAID".equals(b.getPaymentStatus())).mapToDouble(b -> b.getTotalAmount()).sum();
        double pendingAmount = dayBills.stream().filter(b -> "PENDING".equals(b.getPaymentStatus())).mapToDouble(b -> b.getTotalAmount()).sum();

        report.put("date", date);
        report.put("appointmentCount", appts.size());
        report.put("completed", completed);
        report.put("cancelled", cancelled);
        report.put("pending", pending);
        report.put("revenue", revenue);
        report.put("paidAmount", paidAmount);
        report.put("pendingAmount", pendingAmount);
        return report;
    }

    public Map<String, Object> getMonthlyReport(int year, int month) {
        HashMap<String, Object> report = new HashMap<>();
        List<Appointment> allAppts = new ArrayList<>();
        String sql = "SELECT * FROM appointments";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                allAppts.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Appointment> monthAppts = new ArrayList<>();
        for (Appointment a : allAppts) {
            if (a.getAppointmentDate() != null) {
                String[] parts = a.getAppointmentDate().split("-");
                if (parts.length == 3) {
                    int ay = Integer.parseInt(parts[0]);
                    int am = Integer.parseInt(parts[1]);
                    if (ay == year && am == month) {
                        monthAppts.add(a);
                    }
                }
            }
        }

        List<Bill> allBills = new ArrayList<>();
        String billSql = "SELECT * FROM bills";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(billSql)) {
            while (rs.next()) {
                Bill b = new Bill();
                b.setBillId(rs.getInt("bill_id"));
                b.setAppointmentId(rs.getInt("appointment_id"));
                b.setConsultationFee(rs.getDouble("consultation_fee"));
                b.setTreatmentFee(rs.getDouble("treatment_fee"));
                b.setDiscount(rs.getDouble("discount"));
                b.setTax(rs.getDouble("tax"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setPaymentStatus(rs.getString("payment_status"));
                b.setIssuedBy(rs.getInt("issued_by"));
                b.setIssuedAt(rs.getString("issued_at"));
                allBills.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Bill> monthBills = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
        for (Bill b : allBills) {
            if (b.getIssuedAt() != null) {
                try {
                    java.util.Date issued = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(b.getIssuedAt().replace('T', ' '));
                    String ym = sdf.format(issued);
                    String target = String.format("%04d-%02d", year, month);
                    if (ym.equals(target)) {
                        monthBills.add(b);
                    }
                } catch (Exception ignored) {}
            }
        }

        int completed = (int) monthAppts.stream().filter(a -> "Completed".equals(a.getStatus())).count();
        int cancelled = (int) monthAppts.stream().filter(a -> "Cancelled".equals(a.getStatus())).count();

        double revenue = monthBills.stream().mapToDouble(b -> b.getTotalAmount()).sum();
        double paidAmount = monthBills.stream().filter(b -> "PAID".equals(b.getPaymentStatus())).mapToDouble(b -> b.getTotalAmount()).sum();
        long pendingBills = monthBills.stream().filter(b -> "PENDING".equals(b.getPaymentStatus())).count();
        double totalAmount = monthBills.stream().mapToDouble(b -> b.getTotalAmount()).sum();

        String monthName = new java.text.DateFormatSymbols().getMonths()[month - 1] + " " + year;

        report.put("month", monthName);
        report.put("totalAppointments", monthAppts.size());
        report.put("completed", completed);
        report.put("cancelled", cancelled);
        report.put("revenue", revenue);
        report.put("paidAmount", paidAmount);
        report.put("pendingBills", pendingBills);
        report.put("totalAmount", totalAmount);
        return report;
    }

    public Map<String, Object> getDailyAppointmentReport(String date) {
        HashMap<String, Object> report = new HashMap<>();
        ArrayList<Appointment> appts = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE appointment_date = ?";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appts.add(this.mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        report.put("date", date);
        report.put("appointments", appts);
        report.put("total", appts.size());
        return report;
    }

    public Map<String, Object> getRevenueReport(String period) {
        Object sql;
        HashMap<String, Object> report = new HashMap<>();
        if ("daily".equalsIgnoreCase(period)) {
            String today = LocalDate.now().toString();
            sql = "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE DATE(issued_at) = '" + today + "'";
        } else {
            sql = "monthly".equalsIgnoreCase(period) ? "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE MONTH(issued_at) = MONTH(CURDATE()) AND YEAR(issued_at) = YEAR(CURDATE())" : "SELECT SUM(total_amount) AS revenue, COUNT(*) AS count FROM bills WHERE YEAR(issued_at) = YEAR(CURDATE())";
        }
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery((String) sql)) {
            if (rs.next()) {
                report.put("revenue", rs.getDouble("revenue"));
                report.put("count", rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        report.put("period", period);
        return report;
    }

    public Map<String, Object> getTreatmentPopularityReport() {
        HashMap<String, Object> report = new HashMap<>();
        String sql = "SELECT treatment_type, COUNT(*) AS count FROM appointments GROUP BY treatment_type ORDER BY count DESC";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                report.put(rs.getString("treatment_type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getDentistWorkloadReport() {
        HashMap<String, Object> report = new HashMap<>();
        String sql = "SELECT d.name, COUNT(a.appointment_id) AS appt_count FROM dentists d LEFT JOIN appointments a ON d.dentist_id = a.dentist_id AND a.appointment_date >= CURDATE() - INTERVAL 30 DAY GROUP BY d.dentist_id, d.name ORDER BY appt_count DESC";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                report.put(rs.getString("name"), rs.getInt("appt_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getAppointmentStatusReport() {
        HashMap<String, Object> report = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM appointments GROUP BY status";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                report.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public Map<String, Object> getPaymentReport() {
        HashMap<String, Object> report = new HashMap<>();
        String sql = "SELECT payment_method, COUNT(*) AS count, SUM(total_amount) AS total FROM bills GROUP BY payment_method";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                report.put(rs.getString("payment_method"), rs.getInt("count"));
            }
        } catch (SQLException e) {
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
