package nl.hannahsten.texifyidea.startup

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.util.hasLatexModule
import nl.hannahsten.texifyidea.util.hasLatexRunConfigurations

/**
 * Initialize package location cache, because filling it takes a long time, we do not want to do that only at the moment we need it (when resolving references).
 */
class LatexProjectCacheInitializer : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (ApplicationManager.getApplication().isUnitTestMode) return
        val isLatexProject = project.run {
            hasLatexModule() || hasLatexRunConfigurations()
        }
        if (!isLatexProject) return
        withContext(Dispatchers.Default) {
            // Not sure on which thread this is run, run in background to be sure
            val fileset = LatexProjectStructure.updateFilesetsSuspend(project)
            LatexDefinitionService.getInstance(project).ensureRefreshFileset(fileset)
            // there will be an exception if we try to restart the daemon in unit tests
            // see FileStatusMap.CHANGES_NOT_ALLOWED_DURING_HIGHLIGHTING
            DaemonCodeAnalyzer.getInstance(project).restart()
        }
    }
}