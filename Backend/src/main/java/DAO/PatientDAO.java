package dao;

import dbutil.DatabaseUtil;
import model.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Patient findByEmail(String email) {
        String sql = "SELECT * FROM patients WHERE email = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Patient patient = this.mapPatient(rs);
            return patient;
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
    public Patient findByNic(String nic) {
        String sql = "SELECT * FROM patients WHERE NIC = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Patient patient = this.mapPatient(rs);
            return patient;
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
    public Patient findByContact(String contact) {
        String sql = "SELECT * FROM patients WHERE contact = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, contact);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Patient patient = this.mapPatient(rs);
            return patient;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> searchByName(String query) {
        ArrayList<Patient> list = new ArrayList<Patient>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? ORDER BY name LIMIT 20";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapPatient(rs));
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
    public Patient findById(int id) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Patient patient = this.mapPatient(rs);
            return patient;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> findAll() {
        ArrayList<Patient> list = new ArrayList<Patient>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapPatient(rs));
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
    public boolean insert(Patient patient) {
        String sql = "INSERT INTO patients (name, date_of_birth, gender, address, contact, email, NIC, allergies, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, patient.getName());
                    ps.setString(2, patient.getDateOfBirth());
                    ps.setString(3, patient.getGender());
                    ps.setString(4, patient.getAddress());
                    ps.setString(5, patient.getContact());
                    ps.setString(6, patient.getEmail());
                    ps.setString(7, patient.getNic());
                    ps.setString(8, patient.getAllergies());
                    ps.setBoolean(9, patient.isActive());
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
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET name=?, date_of_birth=?, gender=?, address=?, contact=?, email=?, allergies=? WHERE patient_id=?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, patient.getName());
                    ps.setString(2, patient.getDateOfBirth());
                    ps.setString(3, patient.getGender());
                    ps.setString(4, patient.getAddress());
                    ps.setString(5, patient.getContact());
                    ps.setString(6, patient.getEmail());
                    ps.setString(7, patient.getAllergies());
                    ps.setInt(8, patient.getPatientId());
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
    public boolean delete(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, patientId);
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

    public boolean setActive(int patientId, boolean active) {
        String sql = "UPDATE patients SET is_active = ? WHERE patient_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setBoolean(1, active);
                    ps.setInt(2, patientId);
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

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setName(rs.getString("name"));
        p.setDateOfBirth(rs.getString("date_of_birth"));
        p.setGender(rs.getString("gender"));
        p.setAddress(rs.getString("address"));
        p.setContact(rs.getString("contact"));
        p.setEmail(rs.getString("email"));
        p.setNic(rs.getString("NIC"));
        p.setAllergies(rs.getString("allergies"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}

