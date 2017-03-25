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

import org.metanalysis.core.delta.ListEdit.Companion.apply
import org.metanalysis.core.delta.ListEdit.Companion.diff

import kotlin.test.assertEquals

class ListEditTest {
    private fun String.apply(edits: List<ListEdit<Char>>): String =
            toList().apply(edits).joinToString(separator = "")

    private fun String.apply(vararg edits: ListEdit<Char>): String =
            toList().apply(*edits).joinToString(separator = "")

    private fun assertDiff(src: String, dst: String) {
        assertEquals(dst, src.apply(src.toList().diff(dst.toList())))
        assertEquals(src, dst.apply(dst.toList().diff(src.toList())))
    }

    @Test fun `test add element at end`() {
        val expected = "12345"
        val actual = "1234".apply(ListEdit.Add(4, '5'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element at front`() {
        val expected = "12345"
        val actual = "2345".apply(ListEdit.Add(0, '1'))
        assertEquals(expected, actual)
    }

    @Test fun `test add element in middle`() {
        val expected = "12345"
        val actual = "1245".apply(ListEdit.Add(2, '3'))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test add element out of bounds throws`() {
        "1234".apply(ListEdit.Add(5, '6'))
    }

    @Test fun `test remove first element`() {
        val expected = "2345"
        val actual = "12345".apply(ListEdit.Remove(0))
        assertEquals(expected, actual)
    }

    @Test fun `test remove last element`() {
        val expected = "1234"
        val actual = "12345".apply(ListEdit.Remove(4))
        assertEquals(expected, actual)
    }

    @Test fun `test remove middle element`() {
        val expected = "1245"
        val actual = "12345".apply(ListEdit.Remove(2))
        assertEquals(expected, actual)
    }

    @Test fun `test replace first element`() {
        val expected = "02345"
        val actual = "12345".apply(ListEdit.Replace(0, '0'))
        assertEquals(expected, actual)
    }

    @Test fun `test replace last element`() {
        val expected = "12340"
        val actual = "12345".apply(ListEdit.Replace(4, '0'))
        assertEquals(expected, actual)
    }

    @Test fun `test replace middle element`() {
        val expected = "12045"
        val actual = "12345".apply(ListEdit.Replace(2, '0'))
        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `test replace element out of bounds throws`() {
        "1234".apply(ListEdit.Replace(4, '5'))
    }

    @Test(expected = IllegalStateException::class)
    fun `test remove element out of bounds throws`() {
        "1234".apply(ListEdit.Remove(4))
    }

    @Test fun `test chained edits`() {
        val expected = "republican"
        val actual = "democrats".apply(
                ListEdit.Remove(8), // democrat
                ListEdit.Replace(7, 'n'), // democran
                ListEdit.Replace(5, 'c'), // democcan
                ListEdit.Replace(4, 'i'), // demoican
                ListEdit.Replace(3, 'l'), // demlican
                ListEdit.Replace(2, 'b'), // deblican
                ListEdit.Add(2, 'u'), // deublican
                ListEdit.Add(2, 'p'), // depublican
                ListEdit.Replace(0, 'r') // republican
        )
        assertEquals(expected, actual)
    }

    @Test fun `test diff`() {
        val src = "democrats"
        val dst = "republican"
        assertDiff(src, dst)
    }
}