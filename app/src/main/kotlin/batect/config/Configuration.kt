/*
   Copyright 2017-2019 Charles Korn.

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

package batect.config

import batect.config.io.ConfigurationException
import batect.docker.DockerImageNameValidator
import batect.os.PathResolver
import com.charleskorn.kaml.YamlInput
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.decode
import kotlinx.serialization.internal.SerialClassDescImpl

@Serializable
data class Configuration(
    @SerialName("project_name") val projectName: String? = null,
    @Serializable(with = TaskMap.Companion::class) val tasks: TaskMap = TaskMap(),
    @Serializable(with = ContainerMap.Companion::class) val containers: ContainerMap = ContainerMap()
) {
    fun withResolvedProjectName(pathResolver: PathResolver): Configuration {
        if (projectName != null) {
            return this
        }

        if (pathResolver.relativeTo.root == pathResolver.relativeTo) {
            throw ConfigurationException("No project name has been given explicitly, but the configuration file is in the root directory and so a project name cannot be inferred.")
        }

        return this.copy(projectName = pathResolver.relativeTo.fileName.toString())
    }

    @Serializer(forClass = Configuration::class)
    companion object : KSerializer<Configuration> {
        private val projectNameFieldName = "project_name"
        private val tasksFieldName = "tasks"
        private val containersFieldName = "containers"

        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("Configuration") {
            init {
                addElement(projectNameFieldName, isOptional = true)
                addElement(tasksFieldName, isOptional = true)
                addElement(containersFieldName, isOptional = true)
            }
        }

        private val projectNameFieldIndex = descriptor.getElementIndex(projectNameFieldName)
        private val tasksFieldIndex = descriptor.getElementIndex(tasksFieldName)
        private val containersFieldIndex = descriptor.getElementIndex(containersFieldName)

        override fun deserialize(decoder: Decoder): Configuration {
            val input = decoder.beginStructure(descriptor) as YamlInput

            return deserializeFromObject(input).also { input.endStructure(descriptor) }
        }

        private fun deserializeFromObject(input: YamlInput): Configuration {
            var projectName: String? = null
            var tasks = TaskMap()
            var containers = ContainerMap()

            loop@ while (true) {
                when (val i = input.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    projectNameFieldIndex -> projectName = input.decodeProjectName(i)
                    tasksFieldIndex -> tasks = input.decode(TaskMap.Companion)
                    containersFieldIndex -> containers = input.decode(ContainerMap.Companion)
                    else -> throw SerializationException("Unknown index $i")
                }
            }

            return Configuration(projectName, tasks, containers)
        }

        private fun YamlInput.decodeProjectName(index: Int): String {
            val projectName = this.decodeStringElement(descriptor, index)

            if (!DockerImageNameValidator.isValidImageName(projectName)) {
                val location = this.getCurrentLocation()

                throw ConfigurationException(
                    "Invalid project name '$projectName'. The project name must be a valid Docker reference: it ${DockerImageNameValidator.validNameDescription}.",
                    location.line,
                    location.column
                )
            }

            return projectName
        }

        override fun serialize(encoder: Encoder, obj: Configuration): Unit = throw UnsupportedOperationException()
    }
}
