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
import io.kotest.matchers.string.shouldNotContain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SkipPrerequisitesJourneyTest : Spek({
    describe("a task with a prerequisite being run with 'skip prerequisites' enabled") {
        val runner by createForGroup { ApplicationRunner("task-with-prerequisite") }

        on("running that task") {
            val result by runBeforeGroup { runner.runApplication(listOf("--skip-prerequisites", "do-stuff")) }

            it("prints the output from the main task") {
                result.asClue { it.output shouldContain "This is some output from the main task\n" }
            }

            it("does not print the output from the prerequisite task") {
                result.asClue { it.output shouldNotContain "This is some output from the build task\n" }
            }

            it("returns the exit code from that task") {
                result.asClue { it.exitCode shouldBe 123 }
            }
        }
    }
})
