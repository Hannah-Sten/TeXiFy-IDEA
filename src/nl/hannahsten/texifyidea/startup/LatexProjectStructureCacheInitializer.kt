package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation
import nl.hannahsten.texifyidea.util.isLatexProject

/**
 * Initialize package location cache, because filling it takes a long time, we do not want to do that only at the moment we need it (when resolving references).
 */
class LatexProjectStructureCacheInitializer : ProjectActivity {

    override suspend fun execute(project: Project) {
        if(ApplicationManager.getApplication().isUnitTestMode) return
        val isLatexProject = readAction {
            project.isLatexProject()
        }
        if(!isLatexProject) return
        withContext(Dispatchers.Default) {
            // Not sure on which thread this is run, run in background to be sure
            LatexPackageLocation.updateLocationWithKpsewhichSuspend(project)
            LatexProjectStructure.updateFilesetsSuspend(project)
        }
    }
}