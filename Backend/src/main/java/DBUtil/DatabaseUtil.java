package DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

// Singleton pattern: one shared connection pool for the entire backend
public class DatabaseUtil {
    private static DatabaseUtil instance;
    private Deque<Connection> pool;
    private String url;
    private String user;
    private String password;
    private int poolSize;

    private DatabaseUtil() {
        this.url = "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&serverTimezone=UTC";
        this.user = "root";
        this.password = "VIP7788@viraj";
        this.poolSize = 5;
        this.pool = new ArrayDeque<>();
        initPool();
    }

    private void initPool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            for (int i = 0; i < poolSize; i++) {
                Connection conn = DriverManager.getConnection(url, user, password);
                pool.push(conn);
            }
        } catch (ClassNotFoundException | SQLException e) {
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
        if (pool.isEmpty()) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create new database connection: " + e.getMessage(), e);
            }
        }
        return pool.pop();
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && pool.size() < poolSize) {
                    pool.push(conn);
                } else {
                    conn.close();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public synchronized void closeAll() {
        for (Connection conn : pool) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
        pool.clear();
    }
}
