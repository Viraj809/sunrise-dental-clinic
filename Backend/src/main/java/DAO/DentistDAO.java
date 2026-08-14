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

    public boolean insert(Dentist dentist) {
        String sql = "INSERT INTO dentists (name, specialization, contact, email, available_days, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dentist.getName());
            ps.setString(2, dentist.getSpecialization());
            ps.setString(3, dentist.getContact());
            ps.setString(4, dentist.getEmail());
            ps.setString(5, dentist.getAvailableDays());
            ps.setBoolean(6, dentist.isActive());
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
        d.setAvailableDays(rs.getString("available_days"));
        d.setActive(rs.getBoolean("is_active"));
        d.setCreatedAt(rs.getString("created_at"));
        return d;
    }
}
