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

package org.metanalysis.git

import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.Revision
import org.metanalysis.core.versioning.VcsProxy

internal class GitProxy(private val prefix: String) : VcsProxy {
    private val vcs = "git"
    private val headId = "HEAD"
    private val format = "--format=%ct:%an"

    private fun formatCommits(rawCommits: String): List<String> {
        val lines = rawCommits.lines()
        return (0 until lines.size - 1 step 2).map { i ->
            "${lines[i]}:${lines[i + 1]}".removePrefix("commit ")
        }
    }

    private fun parseCommit(formattedCommit: String): Revision {
        val (id, rawDate, author) =
                formattedCommit.split(delimiters = ':', limit = 3)
        val date = rawDate.toLong() * 1000
        return Revision(id, date, author)
    }

    private fun parseFiles(rawFileSet: String): Set<String> =
            rawFileSet.lines().filter(String::isNotBlank).toSet()

    private fun validateRevision(revisionId: String) {
        val result = execute(vcs, "cat-file", "-e", "$revisionId^{commit}")
        require(result.isSuccess) { "Revision '$revisionId' doesn't exist!" }
    }

    override fun getHead(): Revision =
            getRevision(headId) ?: error("'$headId' must exist!")

    override fun getRevision(revisionId: String): Revision? {
        val result =
                execute(vcs, "rev-list", "-1", format, "$revisionId^{commit}")
        val rawCommit = result.getOrNull() ?: return null
        val formattedCommit = formatCommits(rawCommit).single()
        return parseCommit(formattedCommit)
    }

    override fun getChangeSet(revisionId: String): Set<String> {
        validateRevision(revisionId)
        val result = execute(
                vcs, "diff-tree", "-m", "-r", "--root",
                "--name-only", "--relative", "--no-commit-id",
                revisionId
        )
        val rawChangeSet = result.get()
        return parseFiles(rawChangeSet)
    }

    override fun listFiles(revisionId: String): Set<String> {
        validateRevision(revisionId)
        val result = execute(vcs, "ls-tree", "-r", "--name-only", revisionId)
        val rawFileSet = result.get()
        return parseFiles(rawFileSet)
    }

    override fun getFile(revisionId: String, path: String): String? {
        validateRevision(revisionId)
        return execute(vcs, "cat-file", "blob", "$revisionId:$prefix$path")
                .getOrNull()
    }

    override fun getHistory(path: String): List<Revision> {
        val result = execute(
                vcs, "rev-list", "--first-parent", "--reverse", format,
                headId, "--", path
        )
        val rawCommits = result.get()
        val formattedCommits = formatCommits(rawCommits)
        return formattedCommits.map(this::parseCommit)
    }
}
