package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.run.legacy.makeindex.RunMakeindexListener
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import java.io.File

// todo makeindex
object IndexStepProvider {
//    private fun runMakeindexIfNeeded(handler: KillableProcessHandler, mainFile: VirtualFile, filesToCleanUp: MutableList<File>): Boolean {
//        var isMakeindexNeeded = false
//
//        // To find out whether makeindex is needed is relatively expensive,
//        // so we only do this the first time
//        if (!runConfig.options.hasBeenRun) {
//            val commandsInFileSet = mainFile.psiFile(environment.project)?.commandsInFileSet()?.mapNotNull { it.name } ?: emptyList()
//
//            // Option 1 in http://mirrors.ctan.org/macros/latex/contrib/glossaries/glossariesbegin.pdf
//            val usesTexForGlossaries = "\\" + LatexGenericRegularCommand.MAKENOIDXGLOSSARIES.commandWithSlash in commandsInFileSet
//
//            if (usesTexForGlossaries) {
//                // todo ensure that there are at least two LaTeX steps
////                runConfig.compileTwice = true
//            }
//
//            // If no index package is used, we assume we won't have to run makeindex
//            val includedPackages = runConfig.options.mainFile.resolve()
//                ?.psiFile(runConfig.project)
//                ?.includedPackages()
//                ?: setOf()
//
//            isMakeindexNeeded = includedPackages.intersect(PackageMagic.index + PackageMagic.glossary)
//                .isNotEmpty() && runConfig.options.compiler?.includesMakeindex == false && !usesTexForGlossaries
//
//            // Some packages do handle makeindex themselves
//            // Note that when you use imakeidx with the noautomatic option it won't, but we don't check for that
//            val usesAuxilOrOutDir = !runConfig.options.outputPath.isMainFileParent(mainFile, runConfig.project) || !runConfig.options.auxilPath.isMainFileParent(mainFile, runConfig.project)
//            if (includedPackages.contains(LatexPackage.IMAKEIDX) && !usesAuxilOrOutDir) {
//                isMakeindexNeeded = false
//            }
//        }
//
//        // Run makeindex when applicable
//        if (runConfig.isFirstRunConfig && (runConfig.makeindexRunConfigs.isNotEmpty() || isMakeindexNeeded)) {
//            handler.addProcessListener(RunMakeindexListener(runConfig, environment, filesToCleanUp))
//        }
//
//        return isMakeindexNeeded
//    }
}