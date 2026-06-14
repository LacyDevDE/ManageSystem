package de.lacydev.numbrassel.core.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstrakte Basis-Klasse für Datenbankverbindungen.
 * Unterstützt sowohl MariaDB als auch MongoDB.
 */
public abstract class DatabaseConnector {

    protected static final Logger logger = LoggerFactory.getLogger(DatabaseConnector.class);

    protected String host;
    protected int port;
    protected String database;
    protected String username;
    protected String password;

    public DatabaseConnector(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Verbindung zur Datenbank herstellen.
     */
    public abstract boolean connect();

    /**
     * Verbindung zur Datenbank trennen.
     */
    public abstract boolean disconnect();

    /**
     * Prüft, ob die Verbindung aktiv ist.
     */
    public abstract boolean isConnected();

    /**
     * Gibt eine Zusammenfassung der Verbindungsparameter zurück.
     */
    public String getConnectionInfo() {
        return String.format("%s@%s:%d/%s", username, host, port, database);
    }
}
