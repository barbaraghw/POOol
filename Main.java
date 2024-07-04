package ConnectionPoolManager;

import java.sql.Connection;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) {
        try {
            Connection connection = ConnecttionPoolManager.getConnection();
            // Realizar operaciones con la conexión

            // Liberar la conexión al terminar
            ConnecttionPoolManager.releaseConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
