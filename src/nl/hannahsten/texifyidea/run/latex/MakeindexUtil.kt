package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.parser.toStringMap
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PackageMagic

/**
 * Try to find out which index program the user wants to use, based on the given options.
 * This can be multiple, if the user for example uses an index and a glossary.
 */
fun getDefaultMakeindexPrograms(mainFile: VirtualFile?, project: Project, usedPackages: Collection<LatexPackage>): Set<MakeindexProgram> {
    val indexPackageOptions = getIndexPackageOptions(mainFile, project)
    val makeindexOptions = getMakeindexOptions(mainFile, project)

    val indexPrograms = mutableSetOf<MakeindexProgram>()

    if (usedPackages.intersect(PackageMagic.index).isNotEmpty()) {
        val makeindexProgram = if (indexPackageOptions.contains("xindy")) MakeindexProgram.XINDY else MakeindexProgram.MAKEINDEX
        indexPrograms.add(makeindexProgram)
    }

    if (LatexPackage.GLOSSARIES in usedPackages) {
        val glossaryProgram = if (SystemEnvironment.isAvailable("perl")) {
            MakeindexProgram.MAKEGLOSSARIES
        }
        else {
            MakeindexProgram.MAKEGLOSSARIESLITE
        }
        indexPrograms.add(glossaryProgram)
    }
    else if (LatexPackage.GLOSSARIESEXTRA in usedPackages && "record" in indexPackageOptions) {
        indexPrograms.add(MakeindexProgram.BIB2GLS)
    }

    // Possible extra settings to override the indexProgram, see the imakeidx docs
    if (makeindexOptions.contains("makeindex")) {
        indexPrograms.add(MakeindexProgram.MAKEINDEX)
    }
    else if (makeindexOptions.contains("xindy") || makeindexOptions.contains("texindy")) {
        indexPrograms.add(MakeindexProgram.XINDY)
    }
    else if (makeindexOptions.contains("truexindy")) {
        indexPrograms.add(MakeindexProgram.TRUEXINDY)
    }

    return indexPrograms
}

/**
 * Get package options for included index packages.
 */
private fun getIndexPackageOptions(mainFile: VirtualFile?, project: Project): List<String> {
    // Find index package options
    val mainPsiFile = runReadAction { mainFile?.psiFile(project) } ?: throw ExecutionException("Main file not found")
//    return LatexCommandsIndex.Util.getItemsInFileSet(mainPsiFile)
//        .filter { it.commandToken.text in CommandMagic.packageInclusionCommands }
//        .filter { command -> command.getRequiredParameters().any { it in PackageMagic.index.map { pkg -> pkg.name } || it in PackageMagic.glossary.map { pkg -> pkg.name } } }
//        .flatMap { it.getOptionalParameterMap().toStringMap().keys }
    return runReadAction {
        NewCommandsIndex.getByNames(CommandMagic.packageInclusionCommands, mainPsiFile).asSequence()
            .filter { command -> command.requiredParametersText().any { it in PackageMagic.indexNames || it in PackageMagic.glossaryNames } }
            .flatMap { it.getOptionalParameterMap().toStringMap().keys }
            .toList()
    }
}

/**
 * Get optional parameters of the \makeindex command. If an option key does not have a value it will map to the empty string.
 */
fun getMakeindexOptions(mainFile: VirtualFile?, project: Project): Map<String, String> {
    val mainPsiFile = runReadAction { mainFile?.psiFile(project) }
    if (mainPsiFile == null) {
        Notification("LaTeX", "Could not find main file ${mainFile?.path}", "Please make sure the main file exists.", NotificationType.ERROR).notify(project)
        return mapOf()
    }

    val makeindexOptions = HashMap<String, String>()
    runReadAction {
        NewCommandsIndex.getByName("\\makeindex", mainPsiFile)
            .forEach {
                makeindexOptions.putAll(it.getOptionalParameterMap().toStringMap())
            }
    }
    return makeindexOptions
}