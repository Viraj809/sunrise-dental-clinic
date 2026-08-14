package Service;

// Observer interface
public interface NotificationObserver {
    void update(String eventType, int appointmentId, String recipient, String message);
}
