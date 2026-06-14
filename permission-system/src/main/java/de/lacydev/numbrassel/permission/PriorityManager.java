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
        logger.info("  [" + PRIORITY_PLAYER + "] Player");
        logger.info("  [" + PRIORITY_MODERATOR + "] Moderator");
        logger.info("  [" + PRIORITY_ADMIN + "] Admin");
        logger.info("  [" + PRIORITY_OWNER + "] Owner");
        logger.info("  [" + PRIORITY_CONSOLE + "] Console (System)");
    }

    @Override
    public int getPriority(Player player) {
        if (player == null) {
            return PRIORITY_PLAYER;
        }

        // Prüfe Cache
        if (playerPriorities.containsKey(player.getUniqueId().toString())) {
            return playerPriorities.get(player.getUniqueId().toString());
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

        playerPriorities.put(player.getUniqueId().toString(), priority);
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

        playerPriorities.put(player.getUniqueId().toString(), priority);
        logger.info("Priorität für " + player.getName() + " gesetzt auf: " + priority);
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

        // HARD-RULE: Prüfung
        boolean canExecute = senderPrio > targetPrio;

        if (canExecute) {
            logger.debug(String.format(
                    "✓ Action erlaubt: %s (Prio: %d) kann Action auf %s (Prio: %d) ausführen",
                    senderName, senderPrio, target.getName(), targetPrio
            ));
        } else {
            logger.warn(String.format(
                    "✗ Action BLOCKIERT: %s (Prio: %d) DARF NICHT auf %s (Prio: %d) zugreifen",
                    senderName, senderPrio, target.getName(), targetPrio
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
     * Speichert den Priority Manager herunter.
     */
    public void shutdown() {
        playerPriorities.clear();
        logger.info("PriorityManager heruntergefahren.");
    }
}
