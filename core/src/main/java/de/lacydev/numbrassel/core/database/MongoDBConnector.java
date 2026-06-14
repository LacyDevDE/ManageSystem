package de.lacydev.numbrassel.core.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB Datenbankverbindungsverwaltung.
 */
public class MongoDBConnector extends DatabaseConnector {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDBConnector(String host, int port, String database, String username, String password) {
        super(host, port, database, username, password);
    }

    @Override
    public boolean connect() {
        try {
            logger.info("Versuche Verbindung zu MongoDB herzustellen: " + getConnectionInfo());

            String connectionString;
            if (username != null && !username.isEmpty()) {
                connectionString = String.format("mongodb://%s:%s@%s:%d",
                        username, password, host, port);
            } else {
                connectionString = String.format("mongodb://%s:%d", host, port);
            }

            mongoClient = MongoClients.create(connectionString);
            mongoDatabase = mongoClient.getDatabase(database);

            // Test connection
            mongoDatabase.runCommand(new org.bson.Document("ping", 1));

            logger.info("✓ MongoDB Verbindung erfolgreich hergestellt.");
            return true;

        } catch (Exception e) {
            logger.error("✗ Fehler beim Verbinden zu MongoDB", e);
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                logger.info("✓ MongoDB Verbindung geschlossen.");
            }
            return true;
        } catch (Exception e) {
            logger.error("✗ Fehler beim Trennen der MongoDB Verbindung", e);
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            if (mongoClient != null && mongoDatabase != null) {
                mongoDatabase.runCommand(new org.bson.Document("ping", 1));
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    public MongoClient getClient() {
        return mongoClient;
    }
}
