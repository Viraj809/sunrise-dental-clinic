package model;

public class DentistSchedule {
    private int scheduleId;
    private int dentistId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String availabilityStatus;
    private String unavailableDate;
    private String createdAt;

    public int getScheduleId() {
        return this.scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getDentistId() {
        return this.dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public String getDayOfWeek() {
        return this.dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAvailabilityStatus() {
        return this.availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getUnavailableDate() {
        return this.unavailableDate;
    }

    public void setUnavailableDate(String unavailableDate) {
        this.unavailableDate = unavailableDate;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

