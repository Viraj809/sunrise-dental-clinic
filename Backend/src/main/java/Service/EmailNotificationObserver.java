package Service;

import DAO.NotificationDAO;
import Model.Notification;

// Simulated email observer
public class EmailNotificationObserver implements NotificationObserver {
    private NotificationDAO dao = new NotificationDAO();

    @Override
    public void update(String eventType, int appointmentId, String recipient, String message) {
        Notification n = new Notification();
        n.setAppointmentId(appointmentId);
        n.setChannel("EMAIL");
        n.setRecipient(recipient);
        n.setMessage("Email: [" + eventType + "] " + message);
        n.setStatus("SENT");
        dao.insert(n);
        System.out.println("[EMAIL] To: " + recipient + " | " + message);
    }
}
