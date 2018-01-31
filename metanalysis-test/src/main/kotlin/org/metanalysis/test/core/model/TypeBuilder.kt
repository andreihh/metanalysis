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

package org.metanalysis.test.core.model

import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.Type
import org.metanalysis.test.core.Init
import org.metanalysis.test.core.apply

class TypeBuilder(private val name: String) : EntityBuilder<Type> {
    private var modifiers = setOf<String>()
    private var supertypes = setOf<String>()
    private val members = arrayListOf<EntityBuilder<*>>()

    private inline fun <reified T : EntityBuilder<*>> addMember(
        simpleId: String,
        init: Init<T>
    ): TypeBuilder {
        members += newBuilder<T>(simpleId).apply(init)
        return this
    }

    fun modifiers(vararg modifiers: String): TypeBuilder {
        this.modifiers = modifiers.requireNoDuplicates()
        return this
    }

    fun supertypes(vararg supertypes: String): TypeBuilder {
        this.supertypes = supertypes.requireNoDuplicates()
        return this
    }

    fun type(name: String, init: Init<TypeBuilder>): TypeBuilder =
        addMember(name, init)

    fun function(signature: String, init: Init<FunctionBuilder>): TypeBuilder =
        addMember(signature, init)

    fun variable(name: String, init: Init<VariableBuilder>): TypeBuilder =
        addMember(name, init)

    override fun build(parentId: String): Type {
        val id = "$parentId$ENTITY_SEPARATOR$name"
        return Type(
            id = id,
            modifiers = modifiers,
            supertypes = supertypes,
            members = members.map { it.build(id) }
        )
    }
}
