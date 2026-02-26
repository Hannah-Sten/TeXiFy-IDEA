package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState

internal data class LatexRunStepContext(
    val runConfig: LatexRunConfiguration,
    val environment: ExecutionEnvironment,
    val session: LatexRunSessionState,
    val mainFile: VirtualFile,
)
