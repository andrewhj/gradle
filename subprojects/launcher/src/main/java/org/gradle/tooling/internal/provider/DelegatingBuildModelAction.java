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
package org.gradle.tooling.internal.provider;

import org.gradle.BuildResult;
import org.gradle.GradleLauncher;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.initialization.DefaultGradleLauncher;
import org.gradle.initialization.GradleLauncherAction;
import org.gradle.util.UncheckedException;

import java.io.Serializable;

class DelegatingBuildModelAction implements GradleLauncherAction, Serializable {
    private transient GradleLauncherAction action;
    private final Class type;

    public DelegatingBuildModelAction(Class type) {
        this.type = type;
    }

    public Object getResult() {
        return action.getResult();
    }

    public BuildResult run(GradleLauncher launcher) {
        loadAction((DefaultGradleLauncher) launcher);
        return action.run(launcher);
    }

    private void loadAction(DefaultGradleLauncher launcher) {
        DefaultGradleLauncher gradleLauncher = launcher;
        ClassLoaderRegistry classLoaderRegistry = gradleLauncher.getGradle().getServices().get(ClassLoaderRegistry.class);
        try {
            //TODO SF - horrible, ugly
            if (type.toString().endsWith("org.gradle.tooling.model.idea.IdeaProject")) {
                action = (GradleLauncherAction) classLoaderRegistry.getRootClassLoader()
                    .loadClass("org.gradle.tooling.internal.provider.BuildIdeaModelAction").getConstructor(Class.class).newInstance(type);
            } else {
                action = (GradleLauncherAction) classLoaderRegistry.getRootClassLoader()
                    .loadClass("org.gradle.tooling.internal.provider.BuildModelAction").getConstructor(Class.class).newInstance(type);
            }
        } catch (Exception e) {
            throw UncheckedException.asUncheckedException(e);
        }
    }
}
