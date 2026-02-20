package nl.hannahsten.texifyidea.run.ui.console

import com.intellij.icons.AllIcons
import com.intellij.ide.errorTreeView.NavigatableErrorTreeElement
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.EmptyIcon
import javax.swing.Icon

/**
 * One step in the [LatexExecutionConsole].
 *
 * @param stepId If this node is a step or has a step as parent, the step id, null otherwise.
 *
 * @author Sten Wessel
 */
class LatexExecutionNode(project: Project, val stepId: String? = null, val parent: LatexExecutionNode? = null) : PresentableNodeDescriptor<LatexExecutionNode>(project, parent), NavigatableErrorTreeElement {

    companion object {

        private val ICON_RUNNING = AnimatedIcon.Default()
        private val ICON_SUCCESS = AllIcons.RunConfigurations.TestPassed
        private val ICON_ERROR = AllIcons.RunConfigurations.TestError
        private val ICON_WARNING = AllIcons.General.Warning
        private val ICON_INFO = AllIcons.General.Information
        private val ICON_SKIPPED = AllIcons.RunConfigurations.TestIgnored
        private val ICON_NONE = EmptyIcon.ICON_16
    }

    val children = mutableListOf<LatexExecutionNode>()

    var state = State.UNKNOWN
    var title: String? = null
    var description: String? = null
    var file: VirtualFile? = null
    var line: Int? = null

    override fun getElement() = this

    override fun update(presentation: PresentationData) {
        icon = state.icon
        presentation.setIcon(icon)
        if (!title.isNullOrEmpty()) {
            presentation.addText("$title: ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }

        if (!description.isNullOrEmpty()) {
            presentation.addText(description, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
        presentation.locationString = file?.presentableName ?: ("" + if (line != null) ":$line" else "")

        myName = "$title $description"
    }

    enum class State(val icon: Icon) {

        UNKNOWN(ICON_NONE),
        RUNNING(ICON_RUNNING),
        SUCCEEDED(ICON_SUCCESS),
        SKIPPED(ICON_SKIPPED),
        WARNING(ICON_WARNING),
        FAILED(ICON_ERROR)
    }

    override fun getNavigatable(): Navigatable = if (line != null) {
        OpenFileDescriptor(project!!, file!!, line!!)
    }
    else {
        OpenFileDescriptor(project!!, file!!)
    }

    override fun toString() = (if (stepId != null) "$stepId: " else "") + description
}
