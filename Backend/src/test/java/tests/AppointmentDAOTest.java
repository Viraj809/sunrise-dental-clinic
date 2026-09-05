package tests;

import dao.AppointmentDAO;
import model.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppointmentDAOTest extends BaseTest {

    private AppointmentDAO appointmentDAO;

    @BeforeEach
    public void setUp() {
        appointmentDAO = new AppointmentDAO(); 
    }

    @Test
    @Order(1)
    public void testFindById() {
        Appointment appointment = appointmentDAO.findById(33); 
        
        assertNotNull(appointment, "Appointment should not be null");
        assertEquals("SDC-2026-0001", appointment.getAppointmentNo()); 
        assertEquals(21, appointment.getPatientId()); 
        assertEquals(16, appointment.getDentistId()); 
        assertEquals("Completed", appointment.getStatus()); 
    }

    @Test
    @Order(2)
    public void testFindByAppointmentNo() {
        Appointment appointment = appointmentDAO.findByAppointmentNo("SDC-2026-0002"); 
        
        assertNotNull(appointment);
        assertEquals(34, appointment.getAppointmentId()); 
        assertEquals("TRT-010", appointment.getTreatmentType()); 
        assertEquals("Pending", appointment.getStatus()); 
    }

    @Test
    @Order(3)
    public void testFindByDentistId() {
        List<Appointment> appointments = appointmentDAO.findByDentistId(16); 
        
        assertNotNull(appointments);
        assertTrue(appointments.size() >= 2, "Should have at least 2 appointments for Dentist 16"); 
    }

    @Test
    @Order(4)
    public void testInsertAppointment() {
        Appointment newAppt = new Appointment();
        newAppt.setAppointmentNo("SDC-2026-9999");
        newAppt.setPatientId(21); 
        newAppt.setDentistId(16); 
        newAppt.setTreatmentType("TRT-001");
        newAppt.setAppointmentDate("2026-09-10");
        newAppt.setAppointmentTime("14:00:00");
        newAppt.setAppointmentType("Consultation");
        newAppt.setStatus("Scheduled");
        newAppt.setCreatedBy(15); 

        boolean isInserted = appointmentDAO.insert(newAppt); 
        assertTrue(isInserted, "Appointment insertion should be successful");
    }

    @Test
    @Order(5)
    public void testUpdateAppointment() {
        Appointment appointment = appointmentDAO.findByAppointmentNo("SDC-2026-9999"); 
        assertNotNull(appointment);

        appointment.setStatus("Confirmed");
        boolean isUpdated = appointmentDAO.update(appointment); 
        
        assertTrue(isUpdated, "Appointment update should be successful");
        
        Appointment updatedAppt = appointmentDAO.findById(appointment.getAppointmentId()); 
        assertEquals("Confirmed", updatedAppt.getStatus());
    }

    @Test
    @Order(6)
    public void testDeleteAppointment() {
        Appointment appointment = appointmentDAO.findByAppointmentNo("SDC-2026-9999"); 
        assertNotNull(appointment);

        boolean isDeleted = appointmentDAO.delete(appointment.getAppointmentId()); 
        assertTrue(isDeleted, "Appointment deletion should be successful");
        
        Appointment deletedAppt = appointmentDAO.findById(appointment.getAppointmentId()); 
        assertNull(deletedAppt, "Appointment should be null after deletion");
    }
}