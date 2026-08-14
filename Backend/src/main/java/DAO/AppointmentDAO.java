package DAO;

import Model.Appointment;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private DatabaseUtil db;

    public AppointmentDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public Appointment findByAppointmentNo(String appointmentNo) {
        String sql = "SELECT * FROM appointments WHERE appointment_no = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapAppointment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Appointment> findByPatientId(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> findByDentistIdAndDate(int dentistId, String date) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE dentist_id = ? AND appointment_date = ? AND status NOT IN ('Cancelled') ORDER BY appointment_time";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date DESC, appointment_time DESC";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_type, appointment_date, appointment_time, status, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointment.getAppointmentNo());
            ps.setInt(2, appointment.getPatientId());
            ps.setInt(3, appointment.getDentistId());
            ps.setString(4, appointment.getTreatmentType());
            ps.setString(5, appointment.getAppointmentDate());
            ps.setString(6, appointment.getAppointmentTime());
            ps.setString(7, appointment.getStatus());
            ps.setString(8, appointment.getNotes());
            ps.setInt(9, appointment.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id=?, dentist_id=?, treatment_type=?, appointment_date=?, appointment_time=?, status=?, notes=?, updated_at=NOW() WHERE appointment_id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDentistId());
            ps.setString(3, appointment.getTreatmentType());
            ps.setString(4, appointment.getAppointmentDate());
            ps.setString(5, appointment.getAppointmentTime());
            ps.setString(6, appointment.getStatus());
            ps.setString(7, appointment.getNotes());
            ps.setInt(8, appointment.getAppointmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNextAppointmentNo() {
        String sql = "SELECT COUNT(*) + 1 AS next_seq FROM appointments WHERE YEAR(created_at) = YEAR(CURDATE())";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int seq = rs.getInt("next_seq");
                return String.format("SDC-%04d-%04d", Integer.parseInt(java.time.LocalDate.now().toString().substring(0, 4)), seq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SDC-" + java.time.LocalDate.now().getYear() + "-0001";
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
