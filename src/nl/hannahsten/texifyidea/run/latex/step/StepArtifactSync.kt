package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
import nl.hannahsten.texifyidea.run.latex.MakeindexStepOptions
import nl.hannahsten.texifyidea.run.latex.getMakeindexOptions
import nl.hannahsten.texifyidea.util.appendExtension
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

internal class StepArtifactSync(
    private val context: LatexRunStepContext,
    private val step: MakeindexStepOptions,
) {

    fun beforeStep() {
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

    fun afterStep(exitCode: Int) {
        if (exitCode != 0) {
            return
        }

        val workingDirectory = makeindexWorkingDirectory(step) ?: return
        val baseFileName = makeindexBaseFileName(step)
        copyFiles(
            sourceDir = workingDirectory,
            destinationDir = Path(context.session.mainFile.parent.path),
            baseFileName = baseFileName,
            extensions = FileMagic.indexFileExtensions,
            registerCleanup = true,
        )

        if (step.program == MakeindexProgram.BIB2GLS) {
            moveBib2glsOutputToAuxDirectory(baseFileName, workingDirectory)
        }
    }

    private fun moveBib2glsOutputToAuxDirectory(baseFileName: String, workingDirectory: Path) {
        val destinationDir = auxOrOutputDirectory() ?: return
        if (sameDirectory(destinationDir, workingDirectory)) {
            return
        }

        for (extension in setOf("glstex", "glg")) {
            val source = workingDirectory.resolve(baseFileName.appendExtension(extension))
            if (!source.exists()) {
                continue
            }
            val destination = destinationDir.resolve(source.name)
            runCatching {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun copyFiles(
        sourceDir: Path,
        destinationDir: Path,
        baseFileName: String,
        extensions: Set<String>,
        registerCleanup: Boolean,
    ) {
        if (sameDirectory(sourceDir, destinationDir)) {
            return
        }
        for (extension in extensions) {
            val fileName = baseFileName.appendExtension(extension)
            val source = sourceDir.resolve(fileName)
            if (!source.isRegularFile()) {
                continue
            }
            val destination = destinationDir.resolve(fileName)
            if (registerCleanup && source.exists() && !destination.exists()) {
                context.session.addCleanupFile(destination)
            }
            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
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
        return getMakeindexOptions(context.session.mainFile, context.runConfig.project)["name"]
            ?: context.session.mainFile.nameWithoutExtension
    }

    private fun makeindexWorkingDirectory(step: MakeindexStepOptions): Path =
        CommandLineRunStep.resolveWorkingDirectory(context, step.workingDirectoryPath)

    private fun auxOrOutputDirectory(): Path? =
        context.session.auxDir?.path?.let(Path::of)
            ?: context.session.outputDir?.path?.let(Path::of)

    private fun sameDirectory(left: Path, right: Path): Boolean = runCatching {
        left.toRealPath() == right.toRealPath()
    }.getOrElse { left.absolute() == right.absolute() }
}
