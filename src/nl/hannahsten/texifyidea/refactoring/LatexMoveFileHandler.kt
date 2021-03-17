package nl.hannahsten.texifyidea.refactoring

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler
import com.intellij.usageView.UsageInfo
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.allFiles
import nl.hannahsten.texifyidea.util.files.psiFile

class LatexMoveFileHandler : MoveFileHandler() {
    override fun canProcessElement(element: PsiFile?) = element is LatexFile

    override fun prepareMovedFile(file: PsiFile?, moveDestination: PsiDirectory?, oldToNewMap: MutableMap<PsiElement, PsiElement>?) {
        // Fill oldToNewMap for retargetUsages
        oldToNewMap?.set(file ?: return, moveDestination ?: return)
    }

    override fun findUsages(
        psiFile: PsiFile?,
        newParent: PsiDirectory?,
        searchInComments: Boolean,
        searchInNonJavaFiles: Boolean
    ): MutableList<UsageInfo> {
        val project = psiFile?.project ?: return mutableListOf()
        return LatexIncludesIndex.getItems(project)
            .flatMap { it.references.filterIsInstance<InputFileReference>() }
            .map { UsageInfo(it) }
            .toMutableList()
    }

    override fun retargetUsages(usageInfos: MutableList<UsageInfo>?, oldToNewMap: MutableMap<PsiElement, PsiElement>?) {
        usageInfos?.forEach {
            val newName = oldToNewMap?.get(it.file ?: return@forEach) as? PsiDirectory ?: return@forEach
            it.reference?.handleElementRename(newName.name)
        }
    }

    override fun updateMovedFile(file: PsiFile?) {

    }
}