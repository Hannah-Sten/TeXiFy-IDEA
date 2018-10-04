package nl.rubensten.texifyidea.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.psi.LatexCommands
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * @author Ruben Schellekens
 */
object PackageUtils {

    /**
     * List containing all the packages that are available on CTAN.
     *
     * This is a static list for now, will be made dynamic (index CTAN programatically) in the future.
     */
    val CTAN_PACKAGE_NAMES: List<String> = javaClass
            .getResourceAsStream("/nl/rubensten/texifyidea/packages/package.list")
            .bufferedReader()
            .readLine()
            .split(";")
            .toList()

    private val PACKAGE_COMMANDS = setOf("\\usepackage", "\\RequirePackage")

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
    fun insertUsepackage(document: Document, file: PsiFile, packageName: String, parameters: String?) {
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
     * Inserts a usepackage statement for the given package in a certain file.
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

        val document = file.document() ?: return

        val params = pack.parameters
        val parameterString = StringUtil.join(params, ",")
        insertUsepackage(document, file, pack.name, parameterString)
    }

    /**
     * Analyses the given file to finds all the used packages in the included file set.
     *
     * @return A set containing all used package names.
     */
    @JvmStatic
    fun getIncludedPackages(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedPackages(commands, HashSet()) as Set<String>
    }

    /**
     * Analyses the given file and finds all the used packages in the included file set.
     *
     * @return A list containing all used package names (including duplicates).
     */
    @JvmStatic
    fun getIncludedPackagesList(baseFile: PsiFile): List<String> {
        val commands = LatexCommandsIndex.getItemsInFileSet(baseFile)
        return getIncludedPackages(commands, ArrayList()) as List<String>
    }

    /**
     * Analyses the given file and finds all packages included in that file only (not the file set!)
     *
     * @return A set containing all used packages in the given file.
     */
    @JvmStatic
    fun getIncludedPackagesOfSingleFile(baseFile: PsiFile): Set<String> {
        val commands = LatexCommandsIndex.getItems(baseFile)
        return getIncludedPackages(commands, HashSet()) as Set<String>
    }

    /**
     * Analyses the given file and finds all packages included in that file only (not the file set!)
     *
     * @return A list containing all used package names (including duplicates).
     */
    @JvmStatic
    fun getIncludedPackagesOfSingleFileList(baseFile: PsiFile): List<String> {
        val commands = LatexCommandsIndex.getItems(baseFile)
        return getIncludedPackages(commands, ArrayList()) as List<String>
    }

    /**
     * Analyses all the given commands and reduces it to a set of all included packages.
     */
    @JvmStatic
    fun getIncludedPackages(commands: Collection<LatexCommands>, result: MutableCollection<String>): Collection<String> {
        for (cmd in commands) {
            if (cmd.commandToken.text !in PACKAGE_COMMANDS) {
                continue
            }

            val list = cmd.requiredParameters
            if (list.isEmpty()) {
                continue
            }

            val packageName = list[0]

            // Multiple includes.
            if (packageName.contains(",")) {
                Collections.addAll(result, *packageName.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            }
            // Single include.
            else {
                result.add(packageName)
            }
        }

        return result
    }
}

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(pack: Package) = PackageUtils.insertUsepackage(this, pack)

/**
 * @see PackageUtils.insertUsepackage
 */
fun PsiFile.insertUsepackage(packageName: String, parameters: String?) {
    val document = document() ?: return
    PackageUtils.insertUsepackage(document, this, packageName, parameters)
}

/**
 * @see PackageUtils.getIncludedPackages
 */
fun PsiFile.includedPackages(): Collection<String> = PackageUtils.getIncludedPackages(this)