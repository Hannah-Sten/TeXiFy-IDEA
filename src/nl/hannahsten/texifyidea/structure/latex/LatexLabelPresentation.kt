package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.requiredParameter

/**
 * @author Hannah Schellekens
 */
class LatexLabelPresentation(val labelCommand: LatexCommands) : ItemPresentation {

    private val locationString: String

    init {
        if (labelCommand.commandToken.text !in Magic.Command.labels) {
            throw IllegalArgumentException("command is no \\label-command")
        }

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(labelCommand.containingFile.virtualFile)
        val line = document!!.getLineNumber(labelCommand.textOffset) + 1
        this.locationString = labelCommand.containingFile.name + ":" + line
    }

    override fun getPresentableText() = labelCommand.requiredParameter(0) ?: ""

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LABEL!!
}
