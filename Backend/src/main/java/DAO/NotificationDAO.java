package dao;

import dbutil.DatabaseUtil;
import model.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean insert(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, appointment_id, title, channel, recipient, notification_type, message, is_read, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block18: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    if (notification.getUserId() > 0) {
                        ps.setInt(1, notification.getUserId());
                    } else {
                        ps.setNull(1, 4);
                    }
                    if (notification.getAppointmentId() > 0) {
                        ps.setInt(2, notification.getAppointmentId());
                    } else {
                        ps.setNull(2, 4);
                    }
                    ps.setString(3, notification.getTitle());
                    ps.setString(4, notification.getChannel() != null ? notification.getChannel() : "IN_APP");
                    ps.setString(5, notification.getRecipient());
                    ps.setString(6, notification.getNotificationType() != null ? notification.getNotificationType() : "GENERAL");
                    ps.setString(7, notification.getMessage());
                    ps.setBoolean(8, notification.isRead());
                    ps.setString(9, notification.getStatus() != null ? notification.getStatus() : "PENDING");
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block18;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> findByUserId(int userId) {
        ArrayList<Notification> list = new ArrayList<Notification>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapNotification(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Notification> findAll() {
        ArrayList<Notification> list = new ArrayList<Notification>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC LIMIT 200";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapNotification(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean markRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE, status = 'READ' WHERE notification_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, notificationId);
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block14;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return 0;
            int n = rs.getInt(1);
            return n;
        }
        catch (SQLException e) {
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

