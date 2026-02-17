package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.runners.ExecutionEnvironment
import nl.hannahsten.texifyidea.run.latex.LatexCommandLineState

class LatexmkCommandLineState(
    environment: ExecutionEnvironment,
    runConfig: LatexmkRunConfiguration
) : LatexCommandLineState(environment, runConfig)
