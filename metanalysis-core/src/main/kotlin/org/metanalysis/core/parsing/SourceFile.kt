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

package org.metanalysis.core.parsing

import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.model.validateUnitId

/**
 * A raw source file that can be interpreted by parsers.
 *
 * @property path the path of the source file
 * @property rawContent the `UTF-8` encoded content of the file
 * @throws IllegalArgumentException if the given [path] is not a valid
 * [SourceUnit] path
 */
data class SourceFile(val path: String, val rawContent: String) {
    init {
        validateUnitId(path)
    }
}
