package nl.rubensten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.settings.TexifySettings
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
class LatexLabelPresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val locationString: String
    private val presentableText: String

    init {
        val labelingCommands = TexifySettings.getInstance().labelCommands
        if (!labelingCommands.containsKey(labelCommand.commandToken.text)) {
            val token = labelCommand.commandToken.text
            throw IllegalArgumentException("command '$token' is no \\label-command")
        }

        val position = labelingCommands[labelCommand.commandToken.text ?: ""]?.position ?: 1
        presentableText = labelCommand.requiredParameter(position - 1) ?: "no label found"

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(labelCommand.containingFile.virtualFile)
        val line = document!!.getLineNumber(labelCommand.textOffset) + 1
        this.locationString = labelCommand.containingFile.name + ":" + line
    }

    override fun getPresentableText() = presentableText

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LABEL!!
}
