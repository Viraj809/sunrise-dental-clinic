package tests;
import dao.TreatmentDAO;
import model.Treatment;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TreatmentDAOTest {

    private TreatmentDAO treatmentDAO;

    @BeforeEach
    void setUp() {
        // Initialize TreatmentDAO before each test
        treatmentDAO = new TreatmentDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Test: Retrieve all treatments")
    void testFindAll() {
        List<Treatment> treatments = treatmentDAO.findAll();
        
        assertNotNull(treatments, "Treatment list should not be null");
        // The database initially has 17 treatments, so there should be at least 17
        assertTrue(treatments.size() >= 17, "There should be at least 17 treatments in the database");
    }

    @Test
    @Order(2)
    @DisplayName("Test: Find treatment by code")
    void testFindByCode() {
        // Testing with an existing treatment code 'TRT-001'
        Treatment treatment = treatmentDAO.findByCode("TRT-001");
        
        assertNotNull(treatment, "Treatment for TRT-001 should be found");
        assertEquals("Dental Consultation", treatment.getTreatmentName(), "Treatment name should be 'Dental Consultation'");
        assertEquals(1500.00, treatment.getBasePrice(), "Base price should be 1500.00");
        assertEquals("General", treatment.getCategory(), "Category should be 'General'");
    }

    @Test
    @Order(3)
    @DisplayName("Test: Insert a new treatment")
    void testInsert() {
        Treatment newTreatment = new Treatment();
        newTreatment.setTreatmentCode("TRT-999");
        newTreatment.setTreatmentName("Test Teeth Whitening");
        newTreatment.setBasePrice(10000.00);
        newTreatment.setConsultationFee(1000.00);
        newTreatment.setCategory("Cosmetic");
        newTreatment.setDurationMinutes(45);
        newTreatment.setDescription("Test description for insertion");

        boolean isInserted = treatmentDAO.insert(newTreatment);
        assertTrue(isInserted, "Treatment insertion should be successful");

        // Verify that the data was successfully inserted
        Treatment insertedTreatment = treatmentDAO.findByCode("TRT-999");
        assertNotNull(insertedTreatment, "Inserted treatment should be found in the database");
        assertEquals("Test Teeth Whitening", insertedTreatment.getTreatmentName());
    }

    @Test
    @Order(4)
    @DisplayName("Test: Update an existing treatment")
    void testUpdate() {
        // Retrieve the previously inserted treatment
        Treatment treatmentToUpdate = treatmentDAO.findByCode("TRT-999");
        assertNotNull(treatmentToUpdate, "Treatment to update should exist");

        // Modify values
        treatmentToUpdate.setTreatmentName("Updated Test Teeth Whitening");
        treatmentToUpdate.setBasePrice(12000.00);

        boolean isUpdated = treatmentDAO.update(treatmentToUpdate);
        assertTrue(isUpdated, "Treatment update should be successful");

        // Verify the update
        Treatment updatedTreatment = treatmentDAO.findByCode("TRT-999");
        assertEquals("Updated Test Teeth Whitening", updatedTreatment.getTreatmentName());
        assertEquals(12000.00, updatedTreatment.getBasePrice());
    }

    @Test
    @Order(5)
    @DisplayName("Test: Delete a treatment")
    void testDelete() {
        // Delete the temporarily inserted 'TRT-999' treatment
        boolean isDeleted = treatmentDAO.delete("TRT-999");
        assertTrue(isDeleted, "Treatment deletion should be successful");

        // Verify that it no longer exists
        Treatment deletedTreatment = treatmentDAO.findByCode("TRT-999");
        assertNull(deletedTreatment, "Deleted treatment should be null when searched");
    }
}