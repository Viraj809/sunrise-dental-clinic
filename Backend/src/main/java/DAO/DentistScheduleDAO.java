package dao;

import dbutil.DatabaseUtil;
import model.DentistSchedule;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DentistScheduleDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    public List<DentistSchedule> findByDentist(int dentistId) {
        ArrayList<DentistSchedule> list = new ArrayList<DentistSchedule>();
        String sql = "SELECT * FROM dentist_schedule WHERE dentist_id = ? ORDER BY FIELD(day_of_week,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(this.map(rs));
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
    public DentistSchedule findByDentistAndDay(int dentistId, String day) {
        String sql = "SELECT * FROM dentist_schedule WHERE dentist_id = ? AND day_of_week = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, dentistId);
            ps.setString(2, day);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            DentistSchedule dentistSchedule = this.map(rs);
            return dentistSchedule;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean upsert(DentistSchedule s) {
        DentistSchedule existing = this.findByDentistAndDay(s.getDentistId(), s.getDayOfWeek());
        if (existing != null) {
            s.setScheduleId(existing.getScheduleId());
            return this.update(s);
        }
        String sql = "INSERT INTO dentist_schedule (dentist_id, day_of_week, start_time, end_time, availability_status, unavailable_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block17: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, s.getDentistId());
                    ps.setString(2, s.getDayOfWeek());
                    ps.setString(3, s.getStartTime());
                    ps.setString(4, s.getEndTime());
                    ps.setString(5, s.getAvailabilityStatus() != null ? s.getAvailabilityStatus() : "Available");
                    if (s.getUnavailableDate() != null) {
                        ps.setDate(6, Date.valueOf(s.getUnavailableDate()));
                    } else {
                        ps.setNull(6, 91);
                    }
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block17;
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
    public boolean update(DentistSchedule s) {
        String sql = "UPDATE dentist_schedule SET start_time=?, end_time=?, availability_status=?, unavailable_date=? WHERE schedule_id=?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block16: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, s.getStartTime());
                    ps.setString(2, s.getEndTime());
                    ps.setString(3, s.getAvailabilityStatus() != null ? s.getAvailabilityStatus() : "Available");
                    if (s.getUnavailableDate() != null) {
                        ps.setDate(4, Date.valueOf(s.getUnavailableDate()));
                    } else {
                        ps.setNull(4, 91);
                    }
                    ps.setInt(5, s.getScheduleId());
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block16;
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
    public boolean delete(int scheduleId) {
        String sql = "DELETE FROM dentist_schedule WHERE schedule_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, scheduleId);
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

    private DentistSchedule map(ResultSet rs) throws SQLException {
        DentistSchedule s = new DentistSchedule();
        s.setScheduleId(rs.getInt("schedule_id"));
        s.setDentistId(rs.getInt("dentist_id"));
        s.setDayOfWeek(rs.getString("day_of_week"));
        s.setStartTime(rs.getString("start_time"));
        s.setEndTime(rs.getString("end_time"));
        s.setAvailabilityStatus(rs.getString("availability_status"));
        s.setUnavailableDate(rs.getDate("unavailable_date") != null ? rs.getDate("unavailable_date").toString() : null);
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }
}

