package dao;

import dbutil.DatabaseUtil;
import model.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Staff findById(int staffId) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Staff staff = this.mapStaff(rs);
            return staff;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Staff findByEmail(String email) {
        String sql = "SELECT * FROM staff WHERE email = ? AND is_active = TRUE";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Staff staff = this.mapStaff(rs);
            return staff;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Staff findByNic(String nic) {
        String sql = "SELECT * FROM staff WHERE NIC = ? AND is_active = TRUE";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Staff staff = this.mapStaff(rs);
            return staff;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Staff findByContact(String contact) {
        String sql = "SELECT * FROM staff WHERE contact = ? AND is_active = TRUE";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Staff staff = this.mapStaff(rs);
            return staff;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Staff> findAll() {
        ArrayList<Staff> list = new ArrayList<Staff>();
        String sql = "SELECT * FROM staff ORDER BY role, name";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapStaff(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Staff> search(String query) {
        ArrayList<Staff> list = new ArrayList<Staff>();
        String sql = "SELECT * FROM staff WHERE name LIKE ? OR email LIKE ? OR NIC LIKE ? OR contact LIKE ? ORDER BY role, name LIMIT 50";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            String like = "%" + query + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapStaff(rs));
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
    public boolean insert(Staff staff) {
        String sql = "INSERT INTO staff (name, email, contact, address, NIC, password, role, shift_hours, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, staff.getName());
                    ps.setString(2, staff.getEmail());
                    ps.setString(3, staff.getContact());
                    ps.setString(4, staff.getAddress());
                    ps.setString(5, staff.getNic());
                    ps.setString(6, staff.getPassword());
                    ps.setString(7, staff.getRole());
                    ps.setString(8, staff.getShiftHours());
                    ps.setBoolean(9, staff.isActive());
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

    private Staff mapStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setContact(rs.getString("contact"));
        s.setAddress(rs.getString("address"));
        s.setNic(rs.getString("NIC"));
        s.setPassword(rs.getString("password"));
        s.setRole(rs.getString("role"));
        s.setShiftHours(rs.getString("shift_hours"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean update(Staff staff) {
        String sql = "UPDATE staff SET name=?, email=?, contact=?, address=?, password=?, role=?, shift_hours=?, is_active=? WHERE staff_id=?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, staff.getName());
                    ps.setString(2, staff.getEmail());
                    ps.setString(3, staff.getContact());
                    ps.setString(4, staff.getAddress());
                    ps.setString(5, staff.getPassword());
                    ps.setString(6, staff.getRole());
                    ps.setString(7, staff.getShiftHours());
                    ps.setBoolean(8, staff.isActive());
                    ps.setInt(9, staff.getStaffId());
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
     * Enabled aggressive exception aggregation
     */
    public boolean delete(int staffId) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, staffId);
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
}

