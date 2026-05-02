package railway;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 * DBConnection - Database Connection Class
 */
public class DBConnection {

    private static Connection conn = null;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/railway_db" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=Asia/Karachi" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF-8",
                    "root",
                    ""
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Database Error:\n" + e.getMessage(),
                "Connection Failed",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return conn;
    }
}
