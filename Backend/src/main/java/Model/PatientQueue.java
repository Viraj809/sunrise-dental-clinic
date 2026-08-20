package Model;

public class PatientQueue {
    private int queueId;
    private String queueNumber;
    private int appointmentId;
    private int patientId;
    private int dentistId;
    private String appointmentTime;
    private String status;
    private String createdAt;

    public int getQueueId() { return queueId; }
    public void setQueueId(int queueId) { this.queueId = queueId; }
    public String getQueueNumber() { return queueNumber; }
    public void setQueueNumber(String queueNumber) { this.queueNumber = queueNumber; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
