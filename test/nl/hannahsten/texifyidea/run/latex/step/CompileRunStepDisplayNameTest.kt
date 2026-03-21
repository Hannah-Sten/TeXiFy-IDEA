package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode

class CompileRunStepDisplayNameTest : BasePlatformTestCase() {

    fun testLatexCompileRunStepUsesConfiguredDisplayName() {
        val stepOptions = LatexCompileStepOptions().apply {
            compiler = LatexCompiler.LUALATEX
        }

        assertEquals(stepOptions.displayName(), LatexCompileRunStep(stepOptions).displayName)
    }

    fun testLatexmkCompileRunStepUsesConfiguredDisplayName() {
        val stepOptions = LatexmkCompileStepOptions().apply {
            latexmkCompileMode = LatexmkCompileMode.XELATEX_PDF
        }

        assertEquals(stepOptions.displayName(), LatexmkCompileRunStep(stepOptions).displayName)
    }
}
