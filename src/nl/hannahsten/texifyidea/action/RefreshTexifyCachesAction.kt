package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexLibraryDefinitionService
import nl.hannahsten.texifyidea.index.projectstructure.LatexLibraryStructureService

class RefreshTexifyCachesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // Call a refresh of the fileset cache
        // we refresh the library cache only in internal mode (for debugging purpose), as the library normally does not change
        val isInternalMode = ApplicationManager.getApplication().isInternal
        if(isInternalMode) {
            LatexLibraryStructureService.getInstance(project).invalidateLibraryCache()
            LatexLibraryDefinitionService.getInstance(project).invalidateCache()
        }
        LatexDefinitionService.getInstance(project).requestRefresh()
    }
}