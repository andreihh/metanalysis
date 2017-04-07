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

package org.metanalysis.core.versioning

import java.io.IOException

/** Signals that a VCS subprocess failed to complete successfully. */
sealed class SubprocessException(
        message: String?,
        cause: Throwable?
) : IOException(message, cause) {
    /**
     * Signals that the current thread was interrupted while waiting for the
     * running VCS subprocess to finish.
     */
    class SubprocessInterruptedException(
            message: String? = null,
            cause: Throwable? = null
    ) : SubprocessException(message, cause)

    /** Signals that the running VCS subprocess terminated abnormally. */
    class SubprocessTerminatedException(
            message: String? = null,
            cause: Throwable? = null
    ) : SubprocessException(message, cause)
}
