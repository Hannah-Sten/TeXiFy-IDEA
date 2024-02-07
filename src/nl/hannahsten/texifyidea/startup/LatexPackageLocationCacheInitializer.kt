package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.util.files.LatexPackageLocationCache

/**
 * Initialize package location cache, because filling it takes a long time, we do not want to do that only at the moment we need it (when resolving references).
 */
class LatexPackageLocationCacheInitializer : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Not sure on which thread this is run, run in background to be sure
        CoroutineScope(Dispatchers.Default).launch {
            LatexPackageLocationCache.fillCacheWithKpsewhich(project)
        }
    }
}