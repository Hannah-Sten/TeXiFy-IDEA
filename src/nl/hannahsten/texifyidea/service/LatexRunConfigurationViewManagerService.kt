package nl.hannahsten.texifyidea.service

import com.intellij.build.AbstractViewManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class LatexRunConfigurationViewManagerService(project: Project) : AbstractViewManager(project) {

    override fun getViewName() = "LaTeX Run Output"
}
