# Hytale API Schnittstellen-Referenz
Diese Datei dient als API-Referenz für Copilot. Nutze diese Signaturen für die Implementierung.

## Core Server Interaktion
- **Class:** `Server`
  - `void broadcast(String message)`: Sendet eine Nachricht an alle Spieler.
  - `void shutdown()`: Stoppt den Server.

## Spieler & Befehle
- **Class:** `CommandSender`
  - `void sendMessage(String message)`
  - `boolean hasPermission(String permission)`
  - `UUID getUniqueId()`

- **Class:** `Player`
  - `String getName()`
  - `void kick(String reason)`

## Ban & Management
- **Class:** `BanManager` (Dein Modul)
  - `void executeBan(CommandSender sender, String targetName, String reason)`

## Sicherheits-Standard
- **Middleware:** `PriorityCheckMiddleware.canPerformAction(UUID actor, UUID target)`
  - Rückgabe: `true` wenn Aktion erlaubt (actorPriority > targetPriority), sonst `false`.
