package nl.hannahsten.texifyidea.util

import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.toStringMap
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.PackageMagic

/**
 * @author Hannah Schellekens
 */
object PackageUtils {

    /**
     * List containing all the packages that are available on CTAN.
     *
     * This is a static list for now, will be made dynamic (index CTAN programmatically) in the future.
     */
    val CTAN_PACKAGE_NAMES: List<String> = javaClass
        .getResourceAsStream("/nl/hannahsten/texifyidea/packages/package.list")
        .bufferedReader()
        .readLine()
        .split(";")
        .toList()

    /** Commands which can include packages in optional or required arguments. **/
    val PACKAGE_COMMANDS = setOf("\\usepackage", "\\RequirePackage", "\\documentclass")
    private val TIKZ_IMPORT_COMMANDS = setOf("\\usetikzlibrary")
    private val PGF_IMPORT_COMMANDS = setOf("\\usepgfplotslibrary")

    /**
     * Inserts a usepackage statement for the given package in a certain file.
     *
     * @param file
     *          The file to add the usepackage statement to.
     * @param packageName
     *          The name of the package to insert.
     * @param parameters
     *          Parameters to add to the statement, `null` or empty string for no parameters.
     */
    @JvmStatic
    fun insertUsepackage(file: PsiFile, packageName: String, parameters: String?) {

        if (!TexifySettings.getInstance().automaticDependencyCheck) {
            return
        }

        val commands = file.commandsInFile()

        val commandName = if (file.isStyleFile() || file.isClassFile()) "\\RequirePackage" else "\\usepackage"

        var last: LatexCommands? = null
        for (cmd in commands) {
            if (commandName == cmd.commandToken.text) {
                // Do not insert below the subfiles package, it should stay last
                if (cmd.requiredParameters.contains("subfiles")) {
                    break
                }
                else {
                    last = cmd
                }
            }
        }

        val prependNewLine: Boolean
        // The anchor after which the new element will be inserted
        val anchorAfter: PsiElement?

        // When there are no usepackage commands: insert below documentclass.
        if (last == null) {
            val classHuh = commands.asSequence()
                .filter { cmd ->
                    "\\documentclass" == cmd.commandToken
                        .text || "\\LoadClass" == cmd.commandToken.text
                }
                .firstOrNull()
            if (classHuh != null) {
                anchorAfter = classHuh
                prependNewLine = true
            }
            else {
                // No other sensible location can be found
                anchorAfter = null
                prependNewLine = false
            }
        }
        // Otherwise, insert below the lowest usepackage.
        else {
            anchorAfter = last
            prependNewLine = true
        }

        var command = commandName
        command += if (parameters == null || "" == parameters) "" else "[$parameters]"
        command += "{$packageName}"

        val newNode = LatexPsiHelper(file.project).createFromText(command).firstChild.node

        // https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/modifying_psi.html?search=refac#combining-psi-and-document-modifications
        // Avoid 'Write access is allowed inside write-action only" exception
        runWriteAction {
            // Avoid "Attempt to modify PSI for non-committed Document"
            PsiDocumentManager.getInstance(file.project).doPostponedOperationsAndUnblockDocument(file.document() ?: return@runWriteAction)
            PsiDocumentManager.getInstance(file.project).commitDocument(file.document() ?: return@runWriteAction)
            if (anchorAfter != null) {
                val anchorBefore = anchorAfter.node.treeNext
                if (prependNewLine) {
                    val newLine = LatexPsiHelper(file.project).createFromText("\n").firstChild.node
                    anchorAfter.parent.node.addChild(newLine, anchorBefore)
                }
                anchorAfter.parent.node.addChild(newNode, anchorBefore)
            }
            else {
                // Insert at beginning
                file.node.addChild(newNode, file.firstChild.node)
            }
        }
    }

    /**
     * Inserts a usepackage statement for the given package in the root file of the fileset containing the given file.
     * Will not insert a new statement when the package has already been included, or when a conflicting package is already included.
     *
     * @param file The file to add the usepackage statement to.
     * @param pack The package to include.
     *
     * @return false if the package was not inserted, because a conflicting package is already present.
     */
    @JvmStatic
    fun insertUsepackage(file: PsiFile, pack: LatexPackage): Boolean {
        if (pack.isDefault) {
            return true
        }

        if (file.includedPackages().contains(pack.name)) {
            return true
        }

        // Don't insert when a conflicting package is already present
        if (PackageMagic.conflictingPackages.any { it.contains(pack) }) {
            for (conflicts in PackageMagic.conflictingPackages) {
                // Assuming the package is not already included
                if (conflicts.contains(pack) && file.includedPackages().toSet()
                        .intersect(conflicts.map { it.name })
                        .isNotEmpty()
                ) {
                    return false
                }
            }
        }

        // Packages should always be included in the root file
        val rootFile = file.findRootFile()

        val params = pack.parameters
        val parameterString = StringUtil.join(params, ",")
        insertUsepackage(rootFile, pack.name, parameterString)

        return true
    }

    /**
     * Analyses the given file to finds all the used packages in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPackages(baseFile: PsiFile): Set<String> {
        val commands = baseFile.commandsInFileSet()
        return getIncludedPackages(commands, HashSet())
    }

    /**
     * Analyses the given file to finds all the imported tikz libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedTikzLibraries(baseFile: PsiFile): Set<String> {
        val commands = baseFile.commandsInFileSet()
        return getIncludedTikzLibraries(commands, HashSet())
    }

    /**
     * Analyses the given file to finds all the imported pgfplots libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPgfLibraries(baseFile: PsiFile): Set<String> {
        val commands = baseFile.commandsInFileSet()
        return getIncludedPgfLibraries(commands, HashSet())
    }

    /**
     * Analyses the given file and finds all the used packages in the included file set.
     *
     * @return A list containing all used package names (including duplicates).
     */
    @JvmStatic
    fun getIncludedPackagesList(baseFile: PsiFile): List<String> {
        val commands = baseFile.commandsInFileSet()
        return getIncludedPackages(commands, ArrayList())
    }

    /**
     * Gets all packages imported with [PACKAGE_COMMANDS].
     */
    @JvmStatic
    fun <T : MutableCollection<String>> getIncludedPackages(
        commands: Collection<LatexCommands>,
        result: T
    ) = getPackagesFromCommands(commands, PACKAGE_COMMANDS, result)

    /**
     * Gets all packages imported with [TIKZ_IMPORT_COMMANDS].
     */
    @JvmStatic
    fun <T : MutableCollection<String>> getIncludedTikzLibraries(
        commands: Collection<LatexCommands>,
        result: T
    ) = getPackagesFromCommands(commands, TIKZ_IMPORT_COMMANDS, result)

    /**
     * Gets all packages imported with [PGF_IMPORT_COMMANDS].
     */
    @JvmStatic
    fun <T : MutableCollection<String>> getIncludedPgfLibraries(
        commands: Collection<LatexCommands>,
        result: T
    ) = getPackagesFromCommands(commands, PGF_IMPORT_COMMANDS, result)

    /**
     * Analyses all the given commands and reduces it to a set of all included packages.
     * Classes will not be included. todo add packages from index?
     *
     * Note that not all elements returned may be valid package names.
     */
    private fun <T : MutableCollection<String>> getPackagesFromCommands(
        commands: Collection<LatexCommands>,
        packageCommands: Set<String>,
        initial: T
    ): T {
        for (cmd in commands) {
            if (cmd.commandToken.text !in packageCommands) {
                continue
            }

            // Just skip conditionally included packages, because it is too expensive to determine whether
            // they are really included or not
            if (cmd.parent.firstParentOfType(LatexCommands::class)?.name == "\\" + LatexGenericRegularCommand.ONLYIFSTANDALONE.command) {
                continue
            }

            // Assume packages can be included in both optional and required parameters
            // Except a class, because a class is not a package
            val packages = if (cmd.commandToken
                .text == "\\documentclass" || cmd.commandToken
                    .text == "\\LoadClass"
            ) {
                setOf(cmd.optionalParameterMap.toStringMap().keys.toList())
            }
            else {
                setOf(
                    cmd.requiredParameters,
                    cmd.optionalParameterMap.toStringMap().keys
                        .toList()
                )
            }

            for (list in packages) {

                if (list.isEmpty()) {
                    continue
                }

                val packageName = list[0]

                // Multiple includes.
                if (packageName.contains(",")) {
                    initial.addAll(
                        packageName.split(",")
                            .dropLastWhile(String::isNullOrEmpty)
                    )
                }
                // Single include.
                else {
                    initial.add(packageName)
                }
            }
        }

        return initial
    }
}

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
        // Assume that you can not use the bundle name in a \usepackage if it is different from the package name (otherwise this search won't work and we would need to use tlmgr search --global $packageName
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
            longer be updated\.  This happens in preparation for a new release\.

            If you're interested in helping to pretest the new release \(when
            pretests are available\), please read https:\/\/tug\.org\/texlive\/pretest\.html\.
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

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(pack: LatexPackage) = PackageUtils.insertUsepackage(this, pack)

/**
 * @see PackageUtils.getIncludedPackages
 */
fun PsiFile.includedPackages(): Collection<String> = PackageUtils.getIncludedPackages(this)
