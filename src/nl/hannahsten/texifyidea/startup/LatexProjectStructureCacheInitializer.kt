package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.util.files.LatexPackageLocation

/**
 * Initialize package location cache, because filling it takes a long time, we do not want to do that only at the moment we need it (when resolving references).
 */
class LatexProjectStructureCacheInitializer : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Not sure on which thread this is run, run in background to be sure
        withContext(Dispatchers.Default) {
            LatexPackageLocation.updateLocationWithKpsewhichSuspend(project)
            LatexProjectStructure.updateFilesetsNow(project)
        }
    }
}