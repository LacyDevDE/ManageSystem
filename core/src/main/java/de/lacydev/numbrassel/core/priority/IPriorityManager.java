package de.lacydev.numbrassel.core.priority;

import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface für das Prioritätssystem des Numbrassel-Servers.
 * Definiert die Schnittstelle für Prioritäts-Management und -Verifikation.
 */
public interface IPriorityManager {

    /**
     * Gibt die Priorität eines Spielers basierend auf seiner UUID zurück.
     * Höhere Werte = höhere Priorität.
     *
     * @param uuid Die UUID des Spielers
     * @return Optional mit der Priorität, oder empty wenn nicht gefunden
     */
    Optional<Integer> getPriority(UUID uuid);

    /**
     * Gibt die Priorität eines Spielers (Legacy-Methode) zurück.
     * Höhere Werte = höhere Priorität.
     *
     * @param player Der Spieler
     * @return Die Priorität (0 = Standard-Spieler, höher = Admin/Moderator)
     */
    int getPriority(Player player);

    /**
     * Setzt die Priorität eines Spielers.
     *
     * @param player Der Spieler
     * @param priority Die neue Priorität
     * @return true wenn erfolgreich, false bei Fehler
     */
    boolean setPriority(Player player, int priority);

    /**
     * Prüft, ob ein Sender (z.B. Spieler/Kommandozeile) die Berechtigung hat,
     * eine Aktion auf einem Ziel auszuführen.
     *
     * HARD-RULE: sender.getPrio() > target.getPrio() erforderlich!
     *
     * @param sender Der Initiator der Aktion (kann auch CommandSender sein)
     * @param target Der Zielspielter
     * @return true wenn Aktion erlaubt, false wenn blockiert
     */
    boolean canExecuteAction(Object sender, Player target);

    /**
     * Gibt den Namen der Prioritätsstufe zurück (z.B. "Player", "Moderator", "Admin").
     *
     * @param priority Die Prioritätsstufe
     * @return Der Name der Stufe
     */
    String getPriorityName(int priority);

    /**
     * Gibt die maximale Priorität zurück, die in diesem System existiert.
     *
     * @return Die maximale Prioritätsstufe
     */
    int getMaxPriority();

    /**
     * Gibt die Standard-Priorität für neue Spieler zurück.
     *
     * @return Die Standard-Priorität
     */
    int getDefaultPriority();
}
