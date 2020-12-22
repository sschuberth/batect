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

package batect.docker.build.buildkit

import batect.docker.api.SessionStreams
import batect.docker.build.buildkit.services.HealthService
import okhttp3.internal.concurrent.TaskRunner
import okhttp3.internal.http2.Http2Connection
import okhttp3.internal.peerName

class BuildKitSession(
    val sessionId: String,
    val buildId: String,
    val name: String,
    val sharedKey: String
) : AutoCloseable {
    private lateinit var connection: Http2Connection

    fun start(streams: SessionStreams) {
        if (::connection.isInitialized) {
            throw UnsupportedOperationException("Connection already started, can't start again.")
        }

        streams.socket.soTimeout = 0

        val listener = GrpcListener(setOf(HealthService()))

        // TODO: replace this TaskRunner with a standard TaskRunner backend + a threadFactory that sets Thread.setUncaughtExceptionHandler

        val connection = Http2Connection.Builder(false, TaskRunner.INSTANCE)
            .socket(streams.socket, streams.socket.peerName(), streams.source, streams.sink)
            .listener(listener)
            .build()

        connection.start()
    }

    override fun close() {
        if (::connection.isInitialized) {
            connection.close()
        }
    }
}
