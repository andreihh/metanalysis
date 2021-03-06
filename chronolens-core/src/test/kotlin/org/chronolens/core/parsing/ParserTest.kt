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

package org.chronolens.core.parsing

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParserTest {
    @Test fun `test can parse language with provided parser returns true`() {
        assertTrue(Parser.canParse("Test.mock"))
    }

    @Test fun `test can parse unknown language returns false`() {
        assertFalse(Parser.canParse("file.mp3"))
    }

    @Test fun `test parse source with errors returns syntax error`() {
        val result = Parser.parse(
            path = "res.mock",
            rawSource = "{\"invalid\":2"
        )
        assertEquals(Result.SyntaxError, result)
    }

    @Test fun `test parse invalid path throws`() {
        assertFailsWith<IllegalArgumentException> {
            Parser.parse(
                path = "res:/Test.mock",
                rawSource = """
                    {
                      "@class": "SourceFile",
                      "id": "res:/Test.mock"
                    }
                """.trimIndent()
            )
        }
    }

    @Test fun `test parsed source with different path throws`() {
        assertFailsWith<IllegalStateException> {
            Parser.parse(
                path = "Test.mock",
                rawSource = """
                    {
                      "@class": "SourceFile",
                      "id": "Main.mock"
                    }
                """.trimIndent()
            )
        }
    }
}
