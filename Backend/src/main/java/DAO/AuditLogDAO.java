package DAO;

import Model.AuditLog;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {
    private DatabaseUtil db;

    public AuditLogDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public List<AuditLog> findAll(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY performed_at DESC LIMIT " + (limit > 0 ? limit : 200);
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setLogId(rs.getInt("log_id"));
        a.setActionType(rs.getString("action_type"));
        a.setTableName(rs.getString("table_name"));
        a.setRecordId(rs.getInt("record_id"));
        a.setPerformedBy(rs.getInt("performed_by"));
        a.setRole(rs.getString("role"));
        a.setDescription(rs.getString("description"));
        a.setPerformedAt(rs.getString("performed_at"));
        a.setOldValue(rs.getString("old_value"));
        a.setNewValue(rs.getString("new_value"));
        return a;
    }
}
