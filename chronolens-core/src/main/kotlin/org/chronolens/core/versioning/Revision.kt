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

package org.chronolens.core.versioning

/**
 * A revision in a version control system (commit, tag, branch etc.).
 *
 * @property id the unique id of this revision
 * @property date the date at which this revision was committed in milliseconds
 * since the Unix epoch
 * @property author the author of this revision
 * @throws IllegalArgumentException if [date] is negative
 */
data class Revision(val id: String, val date: Long, val author: String) {
    init {
        require(date >= 0L) { "'$id' date '$date' can't be negative!" }
    }
}