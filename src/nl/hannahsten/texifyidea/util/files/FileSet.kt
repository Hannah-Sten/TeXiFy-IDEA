package nl.hannahsten.texifyidea.util.files

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * A toml file can be in any parent directory.
 */
fun VirtualFile.hasTectonicTomlFile() = findTectonicTomlFile() != null

fun VirtualFile.findTectonicTomlFile(): VirtualFile? {
    var parent = this
    for (i in 0..20) {
        if (parent.parent != null && parent.parent.isDirectory && parent.parent.exists()) {
            parent = parent.parent
        }
        else {
            break
        }

        parent.findFile("Tectonic.toml")?.let { return it }
    }
    return null
}

/**
 * Finds all the files in the project that are somehow related using includes.
 *
 * When A includes B and B includes C then A, B & C will all return a set containing A, B & C.
 *
 * @return All the files that are cross-referenced between each other.
 */
fun PsiFile.referencedFileSet(useIndexCache: Boolean = true): Set<PsiFile> {
    val project = this.project
    return LatexProjectStructure.getRelatedFilesFor(this).mapNotNull { it.findPsiFile(project) }.toSet()
//    return ReferencedFileSetService.getInstance().referencedFileSetOf(this, useIndexCache)
}

/**
 * Do not use this function
 */
fun PsiFile.commandsInFileSet(useIndexCache: Boolean = true): Collection<LatexCommands> {
    // TODO: Replace all the usage of this function
//    val res = LatexCommandsIndex.Util.getItemsInFileSet(this, useIndexCache)
//    val res = NewSpecialCommandsIndex.getAllInFileSet(this)
    // You can create breakpoints in the code to see the size of the returned collection.
    return emptyList()
}

fun PsiFile.findExternalDocumentCommand(): LatexCommands? {
    return NewCommandsIndex.getByNameInFileSet(
        LatexGenericRegularCommand.EXTERNALDOCUMENT.command, containingFile.originalFile
    )
        .firstOrNull()
}

/**
 * Get all the definitions and redefinitions in the file set.
 */
fun PsiFile.definitionsAndRedefinitionsInFileSet(): Collection<LatexCommands> {
    return NewSpecialCommandsIndex.getAllCommandDefInFileset(this)
}
