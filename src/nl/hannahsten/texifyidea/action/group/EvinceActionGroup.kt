package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer

class EvinceActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = InternalPdfViewer.EVINCE.isAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}