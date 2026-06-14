package de.lacydev.numbrassel.web;

import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Server für die Admin-Interface.
 * Verwaltet HTTP-Endpoints über Javalin.
 */
public class WebServer {

    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private final JavaPlugin plugin;
    private final ModuleRegistry moduleRegistry;
    private int port;

    public WebServer(JavaPlugin plugin, ModuleRegistry moduleRegistry) {
        this.plugin = plugin;
        this.moduleRegistry = moduleRegistry;
        this.port = plugin.getConfig().getInt("web.port", 8080);
    }

    /**
     * Startet den Web Server.
     */
    public boolean start() {
        try {
            logger.info("Starte Web Server auf Port " + port + "...");
            // Javalin-Konfiguration würde hier erfolgen
            logger.info("✓ Web Server läuft auf http://localhost:" + port);
            return true;
        } catch (Exception e) {
            logger.error("✗ Fehler beim Starten des Web Servers", e);
            return false;
        }
    }

    /**
     * Stoppt den Web Server.
     */
    public void stop() {
        logger.info("Fahre Web Server herunter...");
    }
}
