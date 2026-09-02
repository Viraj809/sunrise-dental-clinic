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
        String sql = "SELECT n.* FROM notices n WHERE n.status = 'Published' AND (n.expiry_date IS NULL OR n.expiry_date >= CURDATE()) AND (n.target_role = 'ALL' OR n.target_role = ? OR (n.target_role = 'SPECIFIC_DENTIST' AND n.target_dentist_id = ?)) ORDER BY n.created_at DESC";
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

    public boolean insert(Notice notice) {
        String sql = "INSERT INTO notices (title, description, priority, target_role, target_dentist_id, publish_date, expiry_date, status, created_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getPriority());
            ps.setString(4, notice.getTargetRole());
            if (notice.getTargetDentistId() != null) {
                ps.setInt(5, notice.getTargetDentistId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, notice.getPublishDate());
            ps.setString(7, notice.getExpiryDate());
            ps.setString(8, notice.getStatus());
            ps.setInt(9, notice.getCreatedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Notice notice) {
        String sql = "UPDATE notices SET title=?, description=?, priority=?, target_role=?, target_dentist_id=?, publish_date=?, expiry_date=?, status=?, updated_at=NOW() WHERE notice_id=?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getDescription());
            ps.setString(3, notice.getPriority());
            ps.setString(4, notice.getTargetRole());
            if (notice.getTargetDentistId() != null) {
                ps.setInt(5, notice.getTargetDentistId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, notice.getPublishDate());
            ps.setString(7, notice.getExpiryDate());
            ps.setString(8, notice.getStatus());
            ps.setInt(9, notice.getNoticeId());
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

    private Notice mapNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNoticeId(rs.getInt("notice_id"));
        n.setTitle(rs.getString("title"));
        n.setDescription(rs.getString("description"));
        n.setPriority(rs.getString("priority"));
        n.setTargetRole(rs.getString("target_role"));
        int tdId = rs.getInt("target_dentist_id");
        n.setTargetDentistId(rs.wasNull() ? null : tdId);
        n.setPublishDate(rs.getString("publish_date"));
        n.setExpiryDate(rs.getString("expiry_date"));
        n.setStatus(rs.getString("status"));
        n.setCreatedBy(rs.getInt("created_by"));
        n.setCreatedAt(rs.getString("created_at"));
        n.setUpdatedAt(rs.getString("updated_at"));
        return n;
    }
}
