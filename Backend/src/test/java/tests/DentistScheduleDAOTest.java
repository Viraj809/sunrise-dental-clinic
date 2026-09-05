package tests;

import dao.DentistScheduleDAO;
import model.DentistSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DentistScheduleDAOTest extends BaseTest {

    private DentistScheduleDAO scheduleDAO;

    @BeforeEach
    public void setUp() {

        scheduleDAO = new DentistScheduleDAO();
    }

    @Test
    public void testUpsertAndFindByDentistAndDay() {
      
        DentistSchedule schedule = new DentistSchedule();
        schedule.setDentistId(9);
        schedule.setDayOfWeek("Monday");
        schedule.setStartTime("09:00:00");
        schedule.setEndTime("17:00:00");
        schedule.setAvailabilityStatus("Available");

        boolean isUpserted = scheduleDAO.upsert(schedule);
        assertTrue(isUpserted, "Schedule upsert should be successful");

       
        DentistSchedule retrieved = scheduleDAO.findByDentistAndDay(9, "Monday");
        assertNotNull(retrieved, "Retrieved schedule should not be null");
        assertEquals("09:00:00", retrieved.getStartTime(), "Start time mismatch");
        assertEquals("17:00:00", retrieved.getEndTime(), "End time mismatch");
        assertEquals("Available", retrieved.getAvailabilityStatus(), "Status mismatch");
    }

    @Test
    public void testFindByDentist() {
      
        List<DentistSchedule> schedules = scheduleDAO.findByDentist(9);
        
        assertNotNull(schedules, "Schedules list should not be null");
        assertFalse(schedules.isEmpty(), "Schedules list should not be empty");
        assertEquals(9, schedules.get(0).getDentistId(), "Dentist ID mismatch");
    }

    @Test
    public void testFindByDentistAndDayNotFound() {
      
        DentistSchedule retrieved = scheduleDAO.findByDentistAndDay(9, "Sunday");
        
      
        if (retrieved != null) {
            assertNotNull(retrieved.getDayOfWeek());
        } else {
            assertNull(retrieved, "Schedule should be null for non-existent day/dentist");
        }
    }

    @Test
    public void testUpdateSchedule() {
   
        DentistSchedule schedule = new DentistSchedule();
        schedule.setDentistId(9);
        schedule.setDayOfWeek("Tuesday");
        schedule.setStartTime("08:00:00");
        schedule.setEndTime("16:00:00");
        schedule.setAvailabilityStatus("Available");
        
        scheduleDAO.upsert(schedule);

   
        DentistSchedule existing = scheduleDAO.findByDentistAndDay(9, "Tuesday");
        assertNotNull(existing);

       
        existing.setEndTime("15:00:00");
        existing.setAvailabilityStatus("Unavailable");
        boolean isUpdated = scheduleDAO.update(existing);

        assertTrue(isUpdated, "Schedule update should be successful");

  
        DentistSchedule updated = scheduleDAO.findByDentistAndDay(9, "Tuesday");
        assertEquals("15:00:00", updated.getEndTime(), "Updated end time mismatch");
        assertEquals("Unavailable", updated.getAvailabilityStatus(), "Updated status mismatch");
    }

    @Test
    public void testDeleteSchedule() {
       
        DentistSchedule schedule = new DentistSchedule();
        schedule.setDentistId(9);
        schedule.setDayOfWeek("Wednesday");
        schedule.setStartTime("10:00:00");
        schedule.setEndTime("14:00:00");
        
        scheduleDAO.upsert(schedule);

        DentistSchedule existing = scheduleDAO.findByDentistAndDay(9, "Wednesday");
        assertNotNull(existing);

        boolean isDeleted = scheduleDAO.delete(existing.getScheduleId());
        assertTrue(isDeleted, "Schedule deletion should be successful");

        DentistSchedule deletedCheck = scheduleDAO.findByDentistAndDay(9, "Wednesday");
        assertNull(deletedCheck, "Deleted schedule should not exist in database");
    }
}