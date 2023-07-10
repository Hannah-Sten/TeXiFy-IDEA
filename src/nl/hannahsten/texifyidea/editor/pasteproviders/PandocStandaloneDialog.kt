package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.refactoring.util.RadioUpDownListener
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

/**
 * todo
 */
class PandocStandaloneDialog(var isAddImports: Boolean? = null) {

    var abort = false

    init {
        DialogBuilder().apply {
            setTitle("Pandoc Import Settings")
            val panel = JPanel()
            panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            val useInternalPaste = JRadioButton("Use internal paste translator")
            val addImports = JRadioButton("Add required pandoc imports and paste")
            addImports.isSelected = true
            val onlyPaste = JRadioButton("Only paste pandoc translation")
            // todo check performance of pandoc. If bad, show this dialog once and save preference in settings

            RadioUpDownListener(useInternalPaste, addImports, onlyPaste)

            val bg = ButtonGroup()
            bg.add(useInternalPaste)
            bg.add(addImports)
            bg.add(onlyPaste)

            // Add the fields to the panel, with a useful label.
            panel.add(useInternalPaste)
            panel.add(addImports)
            panel.add(onlyPaste)

            setCenterPanel(panel)
            addCancelAction()
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                if (addImports.isSelected)
                    isAddImports = true
                else if (onlyPaste.isSelected)
                    isAddImports = false
                else if (useInternalPaste.isSelected)
                    abort = true
            }
        }
    }
}