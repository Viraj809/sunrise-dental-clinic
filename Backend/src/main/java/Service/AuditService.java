package Service;

import DBUtil.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * AuditService – writes explicit, user-action audit entries to the audit_log
 * table (login, logout, user/patient creation, payments, etc.). Appointment
 * row-level changes continue to be captured automatically by database triggers.
 */
public class AuditService {

    private static final AuditService INSTANCE = new AuditService();

    public static AuditService getInstance() {
        return INSTANCE;
    }

    public void log(String actionType, String tableName, int recordId,
                    Integer performedBy, String role, String description) {
        String sql = "INSERT INTO audit_log (action_type, table_name, record_id, performed_by, role, description, performed_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actionType);
            ps.setString(2, tableName);
            ps.setInt(3, recordId);
            if (performedBy != null) ps.setInt(4, performedBy); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, role);
            ps.setString(6, description);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Convenience overload that resolves the acting user from the current session. */
    public void logCurrent(String actionType, String tableName, int recordId, String description) {
        TokenManager.Session s = TokenManager.getInstance().current();
        Integer performedBy = (s != null) ? s.id : null;
        String role = (s != null) ? s.role : null;
        log(actionType, tableName, recordId, performedBy, role, description);
    }
}
