package DAO;

import Model.Patient;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private DatabaseUtil db;

    public PatientDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public Patient findByEmail(String email) {
        String sql = "SELECT * FROM patients WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPatient(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient findByNic(String nic) {
        String sql = "SELECT * FROM patients WHERE NIC = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPatient(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient findById(int id) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapPatient(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPatient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Patient patient) {
        String sql = "INSERT INTO patients (name, date_of_birth, gender, address, contact, email, NIC, blood_group, allergies, password_hash, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getDateOfBirth());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getAddress());
            ps.setString(5, patient.getContact());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getNic());
            ps.setString(8, patient.getBloodGroup());
            ps.setString(9, patient.getAllergies());
            ps.setString(10, patient.getPasswordHash());
            ps.setBoolean(11, patient.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET name=?, date_of_birth=?, gender=?, address=?, contact=?, email=?, blood_group=?, allergies=? WHERE patient_id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getDateOfBirth());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getAddress());
            ps.setString(5, patient.getContact());
            ps.setString(6, patient.getEmail());
            ps.setString(7, patient.getBloodGroup());
            ps.setString(8, patient.getAllergies());
            ps.setInt(9, patient.getPatientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int patientId, String passwordHash) {
        String sql = "UPDATE patients SET password_hash = ? WHERE patient_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setActive(int patientId, boolean active) {
        String sql = "UPDATE patients SET is_active = ? WHERE patient_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
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
        p.setBloodGroup(rs.getString("blood_group"));
        p.setAllergies(rs.getString("allergies"));
        p.setPasswordHash(rs.getString("password_hash"));
        p.setActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}
