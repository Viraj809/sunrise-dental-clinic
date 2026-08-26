package service;

import service.NotificationObserver;
import java.util.ArrayList;
import java.util.List;

public class AppointmentSubject {
    private List<NotificationObserver> observers = new ArrayList<NotificationObserver>();

    public void attach(NotificationObserver observer) {
        this.observers.add(observer);
    }

    public void detach(NotificationObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers(String eventType, int appointmentId, String recipient, String message) {
        for (NotificationObserver observer : this.observers) {
            observer.update(eventType, appointmentId, recipient, message);
        }
    }
}

