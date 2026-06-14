package de.lacydev.numbrassel.core.priority;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Middleware für Prioritäts-Checks bei sicherheitsrelevanten Operationen.
 * Diese Klasse führt die zentrale Prüfung durch: if (sender.getPrio() > target.getPrio())
 */
public class PriorityCheckMiddleware {

    private static final Logger logger = LoggerFactory.getLogger(PriorityCheckMiddleware.class);
    private final IPriorityManager priorityManager;

    public PriorityCheckMiddleware(IPriorityManager priorityManager) {
        this.priorityManager = priorityManager;
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
            return true;
        } catch (Exception e) {
            logger.error("Fehler bei Execution von Action mit Prioritäts-Check", e);
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
