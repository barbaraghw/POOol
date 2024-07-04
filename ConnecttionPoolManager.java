package ConnectionPoolManager;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnecttionPoolManager {
    private static final pool connectionPool = new pool();

    public static void main(String[] args) {
        connectionPool.start(); // Inicia el hilo del pool de conexiones

        try {
            // Ejemplo de uso continuo del pool (puedes ajustar según tu necesidad)
            while (true) {
                Connection connection = connectionPool.getConnection();
                // Utilizar la conexión según sea necesario
                // Por ejemplo, realizar consultas o actualizaciones en la base de datos

                // Simular un uso de conexión
                Thread.sleep(2000); // Esperar 2 segundos

                // Liberar la conexión al pool
                connectionPool.releaseConnection(connection);
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public static void releaseConnection(Connection connection) {
        connectionPool.releaseConnection(connection);
    }

    public static void closePool() throws SQLException {
        connectionPool.closePool();
    }

    public static int getTotalConnections() {
        return connectionPool.getTotalConnections();
    }
}
