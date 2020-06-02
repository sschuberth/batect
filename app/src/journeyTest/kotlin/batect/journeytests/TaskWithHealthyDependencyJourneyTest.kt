/*
   Copyright 2017-2020 Charles Korn.

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
import batect.journeytests.testutils.exitCode
import batect.journeytests.testutils.output
import batect.testutils.createForGroup
import batect.testutils.on
import batect.testutils.runBeforeGroup
import ch.tutteli.atrium.api.verbs.assert
import ch.tutteli.atrium.api.fluent.en_GB.contains
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object TaskWithHealthyDependencyJourneyTest : Spek({
    describe("a task with a healthy dependency") {
        val runner by createForGroup { ApplicationRunner("task-with-healthy-dependency") }

        on("running that task") {
            val result by runBeforeGroup { runner.runApplication(listOf("the-task")) }

            it("displays the output from that task") {
                assert(result).output().contains("Status code for request: 200")
            }

            it("returns the exit code from that task") {
                assert(result).exitCode().toBe(0)
            }
        }
    }
})
