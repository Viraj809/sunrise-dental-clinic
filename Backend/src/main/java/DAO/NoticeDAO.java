package dao;

import dbutil.DatabaseUtil;
import model.Notice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    public List<Notice> findAll() {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT * FROM notices ORDER BY created_at DESC";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapNotice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Notice findById(int id) {
        String sql = "SELECT * FROM notices WHERE notice_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            return mapNotice(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Notice> findPublishedForRole(String role, int staffId) {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.* FROM notices n WHERE n.status = 'Published' AND (n.target_role = 'ALL' OR n.target_role = ?) AND (n.expiry_date IS NULL OR n.expiry_date >= CURDATE()) AND n.notice_id NOT IN (SELECT notice_id FROM notice_reads WHERE staff_id = ?) ORDER BY n.created_at DESC";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, staffId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapNotice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countUnread(int staffId, String role) {
        String sql = "SELECT COUNT(*) FROM notices n WHERE n.status = 'Published' AND (n.target_role = 'ALL' OR n.target_role = ?) AND (n.expiry_date IS NULL OR n.expiry_date >= CURDATE()) AND n.notice_id NOT IN (SELECT notice_id FROM notice_reads WHERE staff_id = ?)";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, staffId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean insert(Notice notice) {
        String sql = "INSERT INTO notices (title, description, priority, target_role, publish_date, expiry_date, status, created_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getPriority());
            ps.setString(4, notice.getTargetRole());
            ps.setString(5, notice.getPublishDate());
            ps.setString(6, notice.getExpiryDate());
            ps.setString(7, notice.getStatus());
            ps.setInt(8, notice.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Notice notice) {
        String sql = "UPDATE notices SET title=?, description=?, priority=?, target_role=?, publish_date=?, expiry_date=?, status=?, updated_at=NOW() WHERE notice_id=?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getPriority());
            ps.setString(4, notice.getTargetRole());
            ps.setString(5, notice.getPublishDate());
            ps.setString(6, notice.getExpiryDate());
            ps.setString(7, notice.getStatus());
            ps.setInt(8, notice.getNoticeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int noticeId) {
        String sql = "DELETE FROM notices WHERE notice_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markRead(int noticeId, int staffId) {
        String sql = "INSERT IGNORE INTO notice_reads (notice_id, staff_id, read_at) VALUES (?, ?, NOW())";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Notice mapNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNoticeId(rs.getInt("notice_id"));
        n.setTitle(rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setPriority(rs.getString("priority"));
        n.setTargetRole(rs.getString("target_role"));
        n.setPublishDate(rs.getString("publish_date"));
        n.setExpiryDate(rs.getString("expiry_date"));
        n.setStatus(rs.getString("status"));
        n.setCreatedBy(rs.getInt("created_by"));
        n.setCreatedAt(rs.getString("created_at"));
        n.setUpdatedAt(rs.getString("updated_at"));
        return n;
    }
}
