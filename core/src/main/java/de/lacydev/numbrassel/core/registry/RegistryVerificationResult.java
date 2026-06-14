package de.lacydev.numbrassel.core.registry;

import java.util.List;

/**
 * Ergebnis einer Modul-Verifikation.
 */
public class RegistryVerificationResult {

    private final boolean valid;
    private final List<String> presentModules;
    private final List<String> missingModules;
    private final long verifiedAt;

    public RegistryVerificationResult(boolean valid, List<String> presentModules,
                                     List<String> missingModules, long verifiedAt) {
        this.valid = valid;
        this.presentModules = presentModules;
        this.missingModules = missingModules;
        this.verifiedAt = verifiedAt;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getPresentModules() {
        return presentModules;
    }

    public List<String> getMissingModules() {
        return missingModules;
    }

    public long getVerifiedAt() {
        return verifiedAt;
    }

    @Override
    public String toString() {
        return "RegistryVerificationResult{" +
                "valid=" + valid +
                ", present=" + presentModules.size() +
                ", missing=" + missingModules.size() +
                '}';
    }
}
