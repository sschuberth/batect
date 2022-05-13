/*
    Copyright 2017-2022 Charles Korn.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package batect.journeytests

import batect.journeytests.testutils.ApplicationRunner
import batect.testutils.createForGroup
import batect.testutils.on
import batect.testutils.runBeforeGroup
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object TaskWithCustomisationJourneyTest : Spek({
    describe("a task with customisations for a dependency container") {
        val runner by createForGroup { ApplicationRunner("task-with-customisation") }

        on("running that task") {
            val result by runBeforeGroup { runner.runApplication(listOf("--output=all", "the-task")) }

            it("prints the expected output from the dependency container indicating that the customisation has been applied") {
                result.asClue {
                    it.output shouldContain "dependency | Working directory is /customised"
                    it.output shouldContain "dependency | Value of CONTAINER_VAR is set on container"
                    it.output shouldContain "dependency | Value of OVERRIDDEN_VAR is overridden value from task"
                    it.output shouldContain "dependency | Value of NEW_VAR is new value from task"
                }
            }

            it("returns the exit code from that task") {
                result.asClue { it.exitCode shouldBe 0 }
            }
        }
    }
})
