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

package org.metanalysis.test.core.versioning

import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.FileNotFoundException
import java.util.Date

class VersionControlSystemMock(
        private val commits: List<CommitMock>
) : VersionControlSystem() {
    companion object {
        val NAME: String = "mockfs"
    }

    data class CommitMock(
            val id: String,
            val author: String,
            val date: Date,
            val changedFiles: Map<String, String?>
    )

    private fun CommitMock.toCommit(): Commit = Commit(id, author, date)

    private val files: Map<String, Map<String, String>>
    private val commitsById = commits.associate { it.id to it.toCommit() }

    init {
        val fileHistory = hashMapOf<String, Map<String, String>>()
        val currentFiles = hashMapOf<String, String>()
        commits.forEach { (id, _, _, changedFiles) ->
            changedFiles.forEach { path, src ->
                if (src == null) {
                    currentFiles -= path
                } else {
                    currentFiles[path] = src
                }
            }
            fileHistory[id] = currentFiles.toMap()
        }
        files = fileHistory
    }

    override val name: String
        get() = NAME

    override fun isSupported(): Boolean = true

    override fun detectRepository(): Boolean = true

    override fun getHead(): Commit = commits.last().toCommit()

    override fun listFiles(revisionId: String): Set<String> =
            requireNotNull(files[revisionId]).keys

    override fun getCommit(revisionId: String): Commit =
            requireNotNull(commitsById[revisionId])

    @Throws(FileNotFoundException::class)
    override fun getFile(revisionId: String, path: String): String =
            requireNotNull(files[revisionId])[path]
                    ?: throw FileNotFoundException("File '$path' not found!")

    override fun getFileHistory(
            revisionId: String,
            path: String
    ): List<Commit> {
        val history = commits
                .filter { path in it.changedFiles }
                .map { it.toCommit() }
        return if (history.isNotEmpty()) history
        else throw FileNotFoundException("File '$path' not found!")
    }
}