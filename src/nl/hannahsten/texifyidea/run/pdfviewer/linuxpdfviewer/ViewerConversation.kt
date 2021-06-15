package nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer

import com.intellij.openapi.project.Project

abstract class ViewerConversation {

    abstract fun forwardSearch(pdfPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean): Int
}