package DAO;

import Model.Treatment;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {
    private DatabaseUtil db;

    public TreatmentDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public List<Treatment> findAll() {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT * FROM treatments ORDER BY treatment_name";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapTreatment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Treatment findByCode(String code) {
        String sql = "SELECT * FROM treatments WHERE treatment_code = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapTreatment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Treatment treatment) {
        String sql = "INSERT INTO treatments (treatment_code, treatment_name, base_price, consultation_fee, category, duration_minutes, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, treatment.getTreatmentCode());
            ps.setString(2, treatment.getTreatmentName());
            ps.setDouble(3, treatment.getBasePrice());
            ps.setDouble(4, treatment.getConsultationFee());
            ps.setString(5, treatment.getCategory());
            ps.setInt(6, treatment.getDurationMinutes());
            ps.setString(7, treatment.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Treatment mapTreatment(ResultSet rs) throws SQLException {
        Treatment t = new Treatment();
        t.setTreatmentId(rs.getInt("treatment_id"));
        t.setTreatmentCode(rs.getString("treatment_code"));
        t.setTreatmentName(rs.getString("treatment_name"));
        t.setBasePrice(rs.getDouble("base_price"));
        t.setConsultationFee(rs.getDouble("consultation_fee"));
        t.setCategory(rs.getString("category"));
        t.setDurationMinutes(rs.getInt("duration_minutes"));
        t.setDescription(rs.getString("description"));
        return t;
    }
}
