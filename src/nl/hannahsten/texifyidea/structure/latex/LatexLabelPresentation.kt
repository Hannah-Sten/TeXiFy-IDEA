package nl.hannahsten.texifyidea.structure.latex

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
class LatexLabelPresentation(labelCommand: LatexCommands) : ItemPresentation {

    private val locationString: String
    private val presentableText: String

    init {
        // TODO: use command semantics
//        val position =
//            CommandManager.labelAliasesInfo.getOrDefault(labelCommand.commandToken.text, null)?.positions?.firstOrNull()
//                ?: 0
        val position = 0
        presentableText = labelCommand.requiredParameterText(position) ?: "no label found"

        // Location string.
        val manager = FileDocumentManager.getInstance()
        val document = manager.getDocument(labelCommand.containingFile.virtualFile)
        val line = document!!.getLineNumber(labelCommand.textOffset) + 1
        this.locationString = labelCommand.containingFile.name + ":" + line
    }

    override fun getPresentableText() = presentableText

    override fun getLocationString() = locationString

    override fun getIcon(b: Boolean) = TexifyIcons.DOT_LABEL
}
