package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.util.io.FileUtil
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.getMakeindexOptions
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

internal class StepArtifactSync(
    private val context: LatexRunStepContext,
    private val stepsByConfigId: Map<String, LatexStepRunConfigurationOptions>,
) {

    fun beforeStep(configId: String) {
        val step = stepsByConfigId[configId] as? MakeindexStepOptions ?: return
        if (step.program != MakeindexProgram.BIB2GLS) {
            return
        }

        val sourceDir = auxOrOutputDirectory() ?: return
        val workingDirectory = makeindexWorkingDirectory(step) ?: return
        val baseFileName = makeindexBaseFileName(step)
        copyFiles(
            sourceDir = sourceDir,
            destinationDir = workingDirectory,
            baseFileName = baseFileName,
            extensions = FileMagic.bib2glsDependenciesExtensions,
            registerCleanup = false,
        )
    }

    fun afterStep(configId: String, exitCode: Int) {
        if (exitCode != 0) {
            return
        }

        val step = stepsByConfigId[configId] as? MakeindexStepOptions ?: return
        val workingDirectory = makeindexWorkingDirectory(step) ?: return
        val baseFileName = makeindexBaseFileName(step)
        copyFiles(
            sourceDir = workingDirectory,
            destinationDir = File(context.mainFile.parent.path),
            baseFileName = baseFileName,
            extensions = FileMagic.indexFileExtensions,
            registerCleanup = true,
        )

        if (step.program == MakeindexProgram.BIB2GLS) {
            moveBib2glsOutputToAuxDirectory(baseFileName, workingDirectory)
        }
    }

    private fun moveBib2glsOutputToAuxDirectory(baseFileName: String, workingDirectory: File) {
        val destinationDir = auxOrOutputDirectory() ?: return
        if (sameDirectory(destinationDir, workingDirectory)) {
            return
        }

        for (extension in setOf("glstex", "glg")) {
            val source = File(workingDirectory, baseFileName.appendExtension(extension))
            if (!source.exists()) {
                continue
            }
            val destination = File(destinationDir, source.name)
            runCatching { FileUtil.rename(source, destination) }
        }
    }

    private fun copyFiles(
        sourceDir: File,
        destinationDir: File,
        baseFileName: String,
        extensions: Set<String>,
        registerCleanup: Boolean,
    ) {
        if (sameDirectory(sourceDir, destinationDir)) {
            return
        }
        for (extension in extensions) {
            val fileName = baseFileName.appendExtension(extension)
            val source = File(sourceDir, fileName)
            if (!source.isFile) {
                continue
            }
            val destination = File(destinationDir, fileName)
            if (registerCleanup && source.exists() && !destination.exists()) {
                context.executionState.addCleanupFile(destination)
            }
            try {
                FileUtil.copy(source, destination)
            }
            catch (_: FileNotFoundException) {
            }
            catch (_: IOException) {
            }
        }
    }

    private fun makeindexBaseFileName(step: MakeindexStepOptions): String {
        val override = step.targetBaseNameOverride?.trim().takeUnless { it.isNullOrBlank() }
        if (override != null) {
            return override
        }
        return getMakeindexOptions(context.mainFile, context.runConfig.project)["name"]
            ?: context.mainFile.nameWithoutExtension
    }

    private fun makeindexWorkingDirectory(step: MakeindexStepOptions): File? = CommandLineRunStep.resolveWorkingDirectory(
        context,
        step.workingDirectoryPath,
    )?.toFile()

    private fun auxOrOutputDirectory(): File? = context.executionState.resolvedAuxDir?.path?.let(::File)
        ?: context.executionState.resolvedOutputDir?.path?.let(::File)

    private fun sameDirectory(left: File, right: File): Boolean = runCatching {
        left.canonicalFile == right.canonicalFile
    }.getOrElse { left.absolutePath == right.absolutePath }
}
