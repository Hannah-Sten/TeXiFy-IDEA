package nl.hannahsten.texifyidea.action.debug

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.completion.LatexContextAwareCompletionAdaptor
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexLibraryDefinitionService
import nl.hannahsten.texifyidea.index.projectstructure.LatexLibraryStructureService
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectFilesets
import nl.hannahsten.texifyidea.index.projectstructure.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.AbstractTexifyContextAwareInspection
import java.lang.management.ManagementFactory
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.time.Duration.Companion.milliseconds

class SimplePerformanceTracker {
    private val myCountOfBuilds: AtomicInteger = AtomicInteger(0)
    private val myTotalTimeCost: AtomicLong = AtomicLong(0)

    fun recordTimeCost(timeCost: Long) {
        myTotalTimeCost.addAndGet(timeCost)
        myCountOfBuilds.incrementAndGet()
    }

    val countOfBuilds: Int
        get() = myCountOfBuilds.get()
    val totalTimeCost: Long
        get() = myTotalTimeCost.get()

    inline fun <T> track(crossinline action: () -> T): T {
        val start = System.currentTimeMillis()
        val result = action()
        recordTimeCost(System.currentTimeMillis() - start)
        return result
    }
}

class PerformanceDiagnosticAction : AnAction() {

    private data class PerformanceData(
        val name: String,
        val count: Int,
        val totalTime: Long,
        val additionalInfo: String = ""
    )

    private fun performance(name: String, tracker: SimplePerformanceTracker, additionalInfo: String = ""): PerformanceData = PerformanceData(
        name, tracker.countOfBuilds, tracker.totalTimeCost, additionalInfo
    )

    private fun formatDurationMillis(milliseconds: Long): String = milliseconds.milliseconds.toString()

    private fun formatRuns(count: Int): String = if (count < 100000) {
        count.toString()
    }
    else {
        String.format(Locale.US, "%.4E", count.toDouble())
    }

    private fun buildFilesetInfo(projectFilesets: LatexProjectFilesets?): String = buildString {
        if (projectFilesets == null) {
            appendLine("No fileset found")
        }
        else {
            appendLine("${projectFilesets.mapping.size} files out of ${projectFilesets.filesets.size} sets")
        }
        appendLine("Expiration: ${LatexProjectStructure.expirationTime}")
    }

    private fun buildCustomDefinitionsInfo(): String = """
            Expiration: ${LatexDefinitionService.expirationTime}
    """.trimIndent()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // show a dialog with performance diagnostic information
        val libService = LatexLibraryStructureService.getInstance(project)
        val projectFilesets = LatexProjectStructure.getFilesets(project)
        val tableData = listOf(
            performance(
                "Library Structure", LatexLibraryStructureService.performanceTracker,
                "Loaded Libraries: ${libService.librarySize()}"
            ),
            PerformanceData(
                "Fileset", LatexProjectStructure.performanceTracker.countOfBuilds, LatexProjectStructure.performanceTracker.totalTimeCost,
                buildFilesetInfo(projectFilesets)
            ),
            performance("Package Definitions", LatexLibraryDefinitionService.performanceTracker),
            performance("Custom Definitions", LatexDefinitionService.performanceTracker, buildCustomDefinitionsInfo()),
            performance("Completion Lookup", LatexContextAwareCompletionAdaptor.performanceTracker),
            performance("Ctx-aware Inspections", AbstractTexifyContextAwareInspection.performanceTracker)
        )

        val totalRunningTime = ManagementFactory.getRuntimeMXBean().uptime + 1 // +1 to avoid division by zero
        val messageHtml = createHTML(true).html {
            body {
                h2 { +TexifyBundle.message("ui.performance.diagnostic.header") }
                table {
                    tr {
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.name") }
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.cpu.time") }
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.uptime.percent") }
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.runs") }
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.average.time") }
                        td { +TexifyBundle.message("ui.performance.diagnostic.column.additional.info") }
                    }
                    for (data in tableData) {
                        tr {
                            td { +data.name }
                            td { +formatDurationMillis(data.totalTime) }
                            td {
                                val percent = (data.totalTime.toDouble() / totalRunningTime * 100).toInt()
                                +"$percent%"
                            }
                            td { +formatRuns(data.count) }
                            td {
                                if (data.count > 0) {
                                    +formatDurationMillis(data.totalTime / data.count)
                                }
                                else {
                                    +TexifyBundle.message("ui.performance.diagnostic.na")
                                }
                            }
                            td {
                                data.additionalInfo.lines().forEach {
                                    +it
                                    br
                                }
                            }
                        }
                    }
                }
                // add a separator line
                hr { }
                +TexifyBundle.message("ui.performance.diagnostic.footer.uptime", totalRunningTime.milliseconds)
                br
                +TexifyBundle.message("ui.performance.diagnostic.footer.cpu.time")
            }
        }
        DialogBuilder().apply {
            setTitle(TexifyBundle.message("ui.dialog.performance.diagnostic.title"))
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

    override fun isDumbAware(): Boolean = true

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}
