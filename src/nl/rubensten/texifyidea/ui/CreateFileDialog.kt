package nl.rubensten.texifyidea.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.JPanel

class CreateFileDialog(private val currentFilePath: String, private val newFileName: String, var newFileFullPath: String? = null) {
    init {
        DialogBuilder().apply {
            setTitle("Create new tex file")
            val panel = JPanel()

            val pathField = TextFieldWithBrowseButton()
            pathField.text = currentFilePath
            pathField.addBrowseFolderListener(
                    TextBrowseFolderListener(
                            FileChooserDescriptor(false, true, false, false, false, false)
                                    .withTitle("Select path of new file")))
            panel.add(pathField)


            setCenterPanel(panel)
            addCancelAction()
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                newFileFullPath = pathField.text
            }
        }
    }
}