package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer

class EvinceActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = PdfViewer.EVINCE.isAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}