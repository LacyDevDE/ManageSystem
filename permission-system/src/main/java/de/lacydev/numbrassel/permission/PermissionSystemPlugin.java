package de.lacydev.numbrassel.permission;

import de.lacydev.numbrassel.core.NumbrasselCore;
import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permission System Modul für Numbrassel.
 * 
 * Kritische Registry-Verifizierung:
 * - Dieses Modul ist als ERFORDERLICH definiert
 * - Falls die ModuleRegistry-Prüfung fehlschlägt, wird das Plugin sofort deaktiviert
 * - Hard-Fail-Policy: Keine Ausnahmen möglich
 */
public class PermissionSystemPlugin extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger(PermissionSystemPlugin.class);
    private static PermissionSystemPlugin instance;
    private ModuleRegistry moduleRegistry;
    private PriorityManager priorityManager;

    @Override
    public void onEnable() {
        instance = this;
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Permission System v" + getDescription().getVersion() + " wird initialisiert...");
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
            boolean registered = moduleRegistry.registerModule("PermissionSystem", this, 60);
            if (!registered) {
                throw new RuntimeException("Registrierung in ModuleRegistry fehlgeschlagen!");
            }
            logger.info("✓ Permission System in ModuleRegistry registriert.");

            // Schritt 4: Konfiguration laden
            saveDefaultConfig();
            logger.info("✓ Konfiguration geladen.");

            // Schritt 5: Priority Manager initialisieren
            this.priorityManager = new PriorityManager(this, moduleRegistry);
            logger.info("✓ Priority Manager initialisiert.");

            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✓ Permission System erfolgreich aktiviert!");
            logger.info("═══════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════", e);
            logger.error("✗ KRITISCHER FEHLER beim Starten des Permission Systems!");
            logger.error("Fehler: " + e.getMessage());
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Fahre Plugin herunter...");

            // HARD-FAIL: Plugin sofort deaktivieren
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("Permission System wird deaktiviert...");

        // Deregistriere aus ModuleRegistry
        if (moduleRegistry != null) {
            moduleRegistry.unregisterModule("PermissionSystem");
            logger.info("✓ Permission System aus ModuleRegistry deregistriert.");
        }

        // Shutdown Priority Manager
        if (priorityManager != null) {
            priorityManager.shutdown();
        }
    }

    public static PermissionSystemPlugin getInstance() {
        return instance;
    }

    public PriorityManager getPriorityManager() {
        return priorityManager;
    }
}
