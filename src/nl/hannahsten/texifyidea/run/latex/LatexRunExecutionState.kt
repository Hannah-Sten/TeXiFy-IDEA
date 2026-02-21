package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.io.File
import java.nio.file.Path

data class LatexRunExecutionState(
    var isFirstRunConfig: Boolean = true,
    var isLastRunConfig: Boolean = false,
    var hasBeenRun: Boolean = false,
    var isInitialized: Boolean = false,
    var resolvedMainFile: VirtualFile? = null,
    var resolvedOutputDir: VirtualFile? = null,
    var resolvedAuxDir: VirtualFile? = null,
    var resolvedWorkingDirectory: Path? = null,
    /** Absolute path to the compiled document output file (for example the generated PDF/XDV). */
    var resolvedOutputFilePath: String? = null,
    var psiFile: SmartPsiElementPointer<PsiFile>? = null,
    var effectiveLatexmkCompileMode: LatexmkCompileMode? = null,
    var effectiveCompilerArguments: String? = null,
    val filesToCleanUp: MutableList<File> = mutableListOf(),
    val directoriesToDeleteIfEmpty: MutableSet<File> = mutableSetOf(),
) {

    fun beginAuxChain() {
        isFirstRunConfig = false
    }

    fun markLastPass() {
        isLastRunConfig = true
    }

    fun markIntermediatePass() {
        isLastRunConfig = false
    }

    fun markHasRun() {
        hasBeenRun = true
    }

    fun prepareForManualRun() {
        isLastRunConfig = false
        hasBeenRun = false
        clearInitialization()
    }

    fun clearInitialization() {
        isInitialized = false
        resolvedMainFile = null
        resolvedOutputDir = null
        resolvedAuxDir = null
        resolvedWorkingDirectory = null
        resolvedOutputFilePath = null
        effectiveLatexmkCompileMode = null
        effectiveCompilerArguments = null
        filesToCleanUp.clear()
        directoriesToDeleteIfEmpty.clear()
    }

    fun addCleanupFile(file: File) {
        filesToCleanUp.add(file)
    }

    fun addCleanupDirectoriesIfEmpty(files: Collection<File>) {
        directoriesToDeleteIfEmpty.addAll(files)
    }

    fun resetAfterAuxChain() {
        isFirstRunConfig = true
        isLastRunConfig = false
        clearInitialization()
    }
}