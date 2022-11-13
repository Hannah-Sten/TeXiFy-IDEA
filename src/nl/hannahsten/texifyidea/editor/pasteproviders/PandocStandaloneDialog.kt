package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.*
import com.intellij.refactoring.util.RadioUpDownListener
import nl.hannahsten.texifyidea.util.formatAsFileName
import java.io.File
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class PandocStandaloneDialog(var isAddImports: Boolean? = null) {
    init {
        DialogBuilder().apply {
            setTitle("Pandoc Import Settings")
            val panel = JPanel()
            panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            val addImports = JRadioButton("Add required imports and paste")
            addImports.isSelected = true
            val onlyPaste = JRadioButton("Only paste")

            RadioUpDownListener(addImports, onlyPaste)

            val bg = ButtonGroup()
            bg.add(addImports)
            bg.add(onlyPaste)

            // Add the fields to the panel, with a useful label.
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
            }
        }
    }
}