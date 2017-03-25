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

package org.metanalysis.core.delta

import org.junit.Test

import org.metanalysis.core.Node.Function
import org.metanalysis.core.Node.Type
import org.metanalysis.core.Node.Variable
import org.metanalysis.core.SourceFile
import org.metanalysis.core.delta.SourceFileTransaction.Companion.diff
import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.java.JavaParser
import org.metanalysis.test.assertEquals

import java.net.URL

import kotlin.test.assertNull

class SourceFileTransactionTest {
    private fun assertDiff(src: SourceFile, dst: SourceFile) {
        assertEquals(src.apply(src.diff(dst)), dst)
        assertEquals(dst.apply(dst.diff(src)), src)
    }

    @Test fun `test add type`() {
        val addedType = Type("IInterface")
        val expected = SourceFile(setOf(addedType))
        val actual = SourceFile(emptySet()).apply(SourceFileTransaction(
                nodeEdits = listOf(NodeSetEdit.Add(addedType))
        ))
        assertEquals(expected, actual)
    }

    @Test fun `test diff equal empty source files`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(emptySet())
        assertDiff(src, dst)
    }

    @Test fun `test diff empty source to single type`() {
        val src = SourceFile(emptySet())
        val dst = SourceFile(setOf(Type("IClass")))
        assertDiff(src, dst)
    }

    @Test fun `test diff integration`() {
        val src = SourceFile(setOf(
                Type(
                        name = "IClass",
                        members = setOf(
                                Variable("version"),
                                Variable("name"),
                                Function("getVersion()", emptyList()),
                                Type("IInterface")
                        )
                ),
                Variable("DEBUG_LEVEL")
        ))
        val dst = SourceFile(setOf(
                Type(
                        name = "IClass",
                        supertypes = setOf("Comparable<IClass>"),
                        members = setOf(
                                Variable("version", "1"),
                                Function(
                                        signature = "getVersion()",
                                        parameters = emptyList(),
                                        body = "{\n  return version;\n}\n"
                                ),
                                Type(
                                        name = "IInterface",
                                        supertypes = setOf("IClass"),
                                        members = setOf(Variable("name"))
                                ),
                                Function(
                                        signature = "compare(IClass)",
                                        parameters = listOf(Variable("other")),
                                        body = "{return version-other.version;}"
                                )
                        )
                ),
                Variable("DEBUG", "true"),
                Function("main(String[])", listOf(Variable("args")), "{}")
        ))
        assertDiff(src, dst)
    }

    @Test fun `test diff network`() {
        val srcUrl = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/826e565b7cfba8de05f9f652c1541df8e8e7efe2/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val dstUrl = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/5e946c270018c71bf25778bc2dc25e5a9dd809b0/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
        val parser = JavaParser()
        val src = parser.parse(srcUrl)
        val dst = parser.parse(dstUrl)
        assertDiff(src, dst)
    }

    @Test fun `test apply null transaction returns equal source file`() {
        val sourceFile = SourceFile(setOf(Variable("version", "1")))
        assertEquals(sourceFile, sourceFile.apply(transaction = null))
    }

    @Test fun `test diff equal source files returns null`() {
        val sourceFile = SourceFile(setOf(
                Variable("version"),
                Type("IClass"),
                Function("getVersion()", emptyList())
        ))
        assertNull(sourceFile.diff(sourceFile))
    }
}
