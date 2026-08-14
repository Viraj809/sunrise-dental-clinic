package Service;

import DAO.NotificationDAO;
import Model.Notification;

// In-app notification observer
public class StaffNotificationObserver implements NotificationObserver {
    private NotificationDAO dao = new NotificationDAO();

    @Override
    public void update(String eventType, int appointmentId, String recipient, String message) {
        Notification n = new Notification();
        n.setAppointmentId(appointmentId);
        n.setChannel("IN_APP");
        n.setRecipient(recipient);
        n.setMessage("[" + eventType + "] " + message);
        n.setStatus("SENT");
        dao.insert(n);
        System.out.println("[IN_APP] " + message);
    }
}
