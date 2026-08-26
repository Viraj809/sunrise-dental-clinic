package dbutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public class DatabaseUtil {
    private static DatabaseUtil instance;
    private Deque<Connection> pool = new ArrayDeque<Connection>();
    private String url = "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&serverTimezone=UTC";
    private String user = "root";
    private String password = "VIP7788@viraj";
    private int poolSize = 5;

    private DatabaseUtil() {
        this.initPool();
    }

    private void initPool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            for (int i = 0; i < this.poolSize; ++i) {
                Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
                this.pool.push(conn);
            }
        }
        catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database connection pool: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        if (this.pool.isEmpty()) {
            try {
                return DriverManager.getConnection(this.url, this.user, this.password);
            }
            catch (SQLException e) {
                throw new RuntimeException("Failed to create new database connection: " + e.getMessage(), e);
            }
        }
        return this.pool.pop();
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && this.pool.size() < this.poolSize) {
                    this.pool.push(conn);
                } else {
                    conn.close();
                }
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
        }
    }

    public synchronized void closeAll() {
        for (Connection conn : this.pool) {
            try {
                conn.close();
            }
            catch (SQLException sQLException) {}
        }
        this.pool.clear();
    }
}

