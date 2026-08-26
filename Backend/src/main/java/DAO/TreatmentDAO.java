package dao;

import dbutil.DatabaseUtil;
import model.Treatment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    public List<Treatment> findAll() {
        ArrayList<Treatment> list = new ArrayList<Treatment>();
        String sql = "SELECT * FROM treatments ORDER BY treatment_name";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapTreatment(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Treatment findByCode(String code) {
        String sql = "SELECT * FROM treatments WHERE treatment_code = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Treatment treatment = this.mapTreatment(rs);
            return treatment;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean insert(Treatment treatment) {
        String sql = "INSERT INTO treatments (treatment_code, treatment_name, base_price, consultation_fee, category, duration_minutes, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, treatment.getTreatmentCode());
                    ps.setString(2, treatment.getTreatmentName());
                    ps.setDouble(3, treatment.getBasePrice());
                    ps.setDouble(4, treatment.getConsultationFee());
                    ps.setString(5, treatment.getCategory());
                    ps.setInt(6, treatment.getDurationMinutes());
                    ps.setString(7, treatment.getDescription());
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
    public boolean update(Treatment treatment) {
        String sql = "UPDATE treatments SET treatment_name=?, base_price=?, consultation_fee=?, category=?, duration_minutes=?, description=? WHERE treatment_code=?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, treatment.getTreatmentName());
                    ps.setDouble(2, treatment.getBasePrice());
                    ps.setDouble(3, treatment.getConsultationFee());
                    ps.setString(4, treatment.getCategory());
                    ps.setInt(5, treatment.getDurationMinutes());
                    ps.setString(6, treatment.getDescription());
                    ps.setString(7, treatment.getTreatmentCode());
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
    public boolean delete(String code) {
        String sql = "DELETE FROM treatments WHERE treatment_code = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, code);
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

