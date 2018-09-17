package org.orienteer.core.boot.loader.service;

import com.google.inject.Singleton;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.boot.loader.distributed.AddModulesToMetadataTask;
import org.orienteer.core.boot.loader.distributed.DeleteMetadataTask;
import org.orienteer.core.boot.loader.distributed.ResolveMetadataConflictTask;
import org.orienteer.core.boot.loader.distributed.UpdateModulesInMetadata;
import org.orienteer.core.boot.loader.util.OrienteerClassLoaderUtil;
import org.orienteer.core.boot.loader.util.artifact.OArtifact;
import org.orienteer.core.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Default implementation for {@link IModuleManager}
 */
@Singleton
public class ModuleManager implements IModuleManager {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleManager.class);

    @Override
    public void addArtifact(OArtifact artifact) {
        addArtifacts(Collections.singleton(artifact));
    }

    @Override
    public void updateArtifact(OArtifact previous, OArtifact artifact) {
        updateArtifacts(CommonUtils.toMap(previous, artifact));
    }

    @Override
    public void deleteArtifact(OArtifact artifact) {
        deleteArtifacts(Collections.singleton(artifact));
    }

    @Override
    public void addArtifacts(Set<OArtifact> artifacts) {
        LOG.info("add artifacts: {}", artifacts);
        Optional<HazelcastInstance> opt = getHazelcast();
        if (opt.isPresent()) {
            HazelcastInstance hz = opt.get();
            IExecutorService executor = hz.getExecutorService(ResolveMetadataConflictTask.EXECUTOR_NAME);
            executeOnEveryMember(hz, member -> {
                Set<OArtifact> copy = OrienteerClassLoaderUtil.deepCopy(artifacts);
                executor.executeOnMember(new AddModulesToMetadataTask(copy), member);
            });
        } else {
            new AddModulesToMetadataTask(artifacts).run();
        }
    }

    @Override
    public void updateArtifacts(Map<OArtifact, OArtifact> artifacts) {
        LOG.info("update artifacts: {}", artifacts);

        Optional<HazelcastInstance> opt = getHazelcast();
        if (opt.isPresent()) {
            HazelcastInstance hz = opt.get();
            IExecutorService executor = hz.getExecutorService(ResolveMetadataConflictTask.EXECUTOR_NAME);
            executeOnEveryMember(hz, member -> {
                Map<OArtifact, OArtifact> copy = OrienteerClassLoaderUtil.deepCopy(artifacts);
                executor.executeOnMember(new UpdateModulesInMetadata(copy), member);
            });
        } else {
            new UpdateModulesInMetadata(artifacts).run();
        }
    }

    @Override
    public void deleteArtifacts(Set<OArtifact> artifacts) {
        LOG.info("delete artifacts: {}", artifacts);
        Optional<HazelcastInstance> opt = getHazelcast();
        if (opt.isPresent()) {
            HazelcastInstance hz = opt.get();
            IExecutorService executor = hz.getExecutorService(ResolveMetadataConflictTask.EXECUTOR_NAME);
            executeOnEveryMember(hz, member -> {
                Set<OArtifact> copy = OrienteerClassLoaderUtil.deepCopy(artifacts);
                executor.executeOnMember(new DeleteMetadataTask(copy), member);
            });
        } else {
            new DeleteMetadataTask(artifacts).run();
        }
    }


    private void executeOnEveryMember(HazelcastInstance hz, Consumer<Member> consumer) {
        Cluster cluster = hz.getCluster();
        cluster.getMembers().forEach(consumer);
    }

    protected Optional<HazelcastInstance> getHazelcast() {
        return getApp().getHazelcast();
    }

    private OrienteerWebApplication getApp() {
        return OrienteerWebApplication.lookupApplication();
    }
}
