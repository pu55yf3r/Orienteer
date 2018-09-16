package org.orienteer.core.boot.loader.service;

import com.google.inject.ImplementedBy;
import org.orienteer.core.boot.loader.util.artifact.OArtifact;

import java.util.Set;

/**
 * Interface for manage Orienteer modules
 */
@ImplementedBy(ModuleManager.class)
public interface IModuleManager {

    void addArtifact(OArtifact artifact);
    void updateArtifact(OArtifact artifact);
    void deleteArtifact(OArtifact artifact);

    void addArtifacts(Set<OArtifact> artifacts);
    void updateArtifacts(Set<OArtifact> artifacts);
    void deleteArtifacts(Set<OArtifact> artifacts);
}