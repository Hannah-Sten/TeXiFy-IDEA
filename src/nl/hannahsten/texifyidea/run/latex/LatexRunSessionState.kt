package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import java.nio.file.Path

data class LatexRunSessionState(
    val project: Project,
    val mainFile: VirtualFile,
    val outputDir: VirtualFile,
    val workingDirectory: Path,
    val distributionType: LatexDistributionType,
    val usesDefaultWorkingDirectory: Boolean,
    val latexSdk: Sdk?,
    val auxDir: VirtualFile? = null,
    val psiFile: SmartPsiElementPointer<PsiFile>? = null,
    /** Absolute path to the compiled document output file (for example the generated PDF/XDV). */
    var resolvedOutputFilePath: String? = null,
    val filesToCleanUp: MutableList<Path> = mutableListOf(),
    val directoriesToDeleteIfEmpty: MutableSet<Path> = mutableSetOf(),
) {

    fun addCleanupFile(file: Path) {
        filesToCleanUp.add(file)
    }

    fun addCleanupDirectoriesIfEmpty(files: Collection<Path>) {
        directoriesToDeleteIfEmpty.addAll(files)
    }
}
