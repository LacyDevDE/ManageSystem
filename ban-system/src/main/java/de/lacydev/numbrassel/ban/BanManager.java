package de.lacydev.numbrassel.ban;

import de.lacydev.numbrassel.core.priority.IPriorityManager;
import de.lacydev.numbrassel.core.priority.PriorityCheckMiddleware;
import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verwaltet das Ban-System inkl. Prioritäts-Checks.
 */
public class BanManager {

    private static final Logger logger = LoggerFactory.getLogger(BanManager.class);
    private final JavaPlugin plugin;
    private final ModuleRegistry moduleRegistry;
    private PriorityCheckMiddleware priorityCheckMiddleware;

    public BanManager(JavaPlugin plugin, ModuleRegistry moduleRegistry) {
        this.plugin = plugin;
        this.moduleRegistry = moduleRegistry;
        logger.info("BanManager initialisiert.");
    }

    /**
     * Initialisiert den PriorityCheckMiddleware.
     * Wird aufgerufen, sobald das PermissionSystem verfügbar ist.
     */
    public void initializePriorityCheck(IPriorityManager priorityManager) {
        this.priorityCheckMiddleware = new PriorityCheckMiddleware(priorityManager);
        logger.info("✓ PriorityCheckMiddleware initialisiert.");
    }

    /**
     * Speichert den Ban-Manager ordnungsgemäß herunter.
     */
    public void shutdown() {
        logger.info("BanManager wird heruntergefahren...");
    }
}
