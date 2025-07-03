package nl.hannahsten.texifyidea.util

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.index.file.LatexExternalPackageInclusionCache
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PackageMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.toStringMap

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
        ?.bufferedReader()
        ?.readLine()
        ?.split(";")
        ?.toList() ?: emptyList()

    /**
     * Get the default psi element to insert new packages/definitions after.
     * The anchor will be the given preferred anchor if not null.
     */
    fun getDefaultInsertAnchor(commands: Collection<LatexCommands>, preferredAnchor: LatexCommands?): Pair<PsiElement?, Boolean> {
        val classHuh = commands.asSequence()
            .filter { cmd ->
                cmd.name == LatexGenericRegularCommand.DOCUMENTCLASS.cmd || cmd.name == LatexGenericRegularCommand.LOADCLASS.cmd
            }
            .firstOrNull()
        val anchorAfter: PsiElement?
        val prependNewLine: Boolean
        if (classHuh != null) {
            anchorAfter = classHuh
            prependNewLine = true
        }
        else {
            // No other sensible location can be found
            anchorAfter = null
            prependNewLine = false
        }

        return if (preferredAnchor == null) {
            Pair(anchorAfter, prependNewLine)
        }
        else {
            Pair(preferredAnchor, true)
        }
    }

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
    private fun insertUsepackage(file: PsiFile, packageName: String, parameters: String?) {
        val commandName = if (file.isStyleFile() || file.isClassFile()) "\\RequirePackage" else "\\usepackage"

        var command = commandName
        command += if (parameters == null || "" == parameters) "" else "[$parameters]"
        command += "{$packageName}"

        return insertPreambleText(file, command)
    }

    /**
     * Inserts text into the preamble. See [insertUsepackage] for more user-friendly versions.
     *
     * This exists strictly for pandoc
     *
     * @param file
     *          The file to add the string to.
     * @param resolvedInsertText
     *          The string to insert to the end of the preamble.
     */
    @JvmStatic
    fun insertPreambleText(file: PsiFile, resolvedInsertText: String) {
        if (!TexifySettings.getInstance().automaticDependencyCheck) {
            return
        }

        val commands = file.commandsInFile()

        val commandName = if (file.isStyleFile() || file.isClassFile()) "\\RequirePackage" else "\\usepackage"

        var last: LatexCommands? = null
        for (cmd in commands) {
            if (commandName == cmd.commandToken.text) {
                // Do not insert below the subfiles package, it should stay last
                if (cmd.requiredParametersText().contains("subfiles")) {
                    break
                }
                else {
                    last = cmd
                }
            }
        }

        val (anchorAfter, prependNewLine) = getDefaultInsertAnchor(commands, last)

        val newNode = LatexPsiHelper(file.project).createFromText(resolvedInsertText).firstChild.node

        insertNodeAfterAnchor(file, anchorAfter, prependNewLine, newNode)
    }

    /**
     * Insert an AST node after a certain anchor, possibly with a newline.
     *
     * @param prependBlankLine If prependNewLine is true, you can set this to true to insert an additional blank line.
     */
    fun insertNodeAfterAnchor(
        file: PsiFile,
        anchorAfter: PsiElement?,
        prependNewLine: Boolean,
        newNode: ASTNode,
        prependBlankLine: Boolean = false
    ) {
        // Don't run in a write action, as that will produce a SideEffectsNotAllowedException for INVOKE_LATER

        // Avoid "Attempt to modify PSI for non-committed Document"
        // https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/modifying_psi.html?search=refac#combining-psi-and-document-modifications
        PsiDocumentManager.getInstance(file.project)
            .doPostponedOperationsAndUnblockDocument(file.document() ?: return)
        PsiDocumentManager.getInstance(file.project).commitDocument(file.document() ?: return)
        runWriteAction {
            val newlineText = if (prependBlankLine) "\n\n" else "\n"
            val newLine = LatexPsiHelper(file.project).createFromText(newlineText).firstChild.node
            // Avoid NPE, see #3083 (cause unknown)
            if (anchorAfter != null && TreeUtil.getFileElement(anchorAfter.parent.node) != null) {
                val anchorBefore = anchorAfter.node.treeNext
                if (prependNewLine || prependBlankLine) {
                    anchorAfter.parent.node.addChild(newLine, anchorBefore)
                }

                anchorAfter.parent.node.addChild(newNode, anchorBefore)
            }
            else {
                // Insert at beginning
                if (prependNewLine || prependBlankLine) {
                    file.node.addChild(newLine, file.firstChild.node)
                }
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
    fun insertUsepackage(file: PsiFile, pack: LatexPackage): Boolean {
        if (pack.isDefault) {
            return true
        }

        if (file.includedPackages().contains(pack)) {
            return true
        }

        // Don't insert when a conflicting package is already present
        if (PackageMagic.conflictingPackages.any { it.contains(pack) }) {
            for (conflicts in PackageMagic.conflictingPackages) {
                // Assuming the package is not already included
                if (conflicts.contains(pack) && file.includedPackages().toSet().intersect(conflicts).isNotEmpty()) {
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
     * Gets all packages imported with tikz library import commands.
     */
    @JvmStatic
    fun <T : MutableCollection<String>> getIncludedTikzLibraries(
        commands: Collection<LatexCommands>,
        result: T
    ) = getPackagesFromCommands(commands, CommandMagic.tikzLibraryInclusionCommands, result)

    /**
     * Gets all packages imported with pgf library import commands.
     */
    @JvmStatic
    fun <T : MutableCollection<String>> getIncludedPgfLibraries(
        commands: Collection<LatexCommands>,
        result: T
    ) = getPackagesFromCommands(commands, CommandMagic.pgfplotsLibraryInclusionCommands, result)

    /**
     * Analyses all the given commands and reduces it to a set of all included packages, libraries or whatever is imported
     * with the given [packageCommands].
     * Classes will be included.
     *
     * Note that not all elements returned may be valid package names.
     */
    fun <T : MutableCollection<String>> getPackagesFromCommands(
        commands: Collection<LatexCommands>,
        packageCommands: Set<String>,
        initial: T
    ): T {
        for (cmd in commands) {
            if (cmd.name !in packageCommands) {
                continue
            }

            // Just skip conditionally included packages, because it is too expensive to determine whether
            // they are really included or not
            if (cmd.parent?.firstParentOfType(LatexCommands::class)?.name == "\\" + LatexGenericRegularCommand.ONLYIFSTANDALONE.command) {
                continue
            }

            // Assume packages can be included in both optional and required parameters
            // Technically a class is not a package, but LatexCommand doesn't separate those things yet so we ignore that here as well
            val packages = setOf(
                cmd.requiredParametersText(),
                cmd.getOptionalParameterMap().toStringMap().keys.toList()
            )

            for (list in packages) {
                if (list.isEmpty()) {
                    continue
                }

                val packageName = list[0]

                // Multiple includes.
                if (packageName.contains(",")) {
                    initial.addAll(
                        packageName.split(",").dropLastWhile(String::isNullOrEmpty)
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

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(pack: LatexPackage) = PackageUtils.insertUsepackage(this, pack)

/**
 * Find all included LaTeX packages in the file set of this file.
 *
 * These may be packages that are in the project, installed in the LateX distribution or somewhere else.
 * This includes packages that are included indirectly (via other packages).
 *
 * @param onlyDirectInclusions If true, only packages included directly are returned.
 * @return List of all included packages. Those who are directly included, may contain duplicates.
 */
fun PsiFile.includedPackages(onlyDirectInclusions: Boolean = false): Set<LatexPackage> {
    val scope = LatexProjectStructure.buildFilesetScopeFor(this)
    val includeCommands = NewSpecialCommandsIndex.getAllPackageIncludes(project, scope)
    return includedPackages(includeCommands, project, onlyDirectInclusions)
}

/**
 * See [includedPackages].
 */
fun includedPackages(commands: Collection<LatexCommands>, project: Project, onlyDirectInclusions: Boolean = false): Set<LatexPackage> {
    val directIncludes = PackageUtils.getPackagesFromCommands(commands, CommandMagic.packageInclusionCommands, mutableListOf())
        .map { LatexPackage(it) }.toSet()
    return if (onlyDirectInclusions) directIncludes else LatexExternalPackageInclusionCache.getAllIndirectlyIncludedPackages(directIncludes, project).toSet()
}