package nl.hannahsten.texifyidea.ui.symbols.tools

import nl.hannahsten.texifyidea.ui.symbols.SymbolCategories
import nl.hannahsten.texifyidea.ui.symbols.SymbolUiEntry
import nl.hannahsten.texifyidea.util.encloseWhen
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 220 -> 44x44 pixels.
 */
private const val DENSITY = 220
private const val QUALITY = 100

/**
 * Generates the list of all symbols for the symbol view based on the [SymbolCategories.symbolList] list.
 *
 * Requires the following programs to be installed: pdflatex, ImageMagick, and Ghostscript.
 *
 * @author Hannah Schellekens
 */
fun generateSymbolImages(symbolDirectory: String, skipExisting: Boolean = true, cleanup: Boolean = true) = auxDirectory(symbolDirectory) {
    val symbols = SymbolCategories.symbolList.distinctBy { it.imagePath }

    symbols.forEach { symbol ->
        println("Generating images for symbol " + (symbol.command?.commandWithSlash ?: symbol.fileName))

        if (skipExisting && File("$symbolDirectory/${symbol.fileName}").exists()) {
            println("> Skipping")
            return@forEach
        }

        symbol.generateImages(symbolDirectory)
    }

    if (cleanup) {
        val allPaths = symbols.flatMap { listOf("$symbolDirectory/${it.fileName}", "$symbolDirectory/${it.fileNameDark}") }
        File(symbolDirectory).listFiles().filter { file -> file.path !in allPaths }.forEach {
            println("Deleting ${it.path}")
            it.delete()
        }
    }
}

/**
 * Generates the images for the symbol.
 */
private fun SymbolUiEntry.generateImages(symbolDirectory: String) {
    createLatexFile(this, Theme.LIGHT, symbolDirectory)
    convertToImage(this, Theme.LIGHT, symbolDirectory)
    File("$symbolDirectory/texify-symbol.tex").delete()

    createLatexFile(this, Theme.DARK, symbolDirectory)
    convertToImage(this, Theme.DARK, symbolDirectory)
    File("$symbolDirectory/texify-symbol.tex").delete()
}

/**
 * Converts the tex file to a png image.
 */
private fun convertToImage(
    symbol: SymbolUiEntry,
    theme: Theme,
    symbolDirectory: String,
    latexFileName: String = "texify-symbol.tex",
    auxilDirectory: String = "auxil"
) {
    // Create pdf.
    val process = ProcessBuilder(
        "pdflatex", "-job-name=texify-symbol", "-file-line-error", "-interaction=nonstopmode", "-aux-directory=\"$auxilDirectory\"", latexFileName
    ).directory(File(symbolDirectory)).start()
    process.waitFor(6, TimeUnit.SECONDS)
    if (process.exitValue() != 0) {
        println("Failed to compile ${symbol.command?.commandWithSlash ?: symbol.fileName} to pdf. Exit code: ${process.exitValue()}")
        return
    }

    // Convert pdf to image.
    val imageName = if (theme == Theme.DARK) symbol.fileNameDark else symbol.fileName
    ProcessBuilder(
        "magick", "convert", "-density", DENSITY.toString(), "-quality", QUALITY.toString(), "texify-symbol.pdf",
        imageName
    ).directory(File(symbolDirectory)).start().waitFor(6, TimeUnit.SECONDS)

    // Delete leftover pdf.
    File("$symbolDirectory/texify-symbol.pdf").delete()
}

/**
 * Creates the tex file to compile.
 */
private fun createLatexFile(symbol: SymbolUiEntry, theme: Theme, directory: String, fileName: String = "texify-symbol.tex") {
    val result = File("$directory/$fileName")
    val latex = latex(symbol, theme)
    result.writeText(latex)
}

/**
 * Generates the latex used to generate the image.
 */
private fun latex(symbol: SymbolUiEntry, theme: Theme): String {
    val latex = symbol.imageLatex.encloseWhen(prefix = "$\\displaystyle ", suffix = "$") { symbol.isMathSymbol }
        // These don't do anything for the image, and if one is missing it is a compile error
        .replace("\\\\left([^a-zA-Z])".toRegex(), "$1").replace("\\\\right([^a-zA-Z])".toRegex(), "$1")
    val dependency = if (symbol.dependency.isDefault) "" else "\\usepackage{${symbol.dependency.name}}\n"

    return """%! author = Sten Wessel
    |\documentclass{standalone}
    |\usepackage{xcolor}
    |\usepackage{adjustbox}
    |\usepackage{amsmath}$dependency
    |\definecolor{${Theme.LIGHT.colourName}}{HTML}{000000}
    |\definecolor{${Theme.DARK.colourName}}{HTML}{BBBBBB}
    |\begin{document}
    |\color{$theme}\parbox[c][3.33ex][c]{3.33ex}{\centering\maxsizebox*{!}{2.5ex}{\maxsizebox*{2.5ex}{!}{$latex}}}
    |\end{document}
    """.trimMargin()
}

/**
 * Creates an aux directory if it does not yet exists and deletes it afterwards.
 */
private fun auxDirectory(symbolDirectory: String, auxDir: String = "auxil", block: () -> Unit) {
    val auxDirFile = File("$symbolDirectory/$auxDir")
    if (auxDirFile.exists().not()) {
        auxDirFile.mkdir()
    }
    block()
    auxDirFile.deleteRecursively()
}

/**
 * @author Hannah Schellekens
 */
private enum class Theme(val colourName: String) {

    LIGHT("lightforeground"),
    DARK("darkforeground");

    override fun toString() = colourName
}