package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

class DebugActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = true

    override fun hideIfNoVisibleChildren(): Boolean = true
}