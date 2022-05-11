package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.BibtexEntry

abstract class RemoteBibLibrary(val name: String) {

    abstract suspend fun getCollection(project: Project): List<BibtexEntry>
}