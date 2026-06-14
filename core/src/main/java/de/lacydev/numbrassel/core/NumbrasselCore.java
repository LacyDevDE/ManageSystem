package de.lacydev.numbrassel.core;

import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import de.lacydev.numbrassel.core.registry.RegistryVerificationResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hauptklasse des Numbrassel Core-Moduls.
 * Verantwortlich für die Initialisierung der ModuleRegistry und die Verwaltung
 * der zentralen Komponenten des Systems.
 */
public class NumbrasselCore extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger(NumbrasselCore.class);
    private static NumbrasselCore instance;
    private ModuleRegistry moduleRegistry;

    @Override
    public void onEnable() {
        instance = this;
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("Numbrassel Core v" + getDescription().getVersion() + " wird initialisiert...");
        logger.info("═══════════════════════════════════════════════════════════");

        try {
            // Konfiguration laden
            saveDefaultConfig();
            logger.info("Konfiguration erfolgreich geladen.");

            // ModuleRegistry initialisieren
            this.moduleRegistry = new ModuleRegistry(this);
            logger.info("ModuleRegistry erfolgreich erstellt.");

            // Registry mit Modulen registrieren (werden später durch Module selbst registriert)
            logger.info("Warte auf Modul-Registrierungen...");

            logger.info("═══════════════════════════════════════════════════════════");
            logger.info("✓ Numbrassel Core erfolgreich aktiviert!");
            logger.info("═══════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("✗ KRITISCHER FEHLER beim Starten des Numbrassel Core!", e);
            logger.error("Fahre Server herunter...");
            Bukkit.shutdown();
        }
    }

    @Override
    public void onDisable() {
        logger.info("Numbrassel Core wird deaktiviert...");
        if (moduleRegistry != null) {
            moduleRegistry.shutdown();
        }
    }

    /**
     * Gibt die Instanz des Core-Plugins zurück.
     */
    public static NumbrasselCore getInstance() {
        return instance;
    }

    /**
     * Gibt die ModuleRegistry zurück.
     */
    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }
}
