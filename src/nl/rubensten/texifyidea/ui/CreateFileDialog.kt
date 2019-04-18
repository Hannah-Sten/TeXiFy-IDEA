package nl.rubensten.texifyidea.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.*
import javax.swing.JPanel
import javax.swing.JTextField

class CreateFileDialog(private val currentFilePath: String, private val newFileName: String, var newFileFullPath: String? = null) {
    init {
        DialogBuilder().apply {
            setTitle("Create new tex file")
            val panel = JPanel()
            panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Field to enter the name of the new file.
            val nameField = JTextField("$newFileName.tex")
            // Field to select the folder/location of the new file.
            val pathField = TextFieldWithBrowseButton()
            pathField.text = currentFilePath
            pathField.addBrowseFolderListener(
                    TextBrowseFolderListener(
                            FileChooserDescriptor(false, true, false, false, false, false)
                                    .withTitle("Select path of new file")))

            panel.add(LabeledComponent.create(nameField, "File name"))
            panel.add(LabeledComponent.create(pathField, "Location"))


            setCenterPanel(panel)
            addCancelAction()
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                newFileFullPath = "${pathField.text}/${nameField.text}"
            }
        }
    }
}