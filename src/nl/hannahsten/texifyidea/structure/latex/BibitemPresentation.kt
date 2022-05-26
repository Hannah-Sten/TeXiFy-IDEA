package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
class BibitemPresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val bibitemName: String
    private val locationString: String

    init {
        if (labelCommand.commandToken.text != "\\bibitem") {
            throw IllegalArgumentException("command is no \\bibitem-command")
        }

        // Get label name.
        this.bibitemName = labelCommand.requiredParameters.firstOrNull() ?: ""

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(labelCommand.containingFile.virtualFile)
        val line = document!!.getLineNumber(labelCommand.textOffset) + 1
        this.locationString = labelCommand.containingFile.name + ":" + line
    }

    override fun getPresentableText() = bibitemName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_BIB
}
