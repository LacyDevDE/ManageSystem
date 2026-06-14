package de.lacydev.numbrassel.permission;

import de.lacydev.numbrassel.core.priority.IPriorityManager;
import de.lacydev.numbrassel.core.registry.ModuleRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementierung des IPriorityManager Interface.
 * Verwaltet die Prioritäten von Spielern und führt die zentrale PrioCheck-Logik durch.
 * 
 * HARD-RULE: if (sender.getPrio() > target.getPrio()) { execute } else { block }
 */
public class PriorityManager implements IPriorityManager {

    private static final Logger logger = LoggerFactory.getLogger(PriorityManager.class);
    private final JavaPlugin plugin;
    private final ModuleRegistry moduleRegistry;
    private final Map<String, Integer> playerPriorities = new HashMap<>();
    private final Map<UUID, Integer> uuidPriorityCache = new HashMap<>();

    // Prioritäts-Stufen
    private static final int PRIORITY_PLAYER = 0;
    private static final int PRIORITY_MODERATOR = 10;
    private static final int PRIORITY_ADMIN = 20;
    private static final int PRIORITY_OWNER = 30;
    private static final int PRIORITY_CONSOLE = Integer.MAX_VALUE; // Console = höchste Priorität

    public PriorityManager(JavaPlugin plugin, ModuleRegistry moduleRegistry) {
        this.plugin = plugin;
        this.moduleRegistry = moduleRegistry;
        logger.info("PriorityManager initialisiert mit Priority-Levels:");
        logger.info("  [" + PRIORITY_PLAYER + "] Player (Standard)");
        logger.info("  [" + PRIORITY_MODERATOR + "] Moderator");
        logger.info("  [" + PRIORITY_ADMIN + "] Admin");
        logger.info("  [" + PRIORITY_OWNER + "] Owner");
        logger.info("  [" + PRIORITY_CONSOLE + "] Console (System)");
    }

    @Override
    public Optional<Integer> getPriority(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        // Prüfe UUID-Cache
        if (uuidPriorityCache.containsKey(uuid)) {
            return Optional.of(uuidPriorityCache.get(uuid));
        }

        // Versuche, den Spieler zu finden
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            int priority = getPriority(player);
            uuidPriorityCache.put(uuid, priority);
            return Optional.of(priority);
        }

        // Fallback: Standard-Priorität zurückgeben
        logger.debug("Spieler mit UUID " + uuid + " nicht gefunden. Verwende Standard-Priorität.");
        return Optional.of(PRIORITY_PLAYER);
    }

    @Override
    public int getPriority(Player player) {
        if (player == null) {
            return PRIORITY_PLAYER;
        }

        String uuidString = player.getUniqueId().toString();

        // Prüfe String-Cache
        if (playerPriorities.containsKey(uuidString)) {
            return playerPriorities.get(uuidString);
        }

        // Berechne Priorität basierend auf Permissions
        int priority = PRIORITY_PLAYER;

        if (player.hasPermission("numbrassel.priority.owner")) {
            priority = PRIORITY_OWNER;
        } else if (player.hasPermission("numbrassel.priority.admin")) {
            priority = PRIORITY_ADMIN;
        } else if (player.hasPermission("numbrassel.priority.moderator")) {
            priority = PRIORITY_MODERATOR;
        }

        playerPriorities.put(uuidString, priority);
        uuidPriorityCache.put(player.getUniqueId(), priority);
        return priority;
    }

    @Override
    public boolean setPriority(Player player, int priority) {
        if (player == null) {
            return false;
        }

        if (priority < 0 || priority > PRIORITY_OWNER) {
            logger.warn("Ungültige Priorität: " + priority);
            return false;
        }

        String uuidString = player.getUniqueId().toString();
        playerPriorities.put(uuidString, priority);
        uuidPriorityCache.put(player.getUniqueId(), priority);
        logger.info("Priorität für " + player.getName() + " (UUID: " + player.getUniqueId() + ") gesetzt auf: " + priority + " (" + getPriorityName(priority) + ")");
        return true;
    }

    @Override
    public boolean canExecuteAction(Object sender, Player target) {
        if (target == null) {
            return false;
        }

        int senderPrio;
        String senderName;

        // Bestimme Priorität des Senders
        if (sender instanceof Player) {
            senderPrio = getPriority((Player) sender);
            senderName = ((Player) sender).getName();
        } else if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
            senderPrio = PRIORITY_CONSOLE; // Console hat höchste Priorität
            senderName = "Console";
        } else {
            logger.warn("Unbekannter Sender-Typ: " + sender.getClass().getName());
            return false;
        }

        int targetPrio = getPriority(target);

        // HARD-RULE: Prüfung - sender.getPrio() > target.getPrio()
        boolean canExecute = senderPrio > targetPrio;

        if (canExecute) {
            logger.debug(String.format(
                    "✓ Action erlaubt: %s (Prio: %d - %s) kann Action auf %s (Prio: %d - %s) ausführen",
                    senderName, senderPrio, getPriorityName(senderPrio),
                    target.getName(), targetPrio, getPriorityName(targetPrio)
            ));
        } else {
            logger.warn(String.format(
                    "✗ Action BLOCKIERT: %s (Prio: %d - %s) DARF NICHT auf %s (Prio: %d - %s) zugreifen",
                    senderName, senderPrio, getPriorityName(senderPrio),
                    target.getName(), targetPrio, getPriorityName(targetPrio)
            ));
        }

        return canExecute;
    }

    @Override
    public String getPriorityName(int priority) {
        switch (priority) {
            case PRIORITY_PLAYER:
                return "Player";
            case PRIORITY_MODERATOR:
                return "Moderator";
            case PRIORITY_ADMIN:
                return "Admin";
            case PRIORITY_OWNER:
                return "Owner";
            case PRIORITY_CONSOLE:
                return "Console";
            default:
                return "Unknown";
        }
    }

    @Override
    public int getMaxPriority() {
        return PRIORITY_CONSOLE;
    }

    @Override
    public int getDefaultPriority() {
        return PRIORITY_PLAYER;
    }

    /**
     * Gibt die Prioritäts-Stufen-Konstanten zurück.
     */
    public int getPriorityPlayer() {
        return PRIORITY_PLAYER;
    }

    public int getPriorityModerator() {
        return PRIORITY_MODERATOR;
    }

    public int getPriorityAdmin() {
        return PRIORITY_ADMIN;
    }

    public int getPriorityOwner() {
        return PRIORITY_OWNER;
    }

    public int getPriorityConsole() {
        return PRIORITY_CONSOLE;
    }

    /**
     * Invalidiert den Cache für einen Spieler (z.B. nach Permission-Änderung).
     */
    public void invalidateCache(UUID uuid) {
        uuidPriorityCache.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            playerPriorities.remove(player.getUniqueId().toString());
        }
        logger.debug("Cache für UUID " + uuid + " invalidiert.");
    }

    /**
     * Speichert den Priority Manager herunter.
     */
    public void shutdown() {
        playerPriorities.clear();
        uuidPriorityCache.clear();
        logger.info("PriorityManager heruntergefahren.");
    }
}
