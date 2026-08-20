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
        String sql = "INSERT INTO notifications (user_id, appointment_id, title, channel, recipient, notification_type, message, is_read, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (notification.getUserId() > 0) ps.setInt(1, notification.getUserId()); else ps.setNull(1, Types.INTEGER);
            if (notification.getAppointmentId() > 0) ps.setInt(2, notification.getAppointmentId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getChannel() != null ? notification.getChannel() : "IN_APP");
            ps.setString(5, notification.getRecipient());
            ps.setString(6, notification.getNotificationType() != null ? notification.getNotificationType() : "GENERAL");
            ps.setString(7, notification.getMessage());
            ps.setBoolean(8, notification.isRead());
            ps.setString(9, notification.getStatus() != null ? notification.getStatus() : "PENDING");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** In-app notifications for a specific user (most recent first). */
    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapNotification(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC LIMIT 200";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapNotification(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean markRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE, status = 'READ' WHERE notification_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setAppointmentId(rs.getInt("appointment_id"));
        n.setTitle(rs.getString("title"));
        n.setChannel(rs.getString("channel"));
        n.setRecipient(rs.getString("recipient"));
        n.setNotificationType(rs.getString("notification_type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setStatus(rs.getString("status"));
        n.setSentAt(rs.getString("sent_at"));
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}
