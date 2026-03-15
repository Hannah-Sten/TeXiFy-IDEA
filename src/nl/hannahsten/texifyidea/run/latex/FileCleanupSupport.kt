package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.util.files.LatexTemporaryBuildArtifacts
import nl.hannahsten.texifyidea.util.runWriteAction
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal object FileCleanupSupport {

    data class DeleteResult(
        val deletedCount: Int,
        val failedPaths: List<Path>,
    )

    fun collectRunTargets(context: LatexRunStepContext): Set<Path> {
        val targets = linkedSetOf<Path>()
        val session = context.session
        val mainDirectory = Path.of(session.mainFile.parent.path)
        val mainBaseName = session.mainFile.nameWithoutExtension

        session.filesToCleanUp.forEach { targets.add(normalize(it)) }

        linkedSetOf(
            Path.of(session.outputDir.path),
            session.auxDir?.path?.let(Path::of),
        ).filterNotNull()
            .filterNot { samePath(it, mainDirectory) }
            .forEach { collectRecursiveMatches(it, targets) }

        collectTopLevelMatches(
            root = mainDirectory,
            matcher = { LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(it, mainBaseName) },
            targets = targets,
        )

        return targets
    }

    fun collectProjectTemporaryBuildTargets(projectRoot: Path): Set<Path> {
        val targets = linkedSetOf<Path>()
        collectRecursiveMatches(projectRoot, targets)
        return targets
    }

    fun delete(paths: Collection<Path>): DeleteResult {
        if (paths.isEmpty()) {
            return DeleteResult(0, emptyList())
        }

        var deletedCount = 0
        val failedPaths = mutableListOf<Path>()
        val normalizedPaths = paths.map(::normalize)

        val deleteAction = {
            val fileSystem = LocalFileSystem.getInstance()
            normalizedPaths.forEach { path ->
                val file = fileSystem.refreshAndFindFileByPath(path.absolutePathString()) ?: return@forEach
                runCatching {
                    file.delete(this)
                    deletedCount++
                }.onFailure {
                    failedPaths.add(path)
                }
            }
        }

        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            runWriteAction(deleteAction)
        }
        else {
            application.invokeAndWait {
                runWriteAction(deleteAction)
            }
        }

        return DeleteResult(deletedCount, failedPaths)
    }

    private fun collectRecursiveMatches(root: Path, targets: MutableSet<Path>) {
        val rootFile = root.toFile()
        if (!rootFile.isDirectory) {
            return
        }

        rootFile.walkTopDown()
            .filter { it.isFile && LatexTemporaryBuildArtifacts.matches(it.toPath()) }
            .forEach { targets.add(normalize(it.toPath())) }
    }

    private fun collectTopLevelMatches(
        root: Path,
        matcher: (Path) -> Boolean,
        targets: MutableSet<Path>,
    ) {
        val rootFile = root.toFile()
        if (!rootFile.isDirectory) {
            return
        }

        rootFile.listFiles().orEmpty()
            .asSequence()
            .filter { it.isFile && matcher(it.toPath()) }
            .forEach { targets.add(normalize(it.toPath())) }
    }

    private fun samePath(left: Path, right: Path): Boolean = normalize(left) == normalize(right)

    private fun normalize(path: Path): Path = runCatching {
        path.toRealPath()
    }.getOrElse {
        path.toAbsolutePath().normalize()
    }
}
