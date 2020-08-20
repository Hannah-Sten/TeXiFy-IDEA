package nl.hannahsten.texifyidea.action.skim

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.EditorAction
import nl.hannahsten.texifyidea.action.ForwardSearchActionBase
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * Starts a forward search action in Skim.
 *
 * Note: this is only available on MacOS.
 *
 * @author Stephan Sundermann
 */
open class ForwardSearchAction : ForwardSearchActionBase(PdfViewer.SKIM)
