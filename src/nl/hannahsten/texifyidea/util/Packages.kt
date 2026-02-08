package nl.hannahsten.texifyidea.util

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.TreeUtil
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.LatexProjectStructure.getFilesetScopeFor
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.PackageUtils.insertUsepackage
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic

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
    fun getDefaultInsertAnchor(commands: Sequence<LatexCommands>, preferredAnchor: LatexCommands?): Pair<PsiElement?, Boolean> {
        val classHuh = commands.firstOrNull { cmd ->
            val name = cmd.nameWithSlash
            name == CommandNames.DOCUMENT_CLASS || name == CommandNames.LOAD_CLASS
        }
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

        insertPreambleText(file, command)
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
    private fun insertPreambleText(file: PsiFile, resolvedInsertText: String) {
        val commands = file.traverseCommands()

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
        return insertUsepackage(file, LatexLib.Package(pack.name), pack.parameters.asList())
    }

    private val conflictingPackagesList = listOf(
        setOf("biblatex.sty", "natbib.sty"),
    )
    private val conflictingPackageMap = buildMap {
        conflictingPackagesList.forEach { names ->
            names.forEach { name ->
                merge(name, names) { old, new -> old + new }
            }
        }
    }

    fun insertUsepackage(file: PsiFile, lib: LatexLib, options: List<String> = emptyList()): Boolean {
        if (lib.isDefault || lib.isCustom) return true
        val packName = lib.asPackageName() ?: return false
        val filesetData = LatexProjectStructure.getFilesetDataFor(file)
        if (filesetData != null) {
            val fileName = lib.toFileName() ?: return true
            if (fileName in filesetData.libraries) return true
            conflictingPackageMap[fileName]?.let { conflicts ->
                // Don't insert when a conflicting package is already present
                if (conflicts.any {
                        fileName != it && filesetData.libraries.contains(it)
                    }
                ) {
                    return false
                }
            }
        }

        // Packages should always be included in the root file
        val rootFile = file.findRootFile()

        insertUsepackage(rootFile, packName, options.joinToString(","))
        LatexDefinitionService.getInstance(file.project).requestRefresh()

        return true
    }

    private fun extractPackageNames(text: String, result: MutableCollection<String>) {
        text.split(PatternMagic.parameterSplit).forEach { param ->
            val packageName = param.trim()
            if (packageName.isNotEmpty()) {
                result.add(packageName)
            }
        }
    }

    private fun <T : MutableCollection<String>> getPackagesFromCommands(
        commands: Iterable<LatexCommands>,
        results: T,
    ): T {
        commands.forEach { cmd ->
            // since we must use stub-based resolution, we can not skip for something like ONLYIFSTANDALONE
            cmd.requiredParametersText().forEach { extractPackageNames(it, results) }
            cmd.optionalParameterTextMap().keys.forEach { extractPackageNames(it, results) }
        }
        return results
    }

    /**
     * Returns a set of all packages that are included in the filesets of the given file.
     *
     * The returned set contains the extensions to distinguish between packages and classes,
     * for example `{article.cls, amsmath.sty}`.
     */
    fun getIncludedLibrariesInFileset(file: PsiFile): Set<String> {
        val data = LatexProjectStructure.getFilesetDataFor(file) ?: return emptySet()
        val filesets = data.filesets
        if (filesets.isEmpty()) return emptySet()
        if (filesets.size == 1) return filesets.first().libraries
        val result = mutableSetOf<String>()
        data.filesets.forEach {
            result.addAll(it.libraries)
        }
        return result
    }

    /**
     * Gets a list of all packages that are explicitly included via `\usepackage`.
     *
     * This does not include packages that are included in packages.
     */
    fun getExplicitUsedPackagesInFileset(file: PsiFile): List<String> {
        val scope = getFilesetScopeFor(file, onlyTexFiles = true)
        val commands = NewCommandsIndex.getByName("\\usepackage", scope)
        return getPackagesFromCommands(commands, mutableListOf())
    }

    /**
     * Analyses the given file to finds all the imported tikz libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedTikzLibraries(baseFile: PsiFile): Set<String> {
        val commands = NewCommandsIndex.getByNamesInFileSet(CommandMagic.tikzLibraryInclusionCommands, baseFile)
        return getPackagesFromCommands(commands, mutableSetOf())
    }

    /**
     * Analyses the given file to finds all the imported pgfplots libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPgfLibraries(baseFile: PsiFile): Set<String> {
        val commands = NewCommandsIndex.getByNamesInFileSet(CommandMagic.pgfplotsLibraryInclusionCommands, baseFile)
        return getPackagesFromCommands(commands, mutableSetOf())
    }
}

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(pack: LatexPackage) = PackageUtils.insertUsepackage(this, pack)

fun PsiFile.insertUsepackage(latexLib: LatexLib) = PackageUtils.insertUsepackage(this, latexLib)

/**
 * Find all included LaTeX packages in the file set of this file.
 *
 * These may be packages that are in the project, installed in the LateX distribution or somewhere else.
 * This includes packages that are included indirectly (via other packages).
 *
 * @return List of all included packages, including those that are included indirectly.
 */
fun PsiFile.includedPackagesInFileset(): Set<LatexPackage> = PackageUtils.getIncludedLibrariesInFileset(this).map { LatexPackage(it.substringBefore('.')) }.toSet()