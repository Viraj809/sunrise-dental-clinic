package tests;

import dbutil.DatabaseUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    @BeforeAll
    public void setUpDatabase() throws Exception {
        System.setProperty("test.mode", "true");
        DatabaseUtil.resetInstance();
        DatabaseUtil.getInstance();

        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(getResource("schema.sql"));
            stmt.execute(getResource("data.sql"));
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test database: " + e.getMessage(), e);
        }
    }

    private String getResource(String name) throws Exception {
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(name);
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
