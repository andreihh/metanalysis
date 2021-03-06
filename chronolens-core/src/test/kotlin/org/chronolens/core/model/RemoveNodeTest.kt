/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.model

import org.chronolens.test.core.model.assertEquals
import org.chronolens.test.core.model.sourceFile
import org.chronolens.test.core.model.sourceTree
import org.junit.Test
import kotlin.test.assertFailsWith

class RemoveNodeTest {
    @Test fun `test remove invalid id throws`() {
        assertFailsWith<IllegalArgumentException> {
            RemoveNode("src/Test.java:/")
        }
    }

    @Test fun `test remove source file`() {
        val expected = sourceTree {
            sourceFile("src/Test.java") {}
        }
        val edit = sourceFile("src/Main.java").remove()

        val actual = sourceTree {
            sourceFile("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
            sourceFile("src/Test.java") {}
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test remove function from type`() {
        val expected = sourceTree {
            sourceFile("src/Main.java") {
                type("Main") {
                    variable("version") {}
                }
            }
        }
        val edit = sourceFile("src/Main.java").type("Main")
            .function("getVersion(String)").remove()

        val actual = sourceTree {
            sourceFile("src/Main.java") {
                type("Main") {
                    variable("version") {}
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
        }
        actual.apply(edit)

        assertEquals(expected, actual)
    }

    @Test fun `test remove non-existing node throws`() {
        val sourceTree = sourceTree {
            sourceFile("src/Test.java") {
                function("getVersion()") {}
            }
        }
        val edit = sourceFile("src/Test.java").variable("version").remove()

        assertFailsWith<IllegalStateException> {
            sourceTree.apply(edit)
        }
    }
}
