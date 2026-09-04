package dao;

import dbutil.DatabaseUtil;
import model.Dentist;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DentistDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    public List<Dentist> findAll() {
        ArrayList<Dentist> list = new ArrayList<Dentist>();
        String sql = "SELECT * FROM dentists WHERE is_active = TRUE ORDER BY name";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapDentist(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Dentist> findAllIncludingInactive() {
        ArrayList<Dentist> list = new ArrayList<Dentist>();
        String sql = "SELECT * FROM dentists ORDER BY name";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapDentist(rs));
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
    public Dentist findById(int id) {
        String sql = "SELECT * FROM dentists WHERE dentist_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Dentist dentist = this.mapDentist(rs);
            return dentist;
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
    public Dentist findByEmail(String email) {
        String sql = "SELECT * FROM dentists WHERE email = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Dentist dentist = this.mapDentist(rs);
            return dentist;
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
    public Dentist findByNic(String nic) {
        String sql = "SELECT * FROM dentists WHERE NIC = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Dentist dentist = this.mapDentist(rs);
            return dentist;
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
    public Dentist findByContact(String contact) {
        String sql = "SELECT * FROM dentists WHERE contact = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Dentist dentist = this.mapDentist(rs);
            return dentist;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean insert(Dentist dentist) {
         String sql = "INSERT INTO dentists (name, specialization, contact, email, NIC, password, available_days, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
         try (Connection conn = this.db.getConnection();){
             boolean bl;
             block14: {
                 PreparedStatement ps = conn.prepareStatement(sql);
                 try {
                     ps.setString(1, dentist.getName());
                     ps.setString(2, dentist.getSpecialization());
                     ps.setString(3, dentist.getContact());
                     ps.setString(4, dentist.getEmail());
                     ps.setString(5, dentist.getNic());
                     ps.setString(6, dentist.getPassword());
                     ps.setString(7, dentist.getAvailableDays());
                     ps.setBoolean(8, dentist.isActive());
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

    private Dentist mapDentist(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setDentistId(rs.getInt("dentist_id"));
        d.setName(rs.getString("name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setContact(rs.getString("contact"));
        d.setEmail(rs.getString("email"));
        d.setNic(rs.getString("NIC"));
        d.setPassword(rs.getString("password"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setActive(rs.getBoolean("is_active"));
        d.setCreatedAt(rs.getString("created_at"));
        return d;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean update(Dentist dentist) {
         String sql = "UPDATE dentists SET name=?, specialization=?, contact=?, email=?, NIC=?, password=?, available_days=?, is_active=? WHERE dentist_id=?";
         try (Connection conn = this.db.getConnection();){
             boolean bl;
             block14: {
                 PreparedStatement ps = conn.prepareStatement(sql);
                 try {
                     ps.setString(1, dentist.getName());
                     ps.setString(2, dentist.getSpecialization());
                     ps.setString(3, dentist.getContact());
                     ps.setString(4, dentist.getEmail());
                     ps.setString(5, dentist.getNic());
                     ps.setString(6, dentist.getPassword());
                     ps.setString(7, dentist.getAvailableDays());
                     ps.setBoolean(8, dentist.isActive());
                     ps.setInt(9, dentist.getDentistId());
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
    public boolean delete(int dentistId) {
        String sql = "DELETE FROM dentists WHERE dentist_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, dentistId);
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
    public boolean hasAppointments(int dentistId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status NOT IN ('Cancelled')";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            boolean bl = rs.getInt(1) > 0;
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAvailableDays(int dentistId) {
        ArrayList<String> days = new ArrayList<String>();
        String sql = "SELECT day_of_week FROM dentist_available_days WHERE dentist_id = ? ORDER BY FIELD(day_of_week,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                days.add(rs.getString("day_of_week"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return days;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveAvailableDays(int dentistId, List<String> days) {
        Connection conn = null;
        try {
            conn = this.db.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM dentist_available_days WHERE dentist_id = ?");){
                psDel.setInt(1, dentistId);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns2 = conn.prepareStatement("INSERT INTO dentist_available_days (dentist_id, day_of_week) VALUES (?, ?)");){
                for (String day : days) {
                    psIns2.setInt(1, dentistId);
                    psIns2.setString(2, day);
                    psIns2.addBatch();
                }
                psIns2.executeBatch();
            }
            conn.commit();
            boolean psIns2 = true;
            return psIns2;
        }
        catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                }
                catch (SQLException sQLException) {
                    // empty catch block
                }
            }
            boolean bl = false;
            return bl;
        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                }
                catch (SQLException sQLException) {}
            }
        }
    }
}

