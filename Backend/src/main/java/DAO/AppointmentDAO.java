package dao;

import dbutil.DatabaseUtil;
import model.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Appointment findByAppointmentNo(String appointmentNo) {
        String sql = "SELECT * FROM appointments WHERE appointment_no = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, appointmentNo);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Appointment appointment = this.mapAppointment(rs);
            return appointment;
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
    public Appointment findById(int appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Appointment appointment = this.mapAppointment(rs);
            return appointment;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Appointment> findByPatientId(int patientId) {
        ArrayList<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapAppointment(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> findByDentistIdAndDate(int dentistId, String date) {
        ArrayList<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE dentist_id = ? AND appointment_date = ? AND status NOT IN ('Cancelled') ORDER BY appointment_time";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapAppointment(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> findByDentistId(int dentistId) {
        ArrayList<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments WHERE dentist_id = ? ORDER BY appointment_date DESC, appointment_time DESC";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.mapAppointment(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Appointment> findAll() {
        ArrayList<Appointment> list = new ArrayList<Appointment>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date DESC, appointment_time DESC";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapAppointment(rs));
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
    public boolean insert(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_no, patient_id, dentist_id, treatment_type, appointment_date, appointment_time, appointment_type, status, notes, contact, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block26: {
                PreparedStatement ps;
                block23: {
                    boolean bl2;
                    block25: {
                        ps = conn.prepareStatement(sql, 1);
                        try {
                            ps.setString(1, appointment.getAppointmentNo());
                            ps.setInt(2, appointment.getPatientId());
                            ps.setInt(3, appointment.getDentistId());
                            ps.setString(4, appointment.getTreatmentType());
                            ps.setString(5, appointment.getAppointmentDate());
                            ps.setString(6, appointment.getAppointmentTime());
                            ps.setString(7, appointment.getAppointmentType() != null ? appointment.getAppointmentType() : "Consultation");
                            ps.setString(8, appointment.getStatus());
                            ps.setString(9, appointment.getNotes());
                            ps.setString(10, appointment.getContact());
                            ps.setInt(11, appointment.getCreatedBy());
                            int affected = ps.executeUpdate();
                            if (affected <= 0) break block23;
                            try (ResultSet keys = ps.getGeneratedKeys();){
                                if (keys.next()) {
                                    appointment.setAppointmentId(keys.getInt(1));
                                }
                            }
                            bl2 = true;
                            if (ps == null) break block25;
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
                    return bl2;
                }
                bl = false;
                if (ps == null) break block26;
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
    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id=?, dentist_id=?, treatment_type=?, appointment_date=?, appointment_time=?, appointment_type=?, status=?, notes=?, contact=?, updated_at=NOW() WHERE appointment_id=?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, appointment.getPatientId());
                    ps.setInt(2, appointment.getDentistId());
                    ps.setString(3, appointment.getTreatmentType());
                    ps.setString(4, appointment.getAppointmentDate());
                    ps.setString(5, appointment.getAppointmentTime());
                    ps.setString(6, appointment.getAppointmentType() != null ? appointment.getAppointmentType() : "Consultation");
                    ps.setString(7, appointment.getStatus());
                    ps.setString(8, appointment.getNotes());
                    ps.setString(9, appointment.getContact());
                    ps.setInt(10, appointment.getAppointmentId());
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
    public boolean delete(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, appointmentId);
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
    public String getNextAppointmentNo() {
        String sql = "SELECT COUNT(*) + 1 AS next_seq FROM appointments WHERE YEAR(created_at) = YEAR(CURDATE())";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            if (!rs.next()) return "SDC-" + LocalDate.now().getYear() + "-0001";
            int seq = rs.getInt("next_seq");
            String string = String.format("SDC-%04d-%04d", Integer.parseInt(LocalDate.now().toString().substring(0, 4)), seq);
            return string;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "SDC-" + LocalDate.now().getYear() + "-0001";
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setAppointmentNo(rs.getString("appointment_no"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDentistId(rs.getInt("dentist_id"));
        a.setTreatmentType(rs.getString("treatment_type"));
        a.setAppointmentDate(rs.getString("appointment_date"));
        a.setAppointmentTime(rs.getString("appointment_time"));
        a.setAppointmentType(rs.getString("appointment_type"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setContact(rs.getString("contact"));
        a.setCreatedBy(rs.getInt("created_by"));
        a.setCreatedAt(rs.getString("created_at"));
        a.setUpdatedAt(rs.getString("updated_at"));
        return a;
    }
}

