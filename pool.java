package ConnectionPoolManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class pool extends Thread {
    private static final String URL = "jdbc:postgresql://localhost:5432/productos";
    private static final String USER = "postgres";
    private static final String PASS = "1234";
    private static final int INITIAL_POOL_SIZE = 10;
    private static final int MIN_POOL_SIZE = 5;
    private static final int INCREMENT_SIZE = 2;
    private static final int MAX_ADDITIONAL_CONNECTIONS = 10;

    private int maxPoolSize = INITIAL_POOL_SIZE;
    private int additionalConnections = 0;
    private int totalConnections = 0; // Contador de conexiones totales
    private final BlockingQueue<Connection> pool;

    public pool() {
        this.pool = new ArrayBlockingQueue<>(maxPoolSize);

        // Inicializa el pool con las conexiones mínimas
        for (int i = 0; i < MIN_POOL_SIZE; i++) {
            try {
                Connection connection = DriverManager.getConnection(URL, USER, PASS);
                pool.offer(connection);
                totalConnections++;
                printTotalConnections();
            } catch (SQLException e) {
                System.err.println("Error initializing the connection pool: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    while (pool.size() >= maxPoolSize) {
                        if (additionalConnections < MAX_ADDITIONAL_CONNECTIONS) {
                            maxPoolSize += INCREMENT_SIZE;
                            additionalConnections += INCREMENT_SIZE;
                            System.out.println("Increased pool size to: " + maxPoolSize);
                        } else {
                            wait();
                        }
                    }
                    Connection connection = createConnection();
                    if (connection != null) {
                        pool.offer(connection);
                        totalConnections++;
                        printTotalConnections();
                    }
                    notifyAll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Connection createConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connection created");
            return connection;
        } catch (SQLException e) {
            return null;
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        while (pool.isEmpty()) {
            if (pool.size() < maxPoolSize) {
                Connection connection = createConnection();
                if (connection != null) {
                    pool.offer(connection);
                    totalConnections++;
                    printTotalConnections();
                }
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        notifyAll();
        return pool.poll();
    }

    public synchronized void releaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                pool.offer(connection);
                notifyAll();
            } else {
                totalConnections--;
                printTotalConnections();
                if (totalConnections < maxPoolSize - additionalConnections) {
                    notifyAll();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection state: " + e.getMessage());
        }
    }

    public synchronized void closePool() {
        for (Connection connection : pool) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        pool.clear();
        totalConnections = 0;
        System.out.println("Connection pool closed.");
    }

    private void printTotalConnections() {
        System.out.println("Total connections: " + totalConnections);
    }

    public synchronized int getTotalConnections() {
        return totalConnections;
    }
}

