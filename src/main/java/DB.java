import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

// Class for work with db
public class DB {
    private static final Logger log = LoggerFactory.getLogger(DB.class);

    // Connection parameters
    private static String URL = "jdbc:h2:mem:";
    private static String USER = "sa";
    private static String PASS = "";

    private static JdbcConnectionPool cp;

    private static String createTableQuery = "CREATE TABLE users AS SELECT * FROM CSVREAD('%s');";
    private static String searchUserQuery = "SELECT count(*) FROM users WHERE surname = ?";

    // Create table and connection pool
    public static void create(String file) {
        log.debug("Creating new database from file {}", file);

        cp = JdbcConnectionPool.create(URL, USER, PASS);

        try (Connection conn = cp.getConnection()) {
            conn.createStatement().execute(String.format(createTableQuery, file));
        } catch (SQLException e) {
            log.error("Failed to create table");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void close() {
        cp.dispose();

        log.debug("Connection to DB closed");
    }

    public static int getUserCount(String surname) {
        int userCount = 0;

        try (
                Connection conn = cp.getConnection();
                PreparedStatement searchUserStatement = conn.prepareStatement(searchUserQuery);
        ) {
            searchUserStatement.setString(1, surname);

            try ( ResultSet resultSet = searchUserStatement.executeQuery() ) {
                if (resultSet.next()) {
                    userCount = resultSet.getInt(1);
                    log.debug("Found {} users", userCount);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to check user");
            e.printStackTrace();
            System.exit(-1);
        }

        return userCount;
    }
}
