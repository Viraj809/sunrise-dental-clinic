package DAO;

import Model.Dentist;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistDAO {
    private DatabaseUtil db;

    public DentistDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public List<Dentist> findAll() {
        List<Dentist> list = new ArrayList<>();
        String sql = "SELECT * FROM dentists WHERE is_active = TRUE ORDER BY name";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapDentist(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Dentist findById(int id) {
        String sql = "SELECT * FROM dentists WHERE dentist_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapDentist(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dentist findByEmail(String email) {
        String sql = "SELECT * FROM dentists WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDentist(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dentist findByNic(String nic) {
        String sql = "SELECT * FROM dentists WHERE NIC = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDentist(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dentist findByContact(String contact) {
        String sql = "SELECT * FROM dentists WHERE contact = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDentist(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Dentist dentist) {
        String sql = "INSERT INTO dentists (name, specialization, contact, email, NIC, available_days, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dentist.getName());
            ps.setString(2, dentist.getSpecialization());
            ps.setString(3, dentist.getContact());
            ps.setString(4, dentist.getEmail());
            ps.setString(5, dentist.getNic());
            ps.setString(6, dentist.getAvailableDays());
            ps.setBoolean(7, dentist.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Dentist mapDentist(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setDentistId(rs.getInt("dentist_id"));
        d.setName(rs.getString("name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setContact(rs.getString("contact"));
        d.setEmail(rs.getString("email"));
        d.setNic(rs.getString("NIC"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setActive(rs.getBoolean("is_active"));
        d.setCreatedAt(rs.getString("created_at"));
        return d;
    }

    public boolean update(Dentist dentist) {
        String sql = "UPDATE dentists SET name=?, specialization=?, contact=?, email=?, NIC=?, available_days=?, is_active=? WHERE dentist_id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dentist.getName());
            ps.setString(2, dentist.getSpecialization());
            ps.setString(3, dentist.getContact());
            ps.setString(4, dentist.getEmail());
            ps.setString(5, dentist.getNic());
            ps.setString(6, dentist.getAvailableDays());
            ps.setBoolean(7, dentist.isActive());
            ps.setInt(8, dentist.getDentistId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int dentistId) {
        String sql = "DELETE FROM dentists WHERE dentist_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAppointments(int dentistId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status NOT IN ('Cancelled')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAvailableDays(int dentistId) {
        List<String> days = new ArrayList<>();
        String sql = "SELECT day_of_week FROM dentist_available_days WHERE dentist_id = ? ORDER BY FIELD(day_of_week,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) days.add(rs.getString("day_of_week"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return days;
    }

    public boolean saveAvailableDays(int dentistId, List<String> days) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM dentist_available_days WHERE dentist_id = ?")) {
                psDel.setInt(1, dentistId);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement("INSERT INTO dentist_available_days (dentist_id, day_of_week) VALUES (?, ?)")) {
                for (String day : days) {
                    psIns.setInt(1, dentistId);
                    psIns.setString(2, day);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
