package nl.hannahsten.texifyidea.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class TexifyProjectCacheService(project: Project, coroutineScope: CoroutineScope) : GenericCacheService<Project>(project, coroutineScope) {

    companion object {
        fun getInstance(project: Project): TexifyProjectCacheService = project.service()
    }
}