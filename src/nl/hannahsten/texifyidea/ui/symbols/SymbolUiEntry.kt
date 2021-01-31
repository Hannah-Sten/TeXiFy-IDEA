package nl.hannahsten.texifyidea.ui.symbols

import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand

/**
 * Represents an entry in the Symbol overview tool window.
 *
 * Images representing the symbols must be saved in a `.png` format. Variants for dark themes must have a `_dark.png`
 * extension.
 *
 * @author Hannah Schellekens
 */
interface SymbolUiEntry : Described {

    /**
     * The underlying command of the symbol, if applicable.
     */
    val command: LatexCommand?

    /**
     * The latex that must be generated when the symbol gets inserted.
     * `<caret>` mars where the caret will end up after insertion.
     */
    val generatedLatex: String

    /**
     * The file name of the light theme image, ends with .png.
     */
    val fileName: String

    /**
     * The file name of the dark theme image, ends with .png.
     */
    val fileNameDark: String
        get() = fileName.replace(".png", "_dark.png")

    /**
     * The resource location of the symbol `png` image for light UIs. Image must be at most 44x44.
     */
    val imagePath: String

    /**
     * The resource location of the symbol `png` image for dark UIs.
     */
    val imagePathDark: String
        get() = imagePath.replace(".png", "_dark.png")

    /**
     * The latex used to generate the image.
      */
    val imageLatex: String

    /**
     * The package on which inserting the symbol is dependent.
     */
    val dependency: LatexPackage
        get() = command?.dependency ?: LatexPackage.DEFAULT

    /**
     * Whether this is a math symbol.
     * `false` when no command is linked to the ui entry.
     */
    val isMathSymbol: Boolean
        get() = command?.isMathMode ?: false
}