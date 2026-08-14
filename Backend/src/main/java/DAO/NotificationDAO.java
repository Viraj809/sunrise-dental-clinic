package DAO;

import Model.Notification;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private DatabaseUtil db;

    public NotificationDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public boolean insert(Notification notification) {
        String sql = "INSERT INTO notifications (appointment_id, channel, recipient, message, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, notification.getAppointmentId() > 0 ? notification.getAppointmentId() : null);
            ps.setString(2, notification.getChannel());
            ps.setString(3, notification.getRecipient());
            ps.setString(4, notification.getMessage());
            ps.setString(5, notification.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> findByAppointmentId(int appointmentId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE appointment_id = ? ORDER BY created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setAppointmentId(rs.getInt("appointment_id"));
        n.setChannel(rs.getString("channel"));
        n.setRecipient(rs.getString("recipient"));
        n.setMessage(rs.getString("message"));
        n.setStatus(rs.getString("status"));
        n.setSentAt(rs.getString("sent_at"));
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}
