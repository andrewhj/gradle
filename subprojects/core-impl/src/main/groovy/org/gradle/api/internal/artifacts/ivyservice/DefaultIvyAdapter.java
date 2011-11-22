/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.version.VersionMatcher;
import org.gradle.api.internal.artifacts.configurations.ResolutionStrategyInternal;
import org.gradle.api.internal.artifacts.ivyservice.clientmodule.ClientModuleRegistry;
import org.gradle.api.internal.artifacts.ivyservice.clientmodule.ClientModuleResolver;
import org.gradle.util.WrapUtil;

public class DefaultIvyAdapter implements IvyAdapter {
    private final Ivy ivy;
    private final ClientModuleRegistry clientModuleRegistry;
    private final VersionMatcher versionMatcher;
    private final ResolutionStrategyInternal resolutionStrategy;
    private final DependencyResolver userResolver;
    private final GradleDependencyResolver internalRepository;

    public DefaultIvyAdapter(Ivy ivy, GradleDependencyResolver internalRepository, ClientModuleRegistry clientModuleRegistry, ResolutionStrategyInternal resolutionStrategy) {
        this.ivy = ivy;
        this.clientModuleRegistry = clientModuleRegistry;
        this.resolutionStrategy = resolutionStrategy;
        this.internalRepository = internalRepository;
        userResolver = ivy.getSettings().getDefaultResolver();
        versionMatcher = ivy.getSettings().getVersionMatcher();
    }
    
    public ResolveData getResolveData(String configurationName) {
        ResolveOptions options = new ResolveOptions();
        options.setDownload(false);
        options.setConfs(WrapUtil.toArray(configurationName));
        return new ResolveData(ivy.getResolveEngine(), options);
    }

    public DependencyToModuleResolver getDependencyToModuleResolver(ResolveData resolveData) {
        DependencyToModuleResolver clientModuleResolver = new ClientModuleResolver(clientModuleRegistry);
        IvyResolverBackedDependencyToModuleResolver ivyBackedResolver = new IvyResolverBackedDependencyToModuleResolver(ivy, resolveData, userResolver, versionMatcher);
        PrimaryResolverChain primaryResolverChain = new PrimaryResolverChain(clientModuleResolver, internalRepository, ivyBackedResolver, null);
        return new VersionForcingDependencyToModuleResolver(primaryResolverChain, this.resolutionStrategy.getForcedModules());
    }

    public ArtifactToFileResolver getArtifactToFileResolver() {
        ArtifactToFileResolver ivyBackedResolver = new IvyResolverBackedArtifactToFileResolver(userResolver);
        return new PrimaryResolverChain(null, internalRepository, null, ivyBackedResolver);
    }
}
