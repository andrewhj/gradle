/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.performance

import org.gradle.performance.categories.NativePerformanceTest
import org.junit.experimental.categories.Category
import spock.lang.Unroll

import static org.gradle.performance.measure.Duration.millis

@Category([NativePerformanceTest])
class NativeBuildPerformanceTest extends AbstractCrossVersionPerformanceTest {
    @Unroll('Project #type native build')
    def "build" () {
        given:
        runner.testId = "native build ${type}"
        runner.testProject = "${type}Native"
        runner.tasksToRun = ["clean", "assemble"]
        runner.maxExecutionTimeRegression = maxExecutionTimeRegression
        runner.targetVersions = ['2.11', 'last']
        runner.useDaemon = true

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()

        where:
        type           | maxExecutionTimeRegression
        "small"        | millis(500)
        "medium"       | millis(500)
        "big"          | millis(2500)
        "multi"        | millis(2500)
    }

    def "Many projects native build" () {
        given:
        runner.testId = "native build many projects"
        runner.testProject = "manyProjectsNative"
        runner.tasksToRun = ["clean", "assemble"]
        runner.maxExecutionTimeRegression = millis(1000)
        runner.targetVersions = ['2.11', 'last']
        runner.useDaemon = true

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()
    }
}
