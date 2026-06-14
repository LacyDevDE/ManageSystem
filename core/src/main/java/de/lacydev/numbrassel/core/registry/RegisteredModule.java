package de.lacydev.numbrassel.core.registry;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Repräsentiert ein registriertes Modul in der ModuleRegistry.
 */
public class RegisteredModule {

    private final String moduleId;
    private final JavaPlugin plugin;
    private final int priority;
    private final long registeredAt;

    public RegisteredModule(String moduleId, JavaPlugin plugin, int priority, long registeredAt) {
        this.moduleId = moduleId;
        this.plugin = plugin;
        this.priority = priority;
        this.registeredAt = registeredAt;
    }

    public String getModuleId() {
        return moduleId;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public int getPriority() {
        return priority;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - registeredAt;
    }
}
