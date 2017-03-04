/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chronos.core.delta

import org.chronos.core.Node

import kotlin.reflect.KClass

sealed class NodeChange {
    data class AddNode(val node: Node) : NodeChange()

    data class RemoveNode(
            val type: KClass<out Node>,
            val identifier: String
    ) : NodeChange() {
        companion object {
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String
            ): RemoveNode = RemoveNode(T::class, identifier)
        }
    }

    data class ChangeNode<T : Node>(
            val type: KClass<T>,
            val identifier: String,
            val transaction: Transaction<T>
    ) : NodeChange() {
        companion object {
            @JvmStatic inline operator fun <reified T : Node> invoke(
                    identifier: String,
                    transaction: Transaction<T>
            ): ChangeNode<T> = ChangeNode(T::class, identifier, transaction)
        }
    }
}