package nl.hannahsten.texifyidea.action.debug

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import kotlinx.html.body
import kotlinx.html.h2
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.tr
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.PackageDefinitionService
import javax.swing.JLabel
import javax.swing.SwingConstants

class PerformanceDiagnosticAction : AnAction() {

    private data class PerformanceData(
        val name: String,
        val count: Int,
        val totalTime: Long,
        val additionalInfo: String = ""
    )

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // show a dialog with performance diagnostic information
        val tableData = listOf(
            PerformanceData(
                "Fileset", LatexProjectStructure.countOfBuilds.get(), LatexProjectStructure.totalBuildTime.get(),
                "Recent Fileset Size: ${LatexProjectStructure.getFilesets(project)?.mapping?.size ?: "N/A"}"
            ),
            PerformanceData(
                "Package Definitions", PackageDefinitionService.countOfBuilds.get(), PackageDefinitionService.totalBuildTime.get(),
            ),
            PerformanceData(
                "Command Definitions", LatexDefinitionService.countOfBuilds.get(), LatexDefinitionService.totalBuildTime.get(),
            )
        )
        val messageHtml = createHTML(true).html {
            body {
                h2 { +"Performance Diagnostic Since Last Restart" }
                table {
                    tr {
                        td { +"Name" }
                        td { +"Count" }
                        td { +"Total Time (ms)" }
                        td { +"Average" }
                        td { +"Additional Info" }
                    }
                    for (data in tableData) {
                        tr {
                            td { +data.name }
                            td { +(data.count.toString()) }
                            td { +(data.totalTime.toString()) }
                            td {
                                if (data.count > 0) {
                                    +(data.totalTime / data.count).toString()
                                }
                                else {
                                    +"N/A"
                                }
                            }
                            td {
                                +data.additionalInfo
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