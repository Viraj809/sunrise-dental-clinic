package DAO;

import Model.DentistSchedule;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistScheduleDAO {
    private DatabaseUtil db;

    public DentistScheduleDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public List<DentistSchedule> findByDentist(int dentistId) {
        List<DentistSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM dentist_schedule WHERE dentist_id = ? ORDER BY FIELD(day_of_week,'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public DentistSchedule findByDentistAndDay(int dentistId, String day) {
        String sql = "SELECT * FROM dentist_schedule WHERE dentist_id = ? AND day_of_week = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setString(2, day);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Insert or update the schedule row for a dentist + day (unique key). */
    public boolean upsert(DentistSchedule s) {
        DentistSchedule existing = findByDentistAndDay(s.getDentistId(), s.getDayOfWeek());
        if (existing != null) {
            s.setScheduleId(existing.getScheduleId());
            return update(s);
        }
        String sql = "INSERT INTO dentist_schedule (dentist_id, day_of_week, start_time, end_time, availability_status, unavailable_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getDentistId());
            ps.setString(2, s.getDayOfWeek());
            ps.setString(3, s.getStartTime());
            ps.setString(4, s.getEndTime());
            ps.setString(5, s.getAvailabilityStatus() != null ? s.getAvailabilityStatus() : "Available");
            if (s.getUnavailableDate() != null) ps.setDate(6, Date.valueOf(s.getUnavailableDate())); else ps.setNull(6, Types.DATE);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DentistSchedule s) {
        String sql = "UPDATE dentist_schedule SET start_time=?, end_time=?, availability_status=?, unavailable_date=? WHERE schedule_id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStartTime());
            ps.setString(2, s.getEndTime());
            ps.setString(3, s.getAvailabilityStatus() != null ? s.getAvailabilityStatus() : "Available");
            if (s.getUnavailableDate() != null) ps.setDate(4, Date.valueOf(s.getUnavailableDate())); else ps.setNull(4, Types.DATE);
            ps.setInt(5, s.getScheduleId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int scheduleId) {
        String sql = "DELETE FROM dentist_schedule WHERE schedule_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
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
