# Numbrassel Server - Architektur-Richtlinien

## Überblick
Dieses Dokument beschreibt die verbindlichen Architektur-Regeln für das modulare Numbrassel-Server-System. Alle Code-Erweiterungen MÜSSEN diese Regeln einhalten.

---

## 1. Modulare Architektur

### Struktur
Das System folgt einer strikten Modular-Architektur mit Maven/Gradle:

```
Numbrassel-Server/
├── core/                    # Zentrale Core-Komponenten
├── ban-system/             # Ban-Management Modul
├── permission-system/      # Permission & Priority-Management
├── web-interface/          # Web Admin-Interface
└── pom.xml                 # Parent POM
```

### Abhängigkeiten
- **Core-Modul**: Hat keine Abhängigkeiten zu anderen Modulen
- **Andere Module**: Sind abhängig vom Core-Modul (bereitgestellt)
- **Keine zirkulären Abhängigkeiten**: Strikt verboten

---

## 2. ModuleRegistry - Zentrale Registrierungsstelle

### Aufgabe
Die `ModuleRegistry` im Core-Modul verwaltet:
- Registrierung aller Module beim Start
- Verifikation erforderlicher Module
- Abhängigkeitsauflösung zwischen Modulen

### Hard-Fail-Policy
**Kritische Regel**: Wenn erforderliche Module fehlen, MUSS das System:
1. Den Fehler loggen
2. `Bukkit.shutdown()` oder `disablePlugin()` aufrufen
3. Den Server-Start VERHINDERN

### Implementierung in neuen Modulen

```java
public class MyModulePlugin extends JavaPlugin {
    private ModuleRegistry moduleRegistry;

    @Override
    public void onEnable() {
        try {
            // 1. Core laden
            NumbrasselCore core = (NumbrasselCore) getServer()
                .getPluginManager().getPlugin("NumbrasselCore");
            if (core == null || !core.isEnabled()) {
                throw new RuntimeException("Core-Plugin nicht gefunden!");
            }

            // 2. Registry abrufen
            this.moduleRegistry = core.getModuleRegistry();
            if (moduleRegistry == null) {
                throw new RuntimeException("ModuleRegistry nicht verfügbar!");
            }

            // 3. Modul registrieren (ID, Plugin, Priorität)
            boolean registered = moduleRegistry.registerModule(
                "MyModule", this, 50);
            if (!registered) {
                throw new RuntimeException("Registrierung fehlgeschlagen!");
            }

            // 4. Weitere Initialisierung...
            
        } catch (Exception e) {
            logger.error("KRITISCHER FEHLER", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (moduleRegistry != null) {
            moduleRegistry.unregisterModule("MyModule");
        }
    }
}
```

---

## 3. Prioritätssystem (IPriorityManager)

### Zentrale Regel: PRIO-CHECK
**HARD-RULE für alle sicherheitsrelevanten Befehle:**

```java
if (sender.getPrio() > target.getPrio()) {
    // Aktion ausführen
    execute();
} else {
    // Aktion blockieren
    block();
}
```

### Prioritäts-Stufen
| Stufe | Wert | Name |
|-------|------|------|
| 0 | 0 | Player |
| 1 | 10 | Moderator |
| 2 | 20 | Admin |
| 3 | 30 | Owner |
| Console | MAX_INT | System/Console |

### Sicherheitsrelevante Befehle
Diese Befehle MÜSSEN einen Prio-Check durchlaufen:
- `/ban <player>`
- `/kick <player>`
- `/mute <player>`
- `/permission set <player> <perm>`
- Alle Admin-Kommandos

### Implementierung

```java
private PriorityCheckMiddleware priorityMiddleware;

public void banPlayer(Player target, Player sender) {
    // PriorityCheckMiddleware verwenden
    boolean allowed = priorityMiddleware.check(sender, target);
    
    if (!allowed) {
        sender.sendMessage("§c Du darfst diese Aktion nicht ausführen!");
        return;
    }
    
    // Ban durchführen
    executeBan(target);
}
```

### Middleware-Details
```java
public class PriorityCheckMiddleware {
    public boolean check(Object sender, Player target) {
        // Automatischer Check: sender.getPrio() > target.getPrio()
        return priorityManager.canExecuteAction(sender, target);
    }
    
    public boolean executeWithCheck(Object sender, Player target, 
                                    Runnable action) {
        if (!check(sender, target)) return false;
        action.run();
        return true;
    }
}
```

---

## 4. Datenbankverbindungen

### Unterstützte Systeme
- **MariaDB**: `MariaDBConnector`
- **MongoDB**: `MongoDBConnector`

### Verwendung

```java
// MariaDB
MariaDBConnector db = new MariaDBConnector(
    "localhost", 3306, "numbrassel", "user", "pass");
if (db.connect()) {
    // Verbindung erfolgreich
    Connection conn = db.getConnection();
}

// MongoDB
MongoDBConnector mongo = new MongoDBConnector(
    "localhost", 27017, "numbrassel", "user", "pass");
if (mongo.connect()) {
    MongoDatabase db = mongo.getDatabase();
}
```

---

## 5. Logging

ALLE Module müssen `org.slf4j.Logger` verwenden:

```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

logger.info("Information");
logger.warn("Warnung");
logger.error("Fehler", exception);
```

Formatierung für Fehler:
```java
logger.error("═══════════════════════════════════════════════════════════");
logger.error("✗ KRITISCHER FEHLER: Nachricht");
logger.error("═══════════════════════════════════════════════════════════");
```

---

## 6. Checkliste für neue Module

### Bei der Erstellung eines neuen Moduls:

- [ ] Maven POM erstellt mit Core als Abhängigkeit (provided)
- [ ] Plugin-Klasse erbt von `JavaPlugin`
- [ ] `onEnable()` implementiert mit:
  - [ ] Core-Plugin-Verifikation
  - [ ] ModuleRegistry-Abruf
  - [ ] Registrierung in Registry (mit sinnvoller Priorität)
  - [ ] Eigene Initialisierung
  - [ ] Try-Catch mit Hard-Fail bei Fehler
- [ ] `onDisable()` implementiert mit:
  - [ ] Deregistrierung aus Registry
  - [ ] Cleanup von Ressourcen
- [ ] SLF4J-Logger verwendet
- [ ] Alle sicherheitsrelevanten Befehle nutzen `PriorityCheckMiddleware`
- [ ] Plugin.yml mit korrektem Namen erstellt

### plugin.yml Template:
```yaml
name: MyModule
version: 1.0.0
main: de.lacydev.numbrassel.mymodule.MyModulePlugin
description: Beschreibung des Moduls
depend: [NumbrasselCore]
```

---

## 7. Fehlerbehandlung

### Kritische Fehler (Hard-Fail)
Fehler bei:
- Modul-Registrierung
- Erforderliche Module fehlen
- Datenbank-Verbindung kann nicht hergestellt werden (falls kritisch)

**Aktion:** Plugin sofort deaktivieren

```java
try {
    // kritischer Code
} catch (Exception e) {
    logger.error("KRITISCHER FEHLER", e);
    getServer().getPluginManager().disablePlugin(this);
}
```

### Unkritische Fehler
- Einzelne Feature-Fehler
- Temporäre Verbindungsfehler

**Aktion:** Fehler loggen, aber Modul lädt weiter

---

## 8. Versionierung und Releases

- Semantische Versionierung: MAJOR.MINOR.PATCH-STATUS
- Beispiel: `1.0.0-SNAPSHOT` → `1.0.0-BETA` → `1.0.0`

---

## 9. Code-Qualität

### Anforderungen
- Java 17+ (JDK 17 erforderlich)
- Maven Clean Code Standards
- Kein Wildcard-Imports
- Javadoc für public APIs
- Thread-Safety bei concurrent Komponenten

---

## Kontakt & Fragen

Bei Fragen zur Architektur oder Richtlinien:
- Siehe GitHub Issues
- Diskutiere mit dem Team
- Frage einen Senior Architect

**Letzte Änderung:** 2026-06-14
