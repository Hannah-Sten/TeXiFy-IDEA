package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.BibtexEntry

abstract class ExternalBibLibrary(val name: String) {

    abstract suspend fun getCollection(project: Project): List<BibtexEntry>
}