package nl.hannahsten.texifyidea.bibreferencemanagers

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.BibtexEntry

interface ReferenceManager {

    suspend fun getCollection(project: Project): List<BibtexEntry>
}