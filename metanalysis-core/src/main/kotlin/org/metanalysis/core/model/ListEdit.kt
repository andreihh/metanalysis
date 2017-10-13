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

package org.metanalysis.core.model

/**
 * An atomic change which should be applied to a list of elements.
 *
 * @param T the type of the elements of the edited list
 */
sealed class ListEdit<T> : Edit<List<T>> {
    companion object {
        /**
         * Applies the given `edits` on this list and returns the result.
         *
         * @param T the type of the elements of the edited list
         * @param edits the edits which should be applied
         * @return the edited list
         * @throws IllegalStateException if this list has an invalid state and
         * the given `edits` couldn't be applied
         */
        @JvmStatic
        fun <T> List<T>.apply(edits: List<ListEdit<T>>): List<T> =
                edits.fold(toMutableList()) { list, edit ->
                    edit.applyOn(list)
                    list
                }

        /** Utility method. */
        @JvmStatic
        fun <T> List<T>.apply(vararg edits: ListEdit<T>): List<T> =
                apply(edits.asList())

        /**
         * Returns the edits which should be applied on this list to obtain the
         * `other` list.
         *
         * @param other the list which should be obtained
         * @return the edits which should be applied on this list
         */
        @JvmStatic
        fun <T> List<T>.diff(other: List<T>): List<ListEdit<T>> {
            var maxValue = 0
            val values = hashMapOf<T, Int>()
            (this + other).forEach {
                if (it !in values) {
                    values[it] = maxValue++
                }
            }
            val a = map(values::getValue).toTypedArray()
            val n = a.size
            val b = other.map(values::getValue).toTypedArray()
            val m = b.size
            val dp = Array(n + 1) { IntArray(m + 1) { Int.MAX_VALUE } }
            dp[0] = IntArray(m + 1) { it }
            for (i in 1..n) {
                dp[i][0] = i
                for (j in 1..m) {
                    dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                    else 1 + minOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
            val edits = arrayListOf<ListEdit<T>>()
            var i = n
            var j = m
            while (i > 0 || j > 0) {
                if (i > 0 && j > 0 && a[i - 1] == b[j - 1]) {
                    i--
                    j--
                } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                    edits.add(Remove(i - 1))
                    i--
                } else {
                    edits.add(Add(i, other[j - 1]))
                    j--
                }
            }
            return edits
        }
    }

    /**
     * Applies this edit on the given mutable list.
     *
     * @param subject the list which should be edited
     * @throws IllegalStateException if the list has an invalid state and this
     * edit couldn't be applied
     */
    protected abstract fun applyOn(subject: MutableList<T>)

    /**
     * Indicates that an element should be added to the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index at which the element should be inserted
     * @property value the element which should be added
     * @throws IllegalArgumentException if `index` is negative
     */
    data class Add<T>(val index: Int, val value: T) : ListEdit<T>() {
        init {
            require(index >= 0) { "Can't add $value at negative index $index!" }
        }

        override fun applyOn(subject: MutableList<T>) {
            check(index <= subject.size) {
                "$index is out of bounds for $subject!"
            }
            subject.add(index, value)
        }
    }

    /**
     * Indicates that an element should be removed from the edited list.
     *
     * @param T the type of the elements of the edited list
     * @property index the index of the element which should be removed
     * @throws IllegalArgumentException if `index` is negative
     */
    data class Remove<T>(val index: Int) : ListEdit<T>() {
        init {
            require(index >= 0) { "Can't remove negative index $index!" }
        }

        override fun applyOn(subject: MutableList<T>) {
            check(index < subject.size) {
                "$index is out of bounds for $subject!"
            }
            subject.removeAt(index)
        }
    }
}