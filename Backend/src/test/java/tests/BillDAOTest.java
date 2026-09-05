package tests;

import dao.BillDAO;
import model.Bill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BillDAOTest extends BaseTest {

    private BillDAO billDAO;

    @BeforeEach
    public void setUp() {
      
        billDAO = new BillDAO();
    }

    @Test
    public void testFindByAppointmentId_Exists() {
      
        int existingAppointmentId = 33; 

        Bill bill = billDAO.findByAppointmentId(existingAppointmentId);

        assertNotNull(bill, "Bill should not be null for existing appointment ID");
        assertEquals(21, bill.getBillId(), "Bill ID should match the database record");
        assertEquals(6300.00, bill.getTotalAmount(), "Total amount should be 6300.00");
        assertEquals("PAID", bill.getPaymentStatus(), "Payment status should be PAID");
        assertEquals("CASH", bill.getPaymentMethod(), "Payment method should be CASH");
    }

    @Test
    public void testFindByAppointmentId_NotFound() {
       
        int nonExistingAppointmentId = 9999; 

        Bill bill = billDAO.findByAppointmentId(nonExistingAppointmentId);

        assertNull(bill, "Bill should be null for non-existing appointment ID");
    }

    @Test
    public void testFindAll() {
        List<Bill> bills = billDAO.findAll();

        assertNotNull(bills, "Bill list should not be null");
        assertTrue(bills.size() > 0, "There should be at least one bill in the database");
    }

    @Test
    public void testInsert() {
      
        Bill newBill = new Bill();
        newBill.setAppointmentId(34);
        newBill.setConsultationFee(1500.00);
        newBill.setTreatmentFee(3500.00);
        newBill.setDiscount(0.00);
        newBill.setTax(100.00);
        newBill.setTotalAmount(5100.00);
        newBill.setPaymentMethod("CARD");
        newBill.setPaymentStatus("PENDING");
        newBill.setIssuedBy(15); // Ashan Perera (SYSTEM_ADMIN)

        boolean isInserted = billDAO.insert(newBill);

        assertTrue(isInserted, "Bill insertion should be successful");
    }

    @Test
    public void testUpdatePaymentStatus() {
       
        int targetBillId = 21;
        
        boolean isUpdated = billDAO.updatePaymentStatus(targetBillId, "PENDING");
        assertTrue(isUpdated, "Payment status update should return true");


        billDAO.updatePaymentStatus(targetBillId, "PAID");
    }

    @Test
    public void testUpdatePaymentDetails() {

        int targetBillId = 21;
        
        boolean isUpdated = billDAO.updatePaymentDetails(targetBillId, "INSURANCE", "PAID");
        assertTrue(isUpdated, "Payment details update should return true");

 
        billDAO.updatePaymentDetails(targetBillId, "CASH", "PAID");
    }
}