package de.lacydev.numbrassel.core.database;

import org.mariadb.jdbc.Connection;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;

/**
 * MariaDB Datenbankverbindungsverwaltung.
 */
public class MariaDBConnector extends DatabaseConnector {

    private MariaDbDataSource dataSource;
    private Connection connection;

    public MariaDBConnector(String host, int port, String database, String username, String password) {
        super(host, port, database, username, password);
    }

    @Override
    public boolean connect() {
        try {
            logger.info("Versuche Verbindung zu MariaDB herzustellen: " + getConnectionInfo());

            dataSource = new MariaDbDataSource();
            dataSource.setServerName(host);
            dataSource.setPort(port);
            dataSource.setDatabaseName(database);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            connection = (Connection) dataSource.getConnection();
            logger.info("✓ MariaDB Verbindung erfolgreich hergestellt.");
            return true;

        } catch (SQLException e) {
            logger.error("✗ Fehler beim Verbinden zu MariaDB", e);
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("✓ MariaDB Verbindung geschlossen.");
            }
            return true;
        } catch (SQLException e) {
            logger.error("✗ Fehler beim Trennen der MariaDB Verbindung", e);
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
