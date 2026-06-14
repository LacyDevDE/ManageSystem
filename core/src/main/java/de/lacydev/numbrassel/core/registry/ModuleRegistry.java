package de.lacydev.numbrassel.core.registry;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zentrale Registrierungsstelle für alle Module des Numbrassel-Systems.
 * Diese Klasse verwaltet die Lebensdauer und Abhängigkeiten aller Module.
 * 
 * Thread-Sicherheit: ConcurrentHashMap für sichere Multi-Thread-Operationen
 */
public class ModuleRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ModuleRegistry.class);
    private final JavaPlugin parentPlugin;
    private final Map<String, RegisteredModule> modules = new ConcurrentHashMap<>();
    private final Map<String, Boolean> loadedModules = new ConcurrentHashMap<>();
    private final Set<String> requiredModules = new HashSet<>();

    // Definierte erforderliche Module für den Betrieb
    static {
        // Beispiel: CoreKomponenten können hier definiert werden
    }

    public ModuleRegistry(JavaPlugin parentPlugin) {
        this.parentPlugin = parentPlugin;
        logger.info("ModuleRegistry initialisiert für Parent-Plugin: " + parentPlugin.getName());
    }

    /**
     * Registriert ein Modul in der Registry.
     * Diese Methode wird von jedem Modul beim Starten aufgerufen.
     *
     * @param moduleId Die eindeutige ID des Moduls (z.B. "BanSystem", "PermissionSystem")
     * @param modulePlugin Das Bukkit-Plugin des Moduls
     * @param priority Die Priorität des Moduls (höher = wird zuerst geladen)
     * @return true wenn erfolgreich registriert, false bei Fehler
     */
    public synchronized boolean registerModule(String moduleId, JavaPlugin modulePlugin, int priority) {
        logger.info("Registriere Modul: " + moduleId);

        if (modules.containsKey(moduleId)) {
            logger.warn("Modul " + moduleId + " ist bereits registriert!");
            return false;
        }

        RegisteredModule registeredModule = new RegisteredModule(
                moduleId,
                modulePlugin,
                priority,
                System.currentTimeMillis()
        );

        modules.put(moduleId, registeredModule);
        loadedModules.put(moduleId, true);
        logger.info("✓ Modul " + moduleId + " erfolgreich registriert (Priorität: " + priority + ")");
        return true;
    }

    /**
     * Registriert ein Modul mit einfacher String-ID.
     * VEREINFACHTE VERSION für schnelle Registrierung.
     *
     * @param name Die eindeutige ID des Moduls
     * @return true wenn erfolgreich, false wenn bereits registriert
     */
    public synchronized boolean registerModule(String name) {
        logger.debug("Schnell-Registrierung von Modul: " + name);

        if (loadedModules.containsKey(name)) {
            logger.warn("Modul " + name + " ist bereits geladen!");
            return false;
        }

        loadedModules.put(name, true);
        logger.info("✓ Modul " + name + " schnell-registriert.");
        return true;
    }

    /**
     * Prüft, ob ein Modul geladen ist.
     * Diese Methode ist thread-sicher und liefert sofort eine Antwort.
     *
     * @param name Die ID des Moduls
     * @return true wenn geladen, false wenn nicht gefunden
     */
    public boolean isModuleLoaded(String name) {
        Boolean loaded = loadedModules.get(name);
        return loaded != null && loaded;
    }

    /**
     * Deregistriert ein Modul aus der Registry.
     * Diese Methode wird beim Herunterfahren eines Moduls aufgerufen.
     *
     * @param moduleId Die ID des zu deregistrierenden Moduls
     * @return true wenn erfolgreich deregistriert, false wenn Modul nicht gefunden
     */
    public synchronized boolean unregisterModule(String moduleId) {
        logger.info("Deregistriere Modul: " + moduleId);

        if (!modules.containsKey(moduleId)) {
            logger.warn("Modul " + moduleId + " nicht gefunden in Registry!");
            return false;
        }

        modules.remove(moduleId);
        loadedModules.put(moduleId, false);
        logger.info("✓ Modul " + moduleId + " erfolgreich deregistriert.");
        return true;
    }

    /**
     * Verifiziert, dass alle erforderlichen Module registriert sind.
     * HARD-FAIL-POLICY: Wenn kritische Module fehlen, wird das Plugin deaktiviert.
     *
     * @param requiredModuleIds Liste der erforderlichen Modul-IDs
     * @return RegistryVerificationResult mit Details zur Prüfung
     */
    public synchronized RegistryVerificationResult verifyModules(String... requiredModuleIds) {
        logger.info("Führe Modul-Verifikation durch (erforderliche Module: " + requiredModuleIds.length + ")");

        List<String> missingModules = new ArrayList<>();
        List<String> presentModules = new ArrayList<>();

        for (String moduleId : requiredModuleIds) {
            if (isModuleLoaded(moduleId)) {
                presentModules.add(moduleId);
                logger.debug("✓ Modul " + moduleId + " gefunden.");
            } else {
                missingModules.add(moduleId);
                logger.warn("✗ Erforderliches Modul FEHLT: " + moduleId);
            }
        }

        boolean isValid = missingModules.isEmpty();

        RegistryVerificationResult result = new RegistryVerificationResult(
                isValid,
                presentModules,
                missingModules,
                System.currentTimeMillis()
        );

        if (!isValid) {
            logger.error("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
            logger.error("✗ KRITISCHE MODULE FEHLEN!");
            logger.error("Fehlende Module: " + missingModules);
            logger.error("Das System kann ohne diese Module nicht betrieben werden.");
            logger.error("Fahre Server herunter...");
            logger.error("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");

            // HARD-FAIL: Plugin deaktivieren und Server herunterfahren
            Bukkit.getScheduler().scheduleSyncDelayedTask(parentPlugin, () -> {
                Bukkit.shutdown();
            }, 5L);
        }

        return result;
    }

    /**
     * Gibt ein registriertes Modul zurück.
     *
     * @param moduleId Die ID des Moduls
     * @return Optional mit dem Modul oder empty wenn nicht gefunden
     */
    public Optional<RegisteredModule> getModule(String moduleId) {
        return Optional.ofNullable(modules.get(moduleId));
    }

    /**
     * Gibt das Bukkit-Plugin eines Moduls zurück.
     *
     * @param moduleId Die ID des Moduls
     * @return Optional mit dem JavaPlugin oder empty wenn nicht gefunden
     */
    public Optional<JavaPlugin> getModulePlugin(String moduleId) {
        return getModule(moduleId).map(RegisteredModule::getPlugin);
    }

    /**
     * Gibt eine Liste aller registrierten Module zurück.
     */
    public List<RegisteredModule> getAllModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Gibt die Anzahl der registrierten Module zurück.
     */
    public int getModuleCount() {
        return modules.size();
    }

    /**
     * Prüft, ob ein Modul registriert ist.
     */
    public boolean isModuleRegistered(String moduleId) {
        return modules.containsKey(moduleId);
    }

    /**
     * Gibt eine Zusammenfassung aller Module zurück.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("ModuleRegistry Summary (Total: ").append(modules.size()).append(")\n");
        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");

        modules.values().stream()
                .sorted(Comparator.comparingInt(RegisteredModule::getPriority).reversed())
                .forEach(module -> {
                    sb.append(String.format("[%d] %s (Plugin: %s)\n",
                            module.getPriority(),
                            module.getModuleId(),
                            module.getPlugin().getName()));
                });

        sb.append("═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        return sb.toString();
    }

    /**
     * Fahrt die Registry ordnungsgemäß herunter.
     */
    public synchronized void shutdown() {
        logger.info("Fahre ModuleRegistry herunter...");
        logger.info("Registrierte Module: " + modules.size());
        modules.clear();
        loadedModules.clear();
        logger.info("✓ ModuleRegistry heruntergefahren.");
    }

    /**
     * Gibt eine Kopie aller geladenen Module zurück (für Debugging).
     */
    public Map<String, Boolean> getLoadedModules() {
        return new ConcurrentHashMap<>(loadedModules);
    }
}
