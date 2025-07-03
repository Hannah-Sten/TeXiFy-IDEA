package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

abstract class CacheService

@Service(Service.Level.PROJECT)
class LatexProjectCacheService(
    val project: Project,
    val coroutineScope: CoroutineScope
) {

    companion object {
        fun getInstance(project: Project): LatexProjectCacheService {
            return project.getService(LatexProjectCacheService::class.java)
        }
    }
}

// fun main() {
//    CachedValuesManager.getManager(TODO()).createCachedValue {
//        CachedValueProvider.Result.create()
//    }
// }