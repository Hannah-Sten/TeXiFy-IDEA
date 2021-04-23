package nl.hannahsten.texifyidea.run.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vcs.LocalFilePath
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.components.JBTextField
import com.intellij.vcsUtil.VcsFileUtil
import java.awt.BorderLayout
import javax.swing.JTextField

/**
 * todo why use this instead of a plain TextfieldWithBrowserButton?
 * Text field that contains a [VirtualFile].
 *
 * The text field has a browse button to select the [VirtualFile].
 * The path of the file is displayed relative to the project root, if defined.
 * Otherwise, the absolute path is shown.
 *
 * @author Sten Wessel
 */
class VirtualFileEditorWithBrowse(label: String?, message: String?, private val project: Project) : LabeledComponent<TextFieldWithBrowseButton>(),
                                                                                                  PanelWithAnchor {

    private var _selected: VirtualFile? = null

    /**
     * The currently selected [VirtualFile].
     */
    var selected: VirtualFile?
        get() = _selected
        set(value) {
            _selected = value
            textField.setText(value?.let { it ->
                project.guessProjectDir()?.let { r ->
                    VcsFileUtil.relativePath(r, it)
                } ?: it.path
            })
        }

    private val textField = TextFieldWithBrowseButton()

    val editor = textField.textField

    init {
        text = label ?: ""
        labelLocation = BorderLayout.WEST

        if (label == null) {
            this.label.isVisible = false
        }

        // TODO: highlight when path is not valid / virtual file could not be found
        textField.addVetoableChangeListener {
            _selected = project.guessProjectDir()?.findFileByRelativePath(text)
                ?: LocalFileSystem.getInstance().findFileByPath(text)
        }

        (textField.textField as? JBTextField)?.emptyText?.text = message ?: ""
        textField.accessibleContext.accessibleName = message

        component = textField

        updateUI()
    }

    fun addBrowseFolderListener(title: String, description: String?, fileChooserDescriptor: FileChooserDescriptor) {
        textField.addBrowseFolderListener(
            title,
            description,
            project,
            fileChooserDescriptor.withRoots(
              *ProjectRootManager.getInstance(project).contentRootsFromAllModules.distinct().toTypedArray()
            ),
            object : TextComponentAccessor<JTextField> {
                override fun getText(component: JTextField): String {
                    return selected?.path ?: ""
                }

                override fun setText(component: JTextField, text: String) {
                    selected = LocalFileSystem.getInstance().findFileByPath(text)
                    component.text = project.guessProjectDir()?.let { VcsFileUtil.relativePath(it, LocalFilePath(text, false)) }
                        ?: selected?.path
                }
            }
        )
    }
}