package nl.rubensten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * @author Ruben Schellekens
 */
class BibitemPresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val bibitemName: String
    private val locationString: String

    init {
        if (labelCommand.commandToken.text != "\\bibitem") {
            throw IllegalArgumentException("command is no \\bibitem-command")
        }

        // Get label name.
        val required = labelCommand.requiredParameters
        if (required.isEmpty()) {
            throw IllegalArgumentException("\\bibitem has no label name")
        }
        this.bibitemName = required[0]

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(labelCommand.containingFile.virtualFile)
        val line = document!!.getLineNumber(labelCommand.textOffset) + 1
        this.locationString = labelCommand.containingFile.name + ":" + line
    }

    override fun getPresentableText() = bibitemName

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_BIB!!
}
