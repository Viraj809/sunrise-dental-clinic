package dbutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public class DatabaseUtil {
    private static DatabaseUtil instance;
    private Deque<Connection> pool = new ArrayDeque<Connection>();
    private String url;
    private String user;
    private String password;
    private int poolSize = 5;

    private DatabaseUtil() {
        this("jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&serverTimezone=UTC",
             "root", "VIP7788@viraj", 5);
    }

    private DatabaseUtil(String url, String user, String password, int poolSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
        this.initPool();
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            String testMode = System.getProperty("test.mode");
            if ("true".equals(testMode)) {
                instance = new DatabaseUtil(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
                    "sa", "", 5
                );
            } else {
                instance = new DatabaseUtil();
            }
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.closeAll();
            instance = null;
        }
    }

    private void initPool() {
        try {
            if (this.url.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            for (int i = 0; i < this.poolSize; ++i) {
                Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
                this.pool.push(conn);
            }
        }
        catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database connection pool: " + e.getMessage(), e);
        }
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
