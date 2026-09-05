package tests;
import dao.NotificationDAO;
import model.Notification;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationDAOTest {

    private NotificationDAO notificationDAO;

    @BeforeEach
    void setUp() {
        notificationDAO = new NotificationDAO(); 
    }

    @Test
    @Order(1)
    @DisplayName("Test: Retrieve all notifications")
    void testFindAll() {
        List<Notification> notifications = notificationDAO.findAll(); 
        
        assertNotNull(notifications, "Notification list should not be null");
        assertTrue(notifications.size() >= 21, "There should be at least 21 notifications in the database"); 
    }

    @Test
    @Order(2)
    @DisplayName("Test: Find notifications by User ID")
    void testFindByUserId() {
        List<Notification> userNotifications = notificationDAO.findByUserId(1); 
        
        assertNotNull(userNotifications, "User 1 notification list should not be null");
        assertTrue(userNotifications.size() >= 4, "User 1 should have at least 4 notifications"); 
        
        Notification firstNotif = userNotifications.get(0);
        assertEquals(1, firstNotif.getUserId(), "User ID should match the queried ID"); 
    }

    @Test
    @Order(3)
    @DisplayName("Test: Insert a new notification")
    void testInsert() {
        Notification newNotification = new Notification();
        newNotification.setUserId(999); 
        newNotification.setTitle("Test Unread Alert");
        newNotification.setChannel("EMAIL");
        newNotification.setRecipient("test999@sunrisedental.lk");
        newNotification.setNotificationType("SYSTEM");
        newNotification.setMessage("This is a JUnit test notification insertion.");
        newNotification.setRead(false); 
        newNotification.setStatus("PENDING"); 

        boolean isInserted = notificationDAO.insert(newNotification); //[cite: 5]
        assertTrue(isInserted, "Inserting a new notification should be successful");
    }

    @Test
    @Order(4)
    @DisplayName("Test: Count unread notifications")
    void testCountUnread() {
        int unreadCount = notificationDAO.countUnread(999); 
        assertTrue(unreadCount >= 1, "User 999 should have at least 1 unread notification");
    }

    @Test
    @Order(5)
    @DisplayName("Test: Mark notification as read")
    void testMarkRead() {
        List<Notification> notifications = notificationDAO.findByUserId(999); 
        assertFalse(notifications.isEmpty(), "User 999 should have notifications to test");

      
        int targetNotificationId = notifications.get(0).getNotificationId(); 

      
        boolean isMarked = notificationDAO.markRead(targetNotificationId); 
        assertTrue(isMarked, "Marking the notification as read should return true");

  
        int newUnreadCount = notificationDAO.countUnread(999);
        assertEquals(0, newUnreadCount, "User 999 should now have 0 unread notifications");
    }
}