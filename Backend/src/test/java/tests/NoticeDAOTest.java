package tests;
import dao.NoticeDAO;
import model.Notice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class NoticeDAOTest extends BaseTest {

    private NoticeDAO noticeDAO;

    @BeforeEach
    public void setUp() {
        noticeDAO = new NoticeDAO();
    }

    @Test
    public void testInsertAndFindById() {
        Notice notice = new Notice();
        notice.setTitle("System Maintenance");
        notice.setDescription("System will be down for maintenance tonight.");
        notice.setPriority("Urgent");
        notice.setTargetRole("ALL");
        notice.setTargetDentistId(null);
        notice.setPublishDate("2026-09-05");
        notice.setExpiryDate("2026-09-06");
        notice.setStatus("Published");
        notice.setCreatedBy(15);

        boolean isInserted = noticeDAO.insert(notice);
        assertTrue(isInserted, "Notice insertion should be successful");

        List<Notice> notices = noticeDAO.findAll();
        assertNotNull(notices);
        assertFalse(notices.isEmpty());

        Notice latest = notices.get(0);
        Notice retrieved = noticeDAO.findById(latest.getNoticeId());
        assertNotNull(retrieved, "Retrieved notice should not be null");
        assertEquals("System Maintenance", retrieved.getTitle());
        assertEquals("Urgent", retrieved.getPriority());
    }

    @Test
    public void testFindPublishedForRole() {
        Notice notice = new Notice();
        notice.setTitle("Dentist Meeting");
        notice.setDescription("Mandatory meeting for specific dentists.");
        notice.setPriority("Important");
        notice.setTargetRole("SPECIFIC_DENTIST");
        notice.setTargetDentistId(9);
        notice.setPublishDate("2026-09-05");
        notice.setExpiryDate("2026-09-10");
        notice.setStatus("Published");
        notice.setCreatedBy(15);

        noticeDAO.insert(notice);

        List<Notice> dentistNotices = noticeDAO.findPublishedForRole("DENTIST", 9);
        assertNotNull(dentistNotices);
        assertFalse(dentistNotices.isEmpty());
    }

    @Test
    public void testUpdateNotice() {
        List<Notice> notices = noticeDAO.findAll();
        if (!notices.isEmpty()) {
            Notice notice = notices.get(0);
            notice.setTitle("Updated System Notice");
            notice.setDescription("Updated description details.");
            boolean isUpdated = noticeDAO.update(notice);
            assertTrue(isUpdated, "Notice update should be successful");

            Notice updated = noticeDAO.findById(notice.getNoticeId());
            assertEquals("Updated System Notice", updated.getTitle());
            assertEquals("Updated description details.", updated.getDescription());
        }
    }

    @Test
    public void testDeleteNotice() {
        Notice notice = new Notice();
        notice.setTitle("Temporary Notice");
        notice.setDescription("This notice will be deleted.");
        notice.setPriority("General");
        notice.setTargetRole("ALL");
        notice.setPublishDate("2026-09-05");
        notice.setExpiryDate("2026-09-07");
        notice.setStatus("Published");
        notice.setCreatedBy(15);

        noticeDAO.insert(notice);

        List<Notice> notices = noticeDAO.findAll();
        Notice latest = notices.get(0);

        boolean isDeleted = noticeDAO.delete(latest.getNoticeId());
        assertTrue(isDeleted, "Notice deletion should be successful");

        Notice deletedNotice = noticeDAO.findById(latest.getNoticeId());
        assertNull(deletedNotice, "Deleted notice should no longer exist");
    }
}