package nl.hannahsten.texifyidea.action.reformat

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.util.runCommand
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

/**
 * Run external tool 'bibtex-tidy' to reformat the file.
 * This action is placed next to the standard Reformat action in the Code menu.
 *
 * @author Thomas
 */
class ReformatWithBibtexTidy : ExternalReformatAction("Reformat File with bibtex-tidy", { it.fileType == BibtexFileType }) {

    companion object {

        val bibtexTidyVersion by lazy { DefaultArtifactVersion(runCommand("bibtex-tidy", "-v") ?: "0.0.0") }
    }

    override fun getCommand(file: PsiFile): List<String> {
        // Outputting to stdout was introduced in 1.7.2
        return if (bibtexTidyVersion < DefaultArtifactVersion("v1.7.2")) {
            listOf("bibtex-tidy", file.name, "--no-backup")
        }
        else {
            // We have to use shell in order to make the pipe work
            // We have to use a special string to escape all the single quotes
            listOf("/bin/sh", "-c", "echo '${file.text.replace("'", "'\"'\"'")}' | bibtex-tidy -")
        }
    }

    override fun processOutput(output: String, file: PsiFile, project: Project) {
        if (bibtexTidyVersion < DefaultArtifactVersion("v1.7.2")) return
        // Otherwise, we can use the stdout output
        replaceBibtexFileContent(output, file, project)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}