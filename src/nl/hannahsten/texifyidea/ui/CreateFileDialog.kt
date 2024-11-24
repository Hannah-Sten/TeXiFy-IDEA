package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.formatAsFilePath
import java.io.File
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author Abby Berkers
 *
 * Dialog to ask the user for a file name and location when creating a new file.
 *
 * @param basePath The path of the file to which the newFileName is relative
 * @param newFileName The name of the new file, with or without the extension. Will be reformatted before being shown to the user.
 * @param newFileFullPath The full path of the new file, without tex extension.
 */
class CreateFileDialog(private val basePath: String?, private val newFileName: String, var newFileFullPath: String? = null) {

    init {
        DialogBuilder().apply {
            setTitle("Create New File")
            val panel = JPanel()
            panel.layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Field to enter the name of the new file.
            // If only the file is new, but the directory exists, use the existing directory and don't change it to follow conventions
            val formattedPath = if (basePath != null) {
                var existingPath = LocalFileSystem.getInstance().findFileByPath(basePath)
                var existingRelativePath = ""
                val partsToFormat = newFileName.split('/').dropWhile { part ->
                    if (existingPath?.exists() == false) return@dropWhile false
                    existingPath = existingPath?.children?.firstOrNull { it.name == part } ?: return@dropWhile false
                    existingRelativePath += "$part/"
                    true
                }
                existingRelativePath + partsToFormat.joinToString("/").formatAsFilePath()
            }
            else {
                newFileName.formatAsFilePath()
            }
            val nameField = JTextField(formattedPath)

            // Field to select the folder/location of the new file.
            val pathField = TextFieldWithBrowseButton()
            pathField.text = basePath ?: return@apply
            // Make sure the dialog is wide enough to fit the whole path in the text field.
            pathField.textField.columns = pathField.text.length

            // Add a listener to the browse button to browse a folder
            pathField.addBrowseFolderListener(
                TextBrowseFolderListener(
                    FileChooserDescriptor(false, true, false, false, false, false)
                        .withTitle("Select Folder of New File")
                )
            )

            // Add the fields to the panel, with a useful label.
            panel.add(LabeledComponent.create(nameField, "File path (extension optional, relative to base directory)"))
            panel.add(LabeledComponent.create(pathField, "Base directory"))

            setCenterPanel(panel)
            addCancelAction()
            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            // Focus the filename field.
            setPreferredFocusComponent(nameField)

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                val pathEndIndex = nameField.text.lastIndexOf('/')
                // If there is no '/' in the file name, we can use the path from the path field box.
                val path = if (pathEndIndex == -1) {
                    pathField.text
                }
                // If there is a '/' in the file name, we have to append the path with the folders given in the file name.
                else {
                    pathField.text + "/" + nameField.text.substring(0, pathEndIndex)
                }

                // Create the directories, if they do not yet exist.
                File(path).mkdirs()
                // Format the text from the name field as a file name (e.g. " " -> "-") and remove the (double) tex extension.
                // We do not check the filename here, as we already suggested a valid name so if the user decides to change it, it's not our problem.
                newFileFullPath = "$path/${nameField.text.substring(pathEndIndex + 1)}"
                    .replace(Regex("(\\.tex)+$", RegexOption.IGNORE_CASE), "")
            }
        }
    }
}