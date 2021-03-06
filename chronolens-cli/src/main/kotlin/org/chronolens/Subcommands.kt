/*
 * Copyright 2017-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens

import org.chronolens.core.cli.Subcommand
import org.chronolens.core.cli.exit
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.repository.PersistentRepository.Companion.persist
import org.chronolens.core.repository.PersistentRepository.ProgressListener
import java.io.File

class LsTree : Subcommand() {
    override val help: String get() = """
        Prints all the interpretable files of the repository from the specified
        revision.
    """

    private val rev by option<String>()
        .help("the inspected revision (default: the <head> revision)")
        .validateRevision(::repository)

    private val repository by lazy(::connect)
    private val revision: String get() = rev ?: repository.getHeadId()

    override fun run() {
        repository.listSources(revision).forEach(::println)
    }
}

class RevList : Subcommand() {
    override val help: String get() = """
        Prints all revisions on the path from the currently checked-out (<head>)
        revision to the root of the revision tree / graph in chronological
        order.
    """

    override fun run() {
        val repository = connect()
        repository.listRevisions().forEach(::println)
    }
}

class Model : Subcommand() {
    override val help: String get() = """
        Prints the interpreted model of the source node with the specified id as
        it is found in the given revision of the repository.

        The path separator is '/', types are separated by ':' and functions and
        variables are separated by '#'.
    """

    private val id by option<String>()
        .help("the inspected source node").required().validateId()

    private val rev by option<String>()
        .help("the inspected revision (default: the <head> revision)")
        .validateRevision(::repository)

    private val repository by lazy(::connect)
    private val revision: String get() = rev ?: repository.getHeadId()

    override fun run() {
        val path = id.sourcePath
        val model = repository.getSource(path, revision)
            ?: exit("File '$path' couldn't be interpreted or doesn't exist!")
        val node = model.walkSourceTree()
            .find { it.qualifiedId == id }
            ?.sourceNode
            ?: exit("Source node '$id' doesn't exist!")
        PrettyPrinterVisitor(System.out).visit(node)
    }
}

class Persist : Subcommand() {
    override val help: String get() = """
        Connects to the repository and persists the source and history model
        from all the files that can be interpreted.

        The model is persisted in the '.chronolens' directory from the
        current working directory.
    """

    override fun run() {
        val repository = connect()
        repository.persist(
            File(repositoryDirectory),
            object : ProgressListener {
                private var sources = 0
                private var transactions = 0
                private var i = 0

                override fun onSnapshotStart(headId: String, sourceCount: Int) {
                    println("Persisting snapshot '$headId'...")
                    sources = sourceCount
                    i = 0
                }

                override fun onSourcePersisted(path: String) {
                    i++
                    print("Persisted $i / $sources sources...\r")
                }

                override fun onSnapshotEnd() {
                    println()
                    println("Done!")
                }

                override fun onHistoryStart(revisionCount: Int) {
                    println("Persisting transactions...")
                    transactions = revisionCount
                    i = 0
                }

                override fun onTransactionPersisted(id: String) {
                    i++
                    print("Persisted $i / $transactions transactions...\r")
                }

                override fun onHistoryEnd() {
                    println()
                    println("Done!")
                }
            }
        )
    }
}

class Clean : Subcommand() {
    override val help: String get() = """
        Deletes the previously persisted repository from the current working
        directory, if it exists.
    """

    override fun run() {
        PersistentRepository.clean(File(repositoryDirectory))
    }
}
