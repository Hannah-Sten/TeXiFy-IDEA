package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.nio.file.Path

data class LatexRunSessionState(
    var resolvedMainFile: VirtualFile? = null,
    var resolvedOutputDir: VirtualFile? = null,
    var resolvedAuxDir: VirtualFile? = null,
    var resolvedWorkingDirectory: Path? = null,
    /** Absolute path to the compiled document output file (for example the generated PDF/XDV). */
    var resolvedOutputFilePath: String? = null,
    var psiFile: SmartPsiElementPointer<PsiFile>? = null,
    var effectiveLatexmkCompileMode: LatexmkCompileMode? = null,
    var effectiveCompilerArguments: String? = null,
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
