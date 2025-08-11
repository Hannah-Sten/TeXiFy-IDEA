package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure

class RefreshFilesetAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // Call a refresh of the fileset cache
        LatexProjectStructure.getFilesets(project, callRefresh = true)
        LatexDefinitionService.getInstance(project).requestRefresh()
    }
}