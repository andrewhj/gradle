/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.repositories;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.gradle.api.artifacts.Module;
import org.gradle.api.internal.artifacts.ivyservice.*;
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies.ProjectDependencyDescriptor;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.util.ReflectionUtil;

import java.io.File;

/**
 * @author Hans Dockter
 */
public class DefaultInternalRepository implements GradleDependencyResolver {
    private final ModuleDescriptorConverter moduleDescriptorConverter;

    public DefaultInternalRepository(ModuleDescriptorConverter moduleDescriptorConverter) {
        this.moduleDescriptorConverter = moduleDescriptorConverter;
    }

    public ModuleVersionResolver create(DependencyDescriptor dependencyDescriptor) {
        ModuleDescriptor moduleDescriptor = findProject(dependencyDescriptor);
        if (moduleDescriptor == null) {
            return null;
        }
        return new FixedModuleVersionResolver(dependencyDescriptor, moduleDescriptor);
    }

    private ModuleDescriptor findProject(DependencyDescriptor descriptor) {
        if (!(descriptor instanceof ProjectDependencyDescriptor)) {
            return null;
        }
        ProjectDependencyDescriptor projectDependencyDescriptor = (ProjectDependencyDescriptor) descriptor;
        ProjectInternal project = projectDependencyDescriptor.getTargetProject();
        Module projectModule = project.getModule();
        ModuleDescriptor projectDescriptor = moduleDescriptorConverter.convert(project.getConfigurations(), projectModule);

        for (DependencyArtifactDescriptor artifactDescriptor : descriptor.getAllDependencyArtifacts()) {
            for (Artifact artifact : projectDescriptor.getAllArtifacts()) {
                if (artifact.getName().equals(artifactDescriptor.getName()) && artifact.getExt().equals(
                        artifactDescriptor.getExt())) {
                    String path = artifact.getExtraAttribute(DefaultIvyDependencyPublisher.FILE_PATH_EXTRA_ATTRIBUTE);
                    ReflectionUtil.invoke(artifactDescriptor, "setExtraAttribute",
                            new Object[]{DefaultIvyDependencyPublisher.FILE_PATH_EXTRA_ATTRIBUTE, path});
                }
            }
        }

        return projectDescriptor;
    }


    public File resolve(Artifact artifact) {
        String path = artifact.getExtraAttribute(DefaultIvyDependencyPublisher.FILE_PATH_EXTRA_ATTRIBUTE);
        if (path == null) {
            return null;
        } 
        return new File(path);
    }
}
