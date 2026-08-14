package Service;

import java.util.ArrayList;
import java.util.List;

// Observer pattern: subject that notifies observers on appointment changes
public class AppointmentSubject {
    private List<NotificationObserver> observers = new ArrayList<>();

    public void attach(NotificationObserver observer) {
        observers.add(observer);
    }

    public void detach(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String eventType, int appointmentId, String recipient, String message) {
        for (NotificationObserver observer : observers) {
            observer.update(eventType, appointmentId, recipient, message);
        }
    }
}
