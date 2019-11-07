package nl.hannahsten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile

/**
 * @author Hannah Schellekens
 */
object PackageUtils {

    /**
     * List containing all the packages that are available on CTAN.
     *
     * This is a static list for now, will be made dynamic (index CTAN programatically) in the future.
     */
    val CTAN_PACKAGE_NAMES: List<String> = javaClass
            .getResourceAsStream("/nl/hannahsten/texifyidea/packages/package.list")
            .bufferedReader()
            .readLine()
            .split(";")
            .toList()

    private val PACKAGE_COMMANDS = setOf("\\usepackage", "\\RequirePackage")
    private val TIKZ_IMPORT_COMMANDS = setOf("\\usetikzlibrary")
    private val PGF_IMPORT_COMMANDS = setOf("\\usepgfplotslibrary")

    /**
     * Inserts a usepackage statement for the given package in a certain file.
     * todo all callers should use other usepackage?
     *
     * @param file
     *          The file to add the usepackage statement to.
     * @param packageName
     *          The name of the package to insert.
     * @param parameters
     *          Parameters to add to the statement, `null` or empty string for no parameters.
     */
    @JvmStatic
    private fun insertUsepackage(document: Document, file: PsiFile, packageName: String, parameters: String?) {
        val commands = LatexCommandsIndex.getItems(file)

        val commandName = if (file.isStyleFile() || file.isClassFile()) "\\RequirePackage" else "\\usepackage"

        var last: LatexCommands? = null
        for (cmd in commands) {
            if (commandName == cmd.commandToken.text) {
                last = cmd
            }
        }

        val newlines: String
        val insertLocation: Int
        var postNewlines: String? = null

        // When there are no usepackage commands: insert below documentclass.
        if (last == null) {
            val classHuh = commands.asSequence()
                    .filter { cmd -> "\\documentclass" == cmd.commandToken.text || "\\LoadClass" == cmd.commandToken.text }
                    .firstOrNull()
            if (classHuh != null) {
                insertLocation = classHuh.textOffset + classHuh.textLength
                newlines = "\n"
            }
            else {
                // No other sensible location can be found
                insertLocation = 0
                newlines = ""
                postNewlines = "\n\n"
            }

        }
        // Otherwise, insert below the lowest usepackage.
        else {
            insertLocation = last.textOffset + last.textLength
            newlines = "\n"
        }

        var command = newlines + commandName
        command += if (parameters == null || "" == parameters) "" else "[$parameters]"
        command += "{$packageName}"

        if (postNewlines != null) {
            command += postNewlines
        }

        document.insertString(insertLocation, command)
    }

    /**
     * Inserts a usepackage statement for the given package in the root file of the fileset containing the given file.
     * Will not insert a new statement when the package has already been included.
     *
     * @param file
     *         The file to add the usepackage statement to.
     * @param pack
     *          The package to include.
     */
    @JvmStatic
    fun insertUsepackage(file: PsiFile, pack: Package) {
        if (pack.isDefault) {
            return
        }

        if (file.includedPackages().contains(pack.name)) {
            return
        }

        // Packages should always be included in the root file
        val rootFile = file.findRootFile()

        val document = rootFile.document() ?: return

        val params = pack.parameters
        val parameterString = StringUtil.join(params, ",")
        insertUsepackage(document, rootFile, pack.name, parameterString)
    }

    /**
     * Analyses the given file to finds all the used packages in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPackages(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedPackages(commands, HashSet())
    }

    /**
     * Analyses the given file to finds all the imported tikz libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedTikzLibraries(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedTikzLibraries(commands, HashSet())
    }

    /**
     * Analyses the given file to finds all the imported pgfplots libraries in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPgfLibraries(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedPgfLibraries(commands, HashSet())
    }

    /**
     * Analyses the given file and finds all the used packages in the included file set.
     *
     * @return A list containing all used package names (including duplicates).
     */
    @JvmStatic
    fun getIncludedPackagesList(baseFile: PsiFile): List<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedPackages(commands, ArrayList())
    }

    /**
     * Analyses the given file and finds all packages included in that file only (not the file set!)
     *
     * @return A set containing all used packages in the given file.
     */
    @JvmStatic
    fun getIncludedPackagesOfSingleFile(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItems(baseFile)
        return getIncludedPackages(commands, HashSet())
    }

    /**
     * Analyses the given file and finds all packages included in that file only (not the file set!)
     *
     * @return A list containing all used package names (including duplicates).
     */
    @JvmStatic
    fun getIncludedPackagesOfSingleFileList(baseFile: PsiFile): List<String> {
        val commands = LatexCommandsIndex.getItems(baseFile)
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

            val list = cmd.requiredParameters
            if (list.isEmpty()) {
                continue
            }

            val packageName = list[0]

            // Multiple includes.
            if (packageName.contains(",")) {
                initial.addAll(packageName.split(",").dropLastWhile(String::isNullOrEmpty))
            }
            // Single include.
            else {
                initial.add(packageName)
            }
        }

        return initial
    }
}

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(pack: Package) = PackageUtils.insertUsepackage(this, pack)

/**
 * @see PackageUtils.getIncludedPackages
 */
fun PsiFile.includedPackages(): Collection<String> = PackageUtils.getIncludedPackages(this)