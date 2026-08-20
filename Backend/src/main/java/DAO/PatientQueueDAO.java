package DAO;

import Model.PatientQueue;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientQueueDAO {
    private DatabaseUtil db;

    public PatientQueueDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public List<PatientQueue> findActive() {
        List<PatientQueue> list = new ArrayList<>();
        String sql = "SELECT * FROM patient_queue WHERE status <> 'Completed' ORDER BY created_at";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public PatientQueue findById(int id) {
        String sql = "SELECT * FROM patient_queue WHERE queue_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNextQueueNumber() {
        String sql = "SELECT COUNT(*) + 1 AS n FROM patient_queue WHERE DATE(created_at) = CURDATE()";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return "Q" + String.format("%03d", rs.getInt("n"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Q001";
    }

    public boolean insert(PatientQueue q) {
        String sql = "INSERT INTO patient_queue (queue_number, appointment_id, patient_id, dentist_id, appointment_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getQueueNumber());
            if (q.getAppointmentId() > 0) ps.setInt(2, q.getAppointmentId()); else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, q.getPatientId());
            ps.setInt(4, q.getDentistId());
            ps.setString(5, q.getAppointmentTime());
            ps.setString(6, q.getStatus() != null ? q.getStatus() : "Waiting");
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) q.setQueueId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int queueId, String status) {
        String sql = "UPDATE patient_queue SET status = ? WHERE queue_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, queueId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int queueId) {
        String sql = "DELETE FROM patient_queue WHERE queue_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, queueId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PatientQueue map(ResultSet rs) throws SQLException {
        PatientQueue q = new PatientQueue();
        q.setQueueId(rs.getInt("queue_id"));
        q.setQueueNumber(rs.getString("queue_number"));
        q.setAppointmentId(rs.getInt("appointment_id"));
        q.setPatientId(rs.getInt("patient_id"));
        q.setDentistId(rs.getInt("dentist_id"));
        q.setAppointmentTime(rs.getString("appointment_time"));
        q.setStatus(rs.getString("status"));
        q.setCreatedAt(rs.getString("created_at"));
        return q;
    }
}
