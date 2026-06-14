package de.lacydev.numbrassel.core.priority;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Middleware für Prioritäts-Checks bei sicherheitsrelevanten Operationen.
 * Diese Klasse führt die zentrale Prüfung durch: if (sender.getPrio() > target.getPrio())
 * 
 * ZENTRALE SICHERHEITS-MIDDLEWARE: Alle sicherheitsrelevanten Befehle MÜSSEN diese Middleware verwenden!
 */
public class PriorityCheckMiddleware {

    private static final Logger logger = LoggerFactory.getLogger(PriorityCheckMiddleware.class);
    private final IPriorityManager priorityManager;

    public PriorityCheckMiddleware(IPriorityManager priorityManager) {
        this.priorityManager = priorityManager;
        logger.info("✓ PriorityCheckMiddleware initialisiert.");
    }

    /**
     * Führt einen Prioritäts-Check durch und gibt das Ergebnis zurück.
     * 
     * LOGIK:
     * - Wenn sender.getPrio() > target.getPrio() -> Aktion wird ausgeführt
     * - Ansonsten -> Aktion wird blockiert
     *
     * @param sender Der Absender (z.B. ein CommandSender)
     * @param target Der Zielspielter
     * @return true wenn die Aktion ausgeführt werden darf, false sonst
     */
    public boolean check(Object sender, Player target) {
        boolean canExecute = priorityManager.canExecuteAction(sender, target);
        
        if (canExecute) {
            logger.debug("✓ Prioritäts-Check bestanden für Action auf Player: " + target.getName());
        } else {
            logger.warn("✗ Prioritäts-Check FEHLGESCHLAGEN für Action auf Player: " + target.getName());
        }
        
        return canExecute;
    }

    /**
     * Erweiterte Variante: Prüft mit UUID statt Player-Objekt.
     * Wird verwendet, wenn der Spieler gerade offline ist.
     *
     * @param senderUUID Die UUID des Absenders
     * @param targetUUID Die UUID des Zielspielters
     * @return true wenn die Aktion ausgeführt werden darf, false sonst
     */
    public boolean canPerformAction(UUID senderUUID, UUID targetUUID) {
        if (senderUUID == null || targetUUID == null) {
            logger.warn("Null UUID in canPerformAction!");
            return false;
        }

        Optional<Integer> senderPrioOpt = priorityManager.getPriority(senderUUID);
        Optional<Integer> targetPrioOpt = priorityManager.getPriority(targetUUID);

        if (!senderPrioOpt.isPresent() || !targetPrioOpt.isPresent()) {
            logger.warn("Konnte Priorität nicht abrufen für UUIDs: " + senderUUID + " oder " + targetUUID);
            return false;
        }

        int senderPrio = senderPrioOpt.get();
        int targetPrio = targetPrioOpt.get();

        // HARD-RULE: sender.getPrio() > target.getPrio()
        boolean canExecute = senderPrio > targetPrio;

        if (canExecute) {
            logger.debug(String.format(
                    "✓ Action erlaubt: UUID %s (Prio: %d) kann Action auf UUID %s (Prio: %d) ausführen",
                    senderUUID, senderPrio, targetUUID, targetPrio
            ));
        } else {
            logger.warn(String.format(
                    "✗ Action BLOCKIERT: UUID %s (Prio: %d) DARF NICHT auf UUID %s (Prio: %d) zugreifen",
                    senderUUID, senderPrio, targetUUID, targetPrio
            ));
        }

        return canExecute;
    }

    /**
     * Führt eine Aktion mit Prioritäts-Check durch.
     * Wenn der Check fehlschlägt, wird eine Warnung geloggt und false zurückgegeben.
     *
     * @param sender Der Absender
     * @param target Der Zielspielter
     * @param action Die auszuführende Aktion
     * @return true wenn die Aktion erfolgreich ausgeführt wurde, false wenn blockiert
     */
    public boolean executeWithCheck(Object sender, Player target, Runnable action) {
        if (!check(sender, target)) {
            logger.warn("Action auf Player " + target.getName() + " wurde blockiert (Priorität zu niedrig).");
            return false;
        }

        try {
            action.run();
            logger.info("✓ Action auf Player " + target.getName() + " erfolgreich ausgeführt.");
            return true;
        } catch (Exception e) {
            logger.error("Fehler bei Execution von Action mit Prioritäts-Check", e);
            return false;
        }
    }

    /**
     * Führt eine Aktion mit UUID-basiertem Prioritäts-Check durch.
     *
     * @param senderUUID Die UUID des Absenders
     * @param targetUUID Die UUID des Zielspielters
     * @param action Die auszuführende Aktion
     * @return true wenn die Aktion erfolgreich ausgeführt wurde, false wenn blockiert
     */
    public boolean executeWithCheck(UUID senderUUID, UUID targetUUID, Runnable action) {
        if (!canPerformAction(senderUUID, targetUUID)) {
            logger.warn("Action wurde blockiert (Priorität zu niedrig) für UUID: " + senderUUID);
            return false;
        }

        try {
            action.run();
            logger.info("✓ Action erfolgreich ausgeführt (UUID-basiert).");
            return true;
        } catch (Exception e) {
            logger.error("Fehler bei Execution von UUID-basierter Action mit Prioritäts-Check", e);
            return false;
        }
    }

    /**
     * Gibt den Prioritätsmanager zurück.
     */
    public IPriorityManager getPriorityManager() {
        return priorityManager;
    }
}
