package Model;

public class AuditLog {
    private int logId;
    private String actionType;
    private String tableName;
    private int recordId;
    private int performedBy;
    private String role;
    private String description;
    private String performedAt;
    private String oldValue;
    private String newValue;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getPerformedBy() { return performedBy; }
    public void setPerformedBy(int performedBy) { this.performedBy = performedBy; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPerformedAt() { return performedAt; }
    public void setPerformedAt(String performedAt) { this.performedAt = performedAt; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}
