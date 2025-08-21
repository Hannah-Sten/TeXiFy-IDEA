package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexLibraryDefinitionService

class RefreshTexifyCachesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // Call a refresh of the fileset cache
        LatexLibraryDefinitionService.getInstance(project).invalidateCache()
        LatexDefinitionService.getInstance(project).requestRefresh()
    }
}