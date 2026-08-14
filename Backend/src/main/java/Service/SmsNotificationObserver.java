package Service;

import DAO.NotificationDAO;
import Model.Notification;

// Simulated SMS observer
public class SmsNotificationObserver implements NotificationObserver {
    private NotificationDAO dao = new NotificationDAO();

    @Override
    public void update(String eventType, int appointmentId, String recipient, String message) {
        Notification n = new Notification();
        n.setAppointmentId(appointmentId);
        n.setChannel("SMS");
        n.setRecipient(recipient);
        n.setMessage("SMS: [" + eventType + "] " + message);
        n.setStatus("SENT");
        dao.insert(n);
        System.out.println("[SMS] To: " + recipient + " | " + message);
    }
}
