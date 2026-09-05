package tests;

import dao.DentistDAO;
import model.Dentist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DentistDAOTest extends BaseTest {

    private DentistDAO dentistDAO;

    @BeforeEach
    public void setUp() {
      
        dentistDAO = new DentistDAO();
    }

    @Test
    public void testFindById() {

        Dentist dentist = dentistDAO.findById(9);
        
        assertNotNull(dentist, "Dentist should not be null");
        assertEquals("Dr. Dinesh Perera", dentist.getName(), "Name mismatch");
        assertEquals("General Dentistry", dentist.getSpecialization(), "Specialization mismatch");
        assertEquals("dperera@sunrisedental.lk", dentist.getEmail(), "Email mismatch");
        assertEquals("198502027890V", dentist.getNic(), "NIC mismatch");
    }

    @Test
    public void testFindByEmail() {

        Dentist dentist = dentistDAO.findByEmail("tgunawardena@sunrisedental.lk");
        
        assertNotNull(dentist, "Dentist should not be null");
        assertEquals(10, dentist.getDentistId(), "ID mismatch");
        assertEquals("Dr. Thilini Gunawardena", dentist.getName(), "Name mismatch");
        assertEquals("Orthodontics", dentist.getSpecialization(), "Specialization mismatch");
    }

    @Test
    public void testFindByNic() {

        Dentist dentist = dentistDAO.findByNic("198607072345V");
        
        assertNotNull(dentist, "Dentist should not be null");
        assertEquals("Dr. Nuwan Jayasuriya", dentist.getName(), "Name mismatch");
        assertEquals(13, dentist.getDentistId(), "ID mismatch");
    }

    @Test
    public void testFindByContact() {
  
        Dentist dentist = dentistDAO.findByContact("0712345608");
        
        assertNotNull(dentist, "Dentist should not be null");
        assertEquals("Dr. Arjuna Koswatta", dentist.getName(), "Name mismatch");
        assertEquals(16, dentist.getDentistId(), "ID mismatch");
    }

    @Test
    public void testHasAppointments() {

        boolean hasAppt = dentistDAO.hasAppointments(16);
        
        assertTrue(hasAppt, "Dr. Arjuna Koswatta should have appointments");
    }

    @Test
    public void testHasNoAppointments() {
    
        boolean hasAppt = dentistDAO.hasAppointments(999);
        
        assertFalse(hasAppt, "Non-existent dentist should not have appointments");
    }
    
    @Test
    public void testFindAll() {
     
        List<Dentist> dentists = dentistDAO.findAll();
        
        assertNotNull(dentists, "Dentists list should not be null");
        assertFalse(dentists.isEmpty(), "Dentists list should not be empty");
    
        assertTrue(dentists.size() >= 8, "There should be at least 8 active dentists");
    }
}