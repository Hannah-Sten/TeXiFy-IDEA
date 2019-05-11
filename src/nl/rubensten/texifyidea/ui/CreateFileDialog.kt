package nl.rubensten.texifyidea.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.*
import nl.rubensten.texifyidea.util.formatAsFileName
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author Abby Berkers
 *
 * Dialog to ask the user for a file name and location when creating a new file.
 *
 * @param currentFilePath The path of the file we are currently in, e.g., the file from which an intention was triggered
 *      that creates a new file.
 * @param newFileName The name of the new file, with or without the '.tex' extension.
 * @param newFileFullPath The full path of the new file, without tex extension.
 */
class CreateFileDialog(private val currentFilePath: String?, private val newFileName: String, var newFileFullPath: String? = null) {
    init {
        DialogBuilder().apply {
            setTitle("Create new tex file")
            val panel = JPanel()
            panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Field to enter the name of the new file.
            val nameField = JTextField(newFileName)
            // Field to select the folder/location of the new file.
            val pathField = TextFieldWithBrowseButton()
            pathField.text = currentFilePath ?: return@apply
            // Make sure the dialog is wide enough to fit the whole path in the text field.
            pathField.textField.columns = pathField.text.length

                    // Add a listener to the browse button to browse a folder
            pathField.addBrowseFolderListener(
                    TextBrowseFolderListener(
                            FileChooserDescriptor(false, true, false, false, false, false)
                                    .withTitle("Select folder of new file")))

            // Add the fields to the panel, with a useful label.
            panel.add(LabeledComponent.create(nameField, "File name (.tex optional)"))
            panel.add(LabeledComponent.create(pathField, "Location"))


            setCenterPanel(panel)
            addCancelAction()
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            // Focus the filename field.
            setPreferredFocusComponent(nameField)

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                // Format the text from the name field as a file name (e.g. " " -> "-") and remove the (double) tex extension.
                newFileFullPath = "${pathField.text}/${nameField.text.formatAsFileName()}"
                        .replace(Regex("(\\.tex)+$", RegexOption.IGNORE_CASE), "")
            }
        }
    }
}