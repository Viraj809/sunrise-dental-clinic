package DAO;

import Model.Staff;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    private DatabaseUtil db;

    public StaffDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public Staff findById(int staffId) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapStaff(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Staff findByEmail(String email) {
        String sql = "SELECT * FROM staff WHERE email = ? AND is_active = TRUE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapStaff(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Staff findByNic(String nic) {
        String sql = "SELECT * FROM staff WHERE NIC = ? AND is_active = TRUE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapStaff(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Staff findByContact(String contact) {
        String sql = "SELECT * FROM staff WHERE contact = ? AND is_active = TRUE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapStaff(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Staff> findAll() {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY role, name";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapStaff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Staff staff) {
        String sql = "INSERT INTO staff (name, email, contact, address, NIC, password_hash, role, shift_hours, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getName());
            ps.setString(2, staff.getEmail());
            ps.setString(3, staff.getContact());
            ps.setString(4, staff.getAddress());
            ps.setString(5, staff.getNic());
            ps.setString(6, staff.getPasswordHash());
            ps.setString(7, staff.getRole());
            ps.setString(8, staff.getShiftHours());
            ps.setBoolean(9, staff.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Staff mapStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setContact(rs.getString("contact"));
        s.setAddress(rs.getString("address"));
        s.setNic(rs.getString("NIC"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setRole(rs.getString("role"));
        s.setShiftHours(rs.getString("shift_hours"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }

    public boolean update(Staff staff) {
        String sql = "UPDATE staff SET name=?, email=?, contact=?, address=?, password_hash=?, role=?, shift_hours=?, is_active=? WHERE staff_id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getName());
            ps.setString(2, staff.getEmail());
            ps.setString(3, staff.getContact());
            ps.setString(4, staff.getAddress());
            ps.setString(5, staff.getPasswordHash());
            ps.setString(6, staff.getRole());
            ps.setString(7, staff.getShiftHours());
            ps.setBoolean(8, staff.isActive());
            ps.setInt(9, staff.getStaffId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

