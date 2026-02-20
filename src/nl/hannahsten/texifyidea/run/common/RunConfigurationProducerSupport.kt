package nl.hannahsten.texifyidea.run.common

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType

internal data class LatexContextFile(
    val container: PsiFile,
    val mainFile: VirtualFile,
)

internal fun resolveLatexContextFile(context: ConfigurationContext): LatexContextFile? {
    val location = context.location ?: return null
    val container = location.psiElement.containingFile ?: return null
    val mainFile = container.virtualFile ?: return null
    return LatexContextFile(container, mainFile)
}

internal fun isTexFile(file: VirtualFile): Boolean {
    val extension = file.extension ?: return false
    return extension.equals(LatexFileType.defaultExtension, ignoreCase = true)
}

internal fun isSameContextFile(configMainFile: VirtualFile?, context: ConfigurationContext): Boolean {
    val psiFile = context.dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
    val currentFile = psiFile.virtualFile ?: return false
    return configMainFile?.path == currentFile.path
}
