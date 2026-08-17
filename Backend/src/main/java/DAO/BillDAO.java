package DAO;

import Model.Bill;
import DBUtil.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private DatabaseUtil db;

    public BillDAO() {
        this.db = DatabaseUtil.getInstance();
    }

    public Bill findByAppointmentId(int appointmentId) {
        String sql = "SELECT * FROM bills WHERE appointment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapBill(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY issued_at DESC";
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBill(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Bill bill) {
        String sql = "INSERT INTO bills (appointment_id, consultation_fee, treatment_fee, discount, tax, total_amount, payment_method, payment_status, issued_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getAppointmentId());
            ps.setDouble(2, bill.getConsultationFee());
            ps.setDouble(3, bill.getTreatmentFee());
            ps.setDouble(4, bill.getDiscount());
            ps.setDouble(5, bill.getTax());
            ps.setDouble(6, bill.getTotalAmount());
            ps.setString(7, bill.getPaymentMethod());
            ps.setString(8, bill.getPaymentStatus());
            ps.setInt(9, bill.getIssuedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentStatus(int billId, String paymentStatus) {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentDetails(int billId, String paymentMethod, String paymentStatus) {
        String sql = "UPDATE bills SET payment_method = ?, payment_status = ? WHERE bill_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentMethod);
            ps.setString(2, paymentStatus);
            ps.setInt(3, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setAppointmentId(rs.getInt("appointment_id"));
        b.setConsultationFee(rs.getDouble("consultation_fee"));
        b.setTreatmentFee(rs.getDouble("treatment_fee"));
        b.setDiscount(rs.getDouble("discount"));
        b.setTax(rs.getDouble("tax"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setIssuedBy(rs.getInt("issued_by"));
        b.setIssuedAt(rs.getString("issued_at"));
        return b;
    }
}
