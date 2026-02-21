package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.nio.file.Path

data class LatexRunExecutionState(
    var isFirstRunConfig: Boolean = true,
    var isLastRunConfig: Boolean = false,
    var hasBeenRun: Boolean = false,
    var isInitialized: Boolean = false,
    var initFingerprint: String? = null,
    var resolvedMainFile: VirtualFile? = null,
    var resolvedOutputDir: VirtualFile? = null,
    var resolvedAuxDir: VirtualFile? = null,
    var resolvedWorkingDirectory: Path? = null,
    var psiFile: SmartPsiElementPointer<PsiFile>? = null,
    var effectiveLatexmkCompileMode: LatexmkCompileMode? = null,
    var effectiveCompilerArguments: String? = null,
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
        initFingerprint = null
        resolvedMainFile = null
        resolvedOutputDir = null
        resolvedAuxDir = null
        resolvedWorkingDirectory = null
        effectiveLatexmkCompileMode = null
        effectiveCompilerArguments = null
    }

    fun resetAfterAuxChain() {
        isFirstRunConfig = true
        isLastRunConfig = false
        clearInitialization()
    }
}
