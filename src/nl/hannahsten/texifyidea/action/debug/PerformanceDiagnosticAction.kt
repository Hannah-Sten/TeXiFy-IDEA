package nl.hannahsten.texifyidea.action.debug

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import kotlinx.html.body
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import javax.swing.JLabel
import javax.swing.SwingConstants

class PerformanceDiagnosticAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // show a dialog with performance diagnostic information
        val count = LatexProjectStructure.countOfBuilding.get()
        val totalTime = LatexProjectStructure.totalBuildTime.get()
        val messageHtml = createHTML(true).html {
            body {
                h2 { +"Performance Diagnostic Since Last Restart" }
                h3 { +"Fileset Performance" }
                table {
                    tr {
                        td { +"Total fileset builds" }
                        td { +(count.toString()) }
                    }
                    tr {
                        td { +"Total CPU time (ms)" }
                        td { +(totalTime.toString()) }
                    }
                    tr {
                        td { +"Average time (ms)" }
                        td {
                            if (count > 0) {
                                +(totalTime / count).toString()
                            } else {
                                +"N/A"
                            }
                        }
                    }
                    e.project?.let { LatexProjectStructure.getFilesets(it) }?.let { fs ->
                        tr {
                            td { +"Recent Fileset Size" }
                            td {
                                +(fs.mapping.size.toString())
                            }
                        }
                    }
                }
            }
        }
        DialogBuilder().apply {
            setTitle("Texify Performance Diagnostic")
            setCenterPanel(
                JLabel(
                    messageHtml,
                    SwingConstants.LEADING
                )
            )
            addOkAction()
            show()
        }
    }

    override fun isDumbAware(): Boolean {
        return true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}