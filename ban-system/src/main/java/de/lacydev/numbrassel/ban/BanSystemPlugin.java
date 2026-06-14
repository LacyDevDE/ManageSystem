package de.lacydev.numbrassel.ban;

import de.lacydev.numbrassel.core.NumbrasselCore;
import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import de.lacydev.numbrassel.core.registry.RegistryVerificationResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ban-System Modul für Numbrassel.
 * 
 * Kritische Registry-Verifizierung:
 * - Dieses Modul ist als ERFORDERLICH definiert
 * - Falls die ModuleRegistry-Prüfung fehlschlägt, wird das Plugin sofort deaktiviert
 * - Hard-Fail-Policy: Keine Ausnahmen möglich
 */
public class BanSystemPlugin extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger(BanSystemPlugin.class);
    private static BanSystemPlugin instance;
    private ModuleRegistry moduleRegistry;
    private BanManager banManager;

    @Override
    public void onEnable() {
        instance = this;
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Ban System v" + getDescription().getVersion() + " wird initialisiert...");
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
            boolean registered = moduleRegistry.registerModule("BanSystem", this, 50);
            if (!registered) {
                throw new RuntimeException("Registrierung in ModuleRegistry fehlgeschlagen!");
            }
            logger.info("✓ Ban System in ModuleRegistry registriert.");

            // Schritt 4: Konfiguration laden
            saveDefaultConfig();
            logger.info("✓ Konfiguration geladen.");

            // Schritt 5: Ban Manager initialisieren
            this.banManager = new BanManager(this, moduleRegistry);
            logger.info("✓ Ban Manager initialisiert.");

            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✓ Ban System erfolgreich aktiviert!");
            logger.info("═══════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════════════════════", e);
            logger.error("✗ KRITISCHER FEHLER beim Starten des Ban Systems!");
            logger.error("Fehler: " + e.getMessage());
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("Fahre Plugin herunter...");

            // HARD-FAIL: Plugin sofort deaktivieren
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info("Ban System wird deaktiviert...");

        // Deregistriere aus ModuleRegistry
        if (moduleRegistry != null) {
            moduleRegistry.unregisterModule("BanSystem");
            logger.info("✓ Ban System aus ModuleRegistry deregistriert.");
        }

        // Shutdown Ban Manager
        if (banManager != null) {
            banManager.shutdown();
        }
    }

    public static BanSystemPlugin getInstance() {
        return instance;
    }

    public BanManager getBanManager() {
        return banManager;
    }
}
