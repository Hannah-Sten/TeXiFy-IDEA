package nl.hannahsten.texifyidea.util

import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

object TexLivePackages {

    /**
     * List of installed packages.
     */
    var packageList: MutableList<String> = mutableListOf()

    /**
     * Given a package name used in \usepackage or \RequirePackage, find the
     * name needed to install from TeX Live. E.g. to be able to use \usepackage{rubikrotation}
     * we need to install the rubik package.
     *
     * In the output
     *
     *    tlmgr: package repository http://ctan.math.utah.edu/ctan/tex-archive/systems/texlive/tlnet (verified)
     *    rubik:
     *            texmf-dist/tex/latex/rubik/rubikrotation.sty
     *
     * we are looking for "rubik". Possibly tex live outputs a "TeX Live 2019 is frozen" message before, so
     * we search for the line that starts with tlmgr. Then the name of the package we are
     * looking for will be on the next line, if it exists.
     */
    fun findTexLiveName(task: Task.Backgroundable, packageName: String, project: Project): String? {
        // Find the package name for tlmgr.
        task.title = "Searching for $packageName..."
        val tlmgrExecutable = LatexSdkUtil.getExecutableName("tlmgr", project)
        // Assume that you can not use the bundle name in a \\usepackage if it is different from the package name (otherwise this search won't work and we would need to use tlmgr search --global $packageName
        val searchResult = "$tlmgrExecutable search --file --global /$packageName.sty".runCommand()
            ?: return null

        // Check if tlmgr needs to be updated first, and do so if needed.
        val tlmgrUpdateCommand = "$tlmgrExecutable update --self"
        if (searchResult.contains(tlmgrUpdateCommand)) {
            task.title = "Updating tlmgr..."
            tlmgrUpdateCommand.runCommand()
        }

        return extractRealPackageNameFromOutput(searchResult)
    }

    fun extractRealPackageNameFromOutput(output: String): String? {
        val tlFrozen = Regex(
            """
            TeX Live \d{4} is frozen forever and will no
            longer be updated\.\s+This happens in preparation for a new release\.

            If you're interested in helping to pretest the new release \(when
            pretests are available\), please read https://tug\.org/texlive/pretest\.html\.
            Otherwise, just wait, and the new release will be ready in due time\.
            """.trimIndent()
        )
        val lines = tlFrozen.replace(output, "").trim().split('\n')
        val tlmgrIndex = lines.indexOfFirst { it.startsWith("tlmgr:") }
        return try {
            lines[tlmgrIndex + 1].trim().dropLast(1) // Drop the : behind the package name.
        }
        catch (e: IndexOutOfBoundsException) {
            null
        }
    }
}