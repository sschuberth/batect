/*
    Copyright 2017-2021 Charles Korn.

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

@file:UseSerializers(
    ZonedDateTimeSerializer::class
)

package batect.telemetry

import batect.logging.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonPrimitive
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Serializable
data class TelemetrySpan(
    val type: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val attributes: Map<String, JsonPrimitive>
) {
    init {
        if (startTime.zone != ZoneOffset.UTC) {
            throw InvalidTelemetrySpanException("Span start time must be in UTC.")
        }

        if (endTime.zone != ZoneOffset.UTC) {
            throw InvalidTelemetrySpanException("Span end time must be in UTC.")
        }
    }
}

class InvalidTelemetrySpanException(message: String) : RuntimeException(message)
