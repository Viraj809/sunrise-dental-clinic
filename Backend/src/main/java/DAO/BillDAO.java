package dao;

import dbutil.DatabaseUtil;
import model.Bill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private DatabaseUtil db = DatabaseUtil.getInstance();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Bill findByAppointmentId(int appointmentId) {
        String sql = "SELECT * FROM bills WHERE appointment_id = ?";
        try (Connection conn = this.db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Bill bill = this.mapBill(rs);
            return bill;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Bill> findAll() {
        ArrayList<Bill> list = new ArrayList<Bill>();
        String sql = "SELECT * FROM bills ORDER BY issued_at DESC";
        try (Connection conn = this.db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql);){
            while (rs.next()) {
                list.add(this.mapBill(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean insert(Bill bill) {
        String sql = "INSERT INTO bills (appointment_id, consultation_fee, treatment_fee, discount, tax, total_amount, payment_method, payment_status, issued_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setInt(1, bill.getAppointmentId());
                    ps.setDouble(2, bill.getConsultationFee());
                    ps.setDouble(3, bill.getTreatmentFee());
                    ps.setDouble(4, bill.getDiscount());
                    ps.setDouble(5, bill.getTax());
                    ps.setDouble(6, bill.getTotalAmount());
                    ps.setString(7, bill.getPaymentMethod());
                    ps.setString(8, bill.getPaymentStatus());
                    ps.setInt(9, bill.getIssuedBy());
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block14;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean updatePaymentStatus(int billId, String paymentStatus) {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, paymentStatus);
                    ps.setInt(2, billId);
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block14;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public boolean updatePaymentDetails(int billId, String paymentMethod, String paymentStatus) {
        String sql = "UPDATE bills SET payment_method = ?, payment_status = ? WHERE bill_id = ?";
        try (Connection conn = this.db.getConnection();){
            boolean bl;
            block14: {
                PreparedStatement ps = conn.prepareStatement(sql);
                try {
                    ps.setString(1, paymentMethod);
                    ps.setString(2, paymentStatus);
                    ps.setInt(3, billId);
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block14;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
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

