package de.lacydev.numbrassel.web;

import de.lacydev.numbrassel.core.NumbrasselCore;
import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web Interface Modul für Numbrassel.
 * 
 * Registry-Verifizierung:
 * - Dieses Modul ist OPTIONAL (kann deaktiviert werden)
 * - Falls die ModuleRegistry-Prüfung fehlschlägt, wird das Plugin deaktiviert
 * - Hard-Fail-Policy: Das System kann ohne dieses Modul weiterlaufen
 */
public class WebInterfacePlugin extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger(WebInterfacePlugin.class);
    private static WebInterfacePlugin instance;
    private ModuleRegistry moduleRegistry;
    private WebServer webServer;

    @Override
    public void onEnable() {
        instance = this;
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Web Interface v" + getDescription().getVersion() + " wird initialisiert...");
        logger.info("═══════════════════════════════════════════════════════════");

        try {
            // Schritt 1: Core-Plugin laden
            NumbrasselCore corePlugin = (NumbrasselCore) getServer().getPluginManager().getPlugin("NumbrasselCore");
            if (corePlugin == null || !corePlugin.isEnabled()) {
                throw new RuntimeException("Core-Plugin nicht gefunden oder deaktiviert!");
            }
            logger.info("✓ Core-Plugin gefunden.");

            // Schritt 2: ModuleRegistry abrufen
            this.moduleRegistry = corePlugin.getModuleRegistry();
            if (moduleRegistry == null) {
                throw new RuntimeException("ModuleRegistry nicht verfügbar!");
            }
            logger.info("✓ ModuleRegistry abgerufen.");

            // Schritt 3: Dieses Modul in der Registry registrieren
            boolean registered = moduleRegistry.registerModule("WebInterface", this, 30);
            if (!registered) {
                throw new RuntimeException("Registrierung in ModuleRegistry fehlgeschlagen!");
            }
            logger.info("✓ Web Interface in ModuleRegistry registriert.");

            // Schritt 4: Konfiguration laden
            saveDefaultConfig();
            logger.info("✓ Konfiguration geladen.");

            // Schritt 5: Web Server initialisieren
            this.webServer = new WebServer(this, moduleRegistry);
            if (!webServer.start()) {
                throw new RuntimeException("Web Server konnte nicht gestartet werden!");
            }
            logger.info("✓ Web Server gestartet.");

            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✓ Web Interface erfolgreich aktiviert!");
            logger.info("═══════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════", e);
            logger.error("✗ FEHLER beim Starten des Web Interfaces!");
            logger.error("Fehler: " + e.getMessage());
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Fahre Plugin herunter...");

            // FAIL: Plugin deaktivieren (aber Server läuft weiter da optional)
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("Web Interface wird deaktiviert...");

        // Stoppe Web Server
        if (webServer != null) {
            webServer.stop();
        }

        // Deregistriere aus ModuleRegistry
        if (moduleRegistry != null) {
            moduleRegistry.unregisterModule("WebInterface");
            logger.info("✓ Web Interface aus ModuleRegistry deregistriert.");
        }
    }

    public static WebInterfacePlugin getInstance() {
        return instance;
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
