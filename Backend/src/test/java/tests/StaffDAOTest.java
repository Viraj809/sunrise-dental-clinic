package tests;

import dao.StaffDAO;
import model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class StaffDAOTest extends BaseTest {

    private StaffDAO staffDAO;

    @BeforeEach
    public void setUp() {
        staffDAO = new StaffDAO();
    }

    @Test
    public void testInsertAndFindById() {
        Staff staff = new Staff();
        staff.setName("Nuwan Kumara");
        staff.setEmail("nuwan.kumara@sunrisedental.lk");
        staff.setContact("0711234599");
        staff.setAddress("No. 10, Main Street, Kandy");
        staff.setNic("199301019999V");
        staff.setPassword("Staff@123");
        staff.setRole("RECEPTIONIST");
        staff.setShiftHours("08:00-16:00");
        staff.setActive(true);

        boolean isInserted = staffDAO.insert(staff);
        assertTrue(isInserted, "Staff insertion should be successful");

        List<Staff> staffList = staffDAO.findAll();
        assertNotNull(staffList);
        assertFalse(staffList.isEmpty());

        Staff retrieved = staffDAO.findByNic("199301019999V");
        assertNotNull(retrieved, "Retrieved staff should not be null");
        assertEquals("Nuwan Kumara", retrieved.getName());
        assertEquals("nuwan.kumara@sunrisedental.lk", retrieved.getEmail());
    }

    @Test
    public void testFindByEmail() {
        Staff staff = staffDAO.findByEmail("ashan@sunrisedental.lk");
        assertNotNull(staff, "Staff should be found by email");
        assertEquals("Ashan Perera", staff.getName());
        assertEquals("199001012345V", staff.getNic());
    }

    @Test
    public void testFindByNic() {
        Staff staff = staffDAO.findByNic("199205054321V");
        assertNotNull(staff, "Staff should be found by NIC");
        assertEquals("Nimali Fernando", staff.getName());
        assertEquals("0711234502", staff.getContact());
    }

    @Test
    public void testFindByContact() {
        Staff staff = staffDAO.findByContact("0711234503");
        assertNotNull(staff, "Staff should be found by contact number");
        assertEquals("Roshan Silva", staff.getName());
        assertEquals("roshan@sunrisedental.lk", staff.getEmail());
    }

    @Test
    public void testSearchByName() {
        List<Staff> results = staffDAO.search("Nimali");
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Nimali Fernando", results.get(0).getName());
    }

    @Test
    public void testUpdateStaff() {
        Staff staff = staffDAO.findByNic("199205054321V"); // Nimali Fernando
        assertNotNull(staff);
        
        String originalAddress = staff.getAddress();
        staff.setAddress("Updated Office Address, Colombo");
        boolean isUpdated = staffDAO.update(staff);
        assertTrue(isUpdated, "Staff update should be successful");

        Staff updated = staffDAO.findById(staff.getStaffId());
        assertEquals("Updated Office Address, Colombo", updated.getAddress());

        // Revert back
        staff.setAddress(originalAddress);
        staffDAO.update(staff);
    }

    @Test
    public void testDeleteStaff() {
        Staff staff = new Staff();
        staff.setName("Temporary Staff");
        staff.setEmail("temp.staff@sunrisedental.lk");
        staff.setContact("0711234598");
        staff.setAddress("Temporary Address");
        staff.setNic("200101018888V");
        staff.setPassword("Staff@123");
        staff.setRole("RECEPTIONIST");
        staff.setShiftHours("08:00-16:00");
        staff.setActive(true);

        staffDAO.insert(staff);
        Staff inserted = staffDAO.findByNic("200101018888V");
        assertNotNull(inserted);

        boolean isDeleted = staffDAO.delete(inserted.getStaffId());
        assertTrue(isDeleted, "Staff deletion should be successful");

        Staff deletedStaff = staffDAO.findById(inserted.getStaffId());
        assertNull(deletedStaff, "Deleted staff should no longer exist");
    }
}