package tests;
import dao.PatientDAO;
import model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class PatientDAOTest extends BaseTest {

    private PatientDAO patientDAO;

    @BeforeEach
    public void setUp() {
        patientDAO = new PatientDAO();
    }

    @Test
    public void testInsertAndFindById() {
        Patient patient = new Patient();
        patient.setName("Suresh Kumara");
        patient.setDateOfBirth("1994-05-12");
        patient.setGender("Male");
        patient.setAddress("No. 10, Temple Road, Nugegoda");
        patient.setContact("0723456099");
        patient.setEmail("suresh.kumara@gmail.com");
        patient.setNic("199405123456V");
        patient.setAllergies("Dust");
        patient.setActive(true);

        boolean isInserted = patientDAO.insert(patient);
        assertTrue(isInserted, "Patient insertion should be successful");

        List<Patient> patients = patientDAO.findAll();
        assertNotNull(patients);
        assertFalse(patients.isEmpty());

        // Find the newly inserted patient by NIC or email from the db data context
        Patient retrieved = patientDAO.findByNic("199405123456V");
        assertNotNull(retrieved, "Retrieved patient should not be null");
        assertEquals("Suresh Kumara", retrieved.getName());
        assertEquals("suresh.kumara@gmail.com", retrieved.getEmail());
    }

    @Test
    public void testFindByEmail() {
        // Using existing sample data from database dump: Amara Silva
        Patient patient = patientDAO.findByEmail("amara.silva@gmail.com");
        assertNotNull(patient, "Patient should be found by email");
        assertEquals("Amara Silva", patient.getName());
        assertEquals("199004152201V", patient.getNic());
    }

    @Test
    public void testFindByNic() {
        // Using existing sample data from database dump: Lakshan Fernando
        Patient patient = patientDAO.findByNic("198511227382V");
        assertNotNull(patient, "Patient should be found by NIC");
        assertEquals("Lakshan Fernando", patient.getName());
        assertEquals("0723456002", patient.getContact());
    }

    @Test
    public void testFindByContact() {
        // Using existing sample data from database dump: Priya Dissanayake
        Patient patient = patientDAO.findByContact("0723456003");
        assertNotNull(patient, "Patient should be found by contact number");
        assertEquals("Priya Dissanayake", patient.getName());
        assertEquals("priya.d@gmail.com", patient.getEmail());
    }

    @Test
    public void testSearchByName() {
        List<Patient> results = patientDAO.searchByName("Amara");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Amara Silva", results.get(0).getName());
    }

    @Test
    public void testUpdatePatient() {
        Patient patient = patientDAO.findByNic("199004152201V"); // Amara Silva
        assertNotNull(patient);
        
        String originalAddress = patient.getAddress();
        patient.setAddress("Updated Address, Colombo");
        boolean isUpdated = patientDAO.update(patient);
        assertTrue(isUpdated, "Patient update should be successful");

        Patient updated = patientDAO.findById(patient.getPatientId());
        assertEquals("Updated Address, Colombo", updated.getAddress());

        // Revert back
        patient.setAddress(originalAddress);
        patientDAO.update(patient);
    }

    @Test
    public void testSetActiveStatus() {
        Patient patient = patientDAO.findByNic("198511227382V"); // Lakshan Fernando
        assertNotNull(patient);

        boolean statusChanged = patientDAO.setActive(patient.getPatientId(), false);
        assertTrue(statusChanged, "Setting active status should succeed");

        Patient updated = patientDAO.findById(patient.getPatientId());
        assertFalse(updated.isActive(), "Patient active flag should be false");

        // Revert back to active
        patientDAO.setActive(patient.getPatientId(), true);
    }

    @Test
    public void testDeletePatient() {
        Patient patient = new Patient();
        patient.setName("Temporary Patient");
        patient.setDateOfBirth("2000-01-01");
        patient.setGender("Other");
        patient.setAddress("Temporary Address");
        patient.setContact("0723456999");
        patient.setEmail("temp.patient@gmail.com");
        patient.setNic("200001019999V");
        patient.setAllergies("None");
        patient.setActive(true);

        patientDAO.insert(patient);
        Patient inserted = patientDAO.findByNic("200001019999V");
        assertNotNull(inserted);

        boolean isDeleted = patientDAO.delete(inserted.getPatientId());
        assertTrue(isDeleted, "Patient deletion should be successful");

        Patient deletedPatient = patientDAO.findById(inserted.getPatientId());
        assertNull(deletedPatient, "Deleted patient should no longer exist");
    }
}