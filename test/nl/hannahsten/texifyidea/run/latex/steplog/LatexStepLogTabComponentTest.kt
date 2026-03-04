package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexStepType
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import nl.hannahsten.texifyidea.run.latex.flow.StepLogEvent
import nl.hannahsten.texifyidea.run.latex.step.InlineLatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import java.nio.file.Path

class LatexStepLogTabComponentTest : BasePlatformTestCase() {

    fun testSplitOutputForParsingPreservesLineBoundaries() {
        assertEquals(
            listOf("line1\n", "line2\n", "tail"),
            LatexStepLogTabComponent.splitOutputForParsing("line1\nline2\ntail")
        )
        assertEquals(
            listOf("single line"),
            LatexStepLogTabComponent.splitOutputForParsing("single line")
        )
        assertEquals(
            emptyList<String>(),
            LatexStepLogTabComponent.splitOutputForParsing("")
        )
    }

    fun testOverfullUnderfullMatcherFollowsLegacyKeywords() {
        assertTrue(LatexStepLogTabComponent.isOverfullUnderfullMessage("Overfull \\hbox (1.0pt too wide) in paragraph at lines 4--5"))
        assertTrue(LatexStepLogTabComponent.isOverfullUnderfullMessage("underfull \\vbox (badness 10000) has occurred while \\output is active"))
        assertFalse(LatexStepLogTabComponent.isOverfullUnderfullMessage("Package hyperref Warning: Token not allowed"))
    }

    fun testTryJumpMessageInConsoleDoesNotRefreshWhenOffsetMissing() {
        val fixture = createFixture()
        try {
            emitStepOutput(fixture.component, fixture.step, "alpha\nbeta\n")
            renderStepRawLog(fixture.component, 0)
            val beforeText = privateField<String>(fixture.component, "renderedOutputText")
            val beforeStep = privateField<Int?>(fixture.component, "renderedStepIndex")

            val result = tryJump(fixture.component, messageNodeData(fixture.component, 0, null))

            assertFalse(result)
            assertEquals(beforeText, privateField<String>(fixture.component, "renderedOutputText"))
            assertEquals(beforeStep, privateField<Int?>(fixture.component, "renderedStepIndex"))
        }
        finally {
            Disposer.dispose(fixture.component)
        }
    }

    fun testTryJumpMessageInConsoleAcceptsDistinctOffsets() {
        val fixture = createFixture()
        try {
            emitStepOutput(fixture.component, fixture.step, "alpha\nbeta\n")

            val first = tryJump(fixture.component, messageNodeData(fixture.component, 0, 0))
            val second = tryJump(fixture.component, messageNodeData(fixture.component, 0, 6))

            assertTrue(first)
            assertTrue(second)
            assertEquals(0, privateField<Int?>(fixture.component, "renderedStepIndex"))
        }
        finally {
            Disposer.dispose(fixture.component)
        }
    }

    fun testBibtexFilterHidesBibtexMessages() {
        val fixture = createFixture(LatexStepType.BIBTEX)
        try {
            setPrivateField(fixture.component, "showBibtexMessages", false)
            addParsedMessage(
                fixture.component,
                stepIndex = 0,
                message = ParsedStepMessage(
                    message = "Warning-- I found no database entries",
                    level = ParsedStepMessageLevel.WARNING,
                ),
                logOffset = 0,
            )

            assertEquals(1, parsedRecordCount(fixture.component, 0))
            assertEquals(0, messageNodeCount(fixture.component, 0))
        }
        finally {
            Disposer.dispose(fixture.component)
        }
    }

    fun testComponentReadsPersistedUiStateOnInit() {
        val config = LatexStepLogUiConfiguration.getInstance(project)
        config.loadState(
            LatexStepLogUiConfiguration(
                expanded = false,
                showBibtexMessages = false,
                showOverfullUnderfullMessages = false,
            )
        )

        val fixture = createFixture()
        try {
            assertFalse(privateField<Boolean>(fixture.component, "isTreeExpanded"))
            assertFalse(privateField<Boolean>(fixture.component, "showBibtexMessages"))
            assertFalse(privateField<Boolean>(fixture.component, "showOverfullUnderfullMessages"))
        }
        finally {
            Disposer.dispose(fixture.component)
        }
    }

    fun testUiStateWriteBackToConfiguration() {
        val config = LatexStepLogUiConfiguration.getInstance(project)
        config.loadState(
            LatexStepLogUiConfiguration(
                expanded = true,
                showBibtexMessages = true,
                showOverfullUnderfullMessages = true,
            )
        )

        val fixture = createFixture()
        try {
            invokeNoArgBooleanSetter(fixture.component, "setTreeExpanded", false)
            invokeNoArgBooleanSetter(fixture.component, "setShowBibtexMessages", false)
            invokeNoArgBooleanSetter(fixture.component, "setShowOverfullUnderfullMessages", false)

            assertFalse(config.expanded)
            assertFalse(config.showBibtexMessages)
            assertFalse(config.showOverfullUnderfullMessages)
        }
        finally {
            Disposer.dispose(fixture.component)
        }
    }

    private fun createFixture(stepType: String = LatexStepType.LATEX_COMPILE): StepLogFixture {
        val mainFile = myFixture.addFileToProject("step-log-main.tex", "\\documentclass{article}").virtualFile
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val session = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = mainFile.parent,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = mainFile.parent,
        )
        val context = LatexRunStepContext(runConfig, environment, session)
        val step = TestInlineStep(id = stepType, configId = "s1")
        val handler = StepAwareSequentialProcessHandler(listOf(step), context)
        val component = LatexStepLogTabComponent(project, mainFile, handler)
        return StepLogFixture(component, step)
    }

    private fun emitStepOutput(component: LatexStepLogTabComponent, step: LatexRunStep, text: String) {
        handleEvent(component, StepLogEvent.StepOutput(0, step, text, ProcessOutputTypes.STDOUT))
    }

    private fun handleEvent(component: LatexStepLogTabComponent, event: StepLogEvent) {
        val method = component.javaClass.getDeclaredMethod("handleEvent", StepLogEvent::class.java)
        method.isAccessible = true
        method.invoke(component, event)
    }

    private fun renderStepRawLog(component: LatexStepLogTabComponent, stepIndex: Int) {
        val method = component.javaClass.getDeclaredMethod("renderStepRawLog", Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(component, stepIndex)
    }

    private fun tryJump(component: LatexStepLogTabComponent, messageData: Any): Boolean {
        val method = component.javaClass.getDeclaredMethod("tryJumpMessageInConsole", messageData.javaClass)
        method.isAccessible = true
        return method.invoke(component, messageData) as Boolean
    }

    private fun addParsedMessage(
        component: LatexStepLogTabComponent,
        stepIndex: Int,
        message: ParsedStepMessage,
        logOffset: Int?,
    ) {
        val method = component.javaClass.getDeclaredMethod(
            "addParsedMessage",
            Int::class.javaPrimitiveType,
            ParsedStepMessage::class.java,
            Int::class.javaObjectType,
        )
        method.isAccessible = true
        method.invoke(component, stepIndex, message, logOffset)
    }

    private fun messageNodeData(component: LatexStepLogTabComponent, stepIndex: Int, logOffset: Int?): Any {
        val clazz = component.javaClass.declaredClasses.first { it.simpleName == "MessageNodeData" }
        val ctor = clazz.getDeclaredConstructor(
            ParsedStepMessage::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaObjectType,
        )
        ctor.isAccessible = true
        return ctor.newInstance(
            ParsedStepMessage(
                message = "synthetic warning",
                level = ParsedStepMessageLevel.WARNING,
            ),
            stepIndex,
            logOffset,
        )
    }

    private fun parsedRecordCount(component: LatexStepLogTabComponent, stepIndex: Int): Int {
        val map = privateField<Map<Int, List<*>>>(component, "parsedRecordsByStep")
        return map[stepIndex]?.size ?: 0
    }

    private fun messageNodeCount(component: LatexStepLogTabComponent, stepIndex: Int): Int {
        val stepNodes = privateField<Map<Int, Any>>(component, "stepNodes")
        val stepNode = stepNodes[stepIndex] ?: return 0
        val children = privateField<List<Any>>(stepNode, "children")
        return children.count { child ->
            privateField<Any?>(child, "messageData") != null
        }
    }

    private fun setPrivateField(instance: Any, name: String, value: Any?) {
        val field = instance.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(instance, value)
    }

    private fun invokeNoArgBooleanSetter(instance: Any, methodName: String, value: Boolean) {
        val method = instance.javaClass.getDeclaredMethod(methodName, Boolean::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(instance, value)
    }

    private fun <T> privateField(instance: Any, name: String): T {
        val field = instance.javaClass.getDeclaredField(name)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(instance) as T
    }

    private data class StepLogFixture(
        val component: LatexStepLogTabComponent,
        val step: LatexRunStep,
    )

    private class TestInlineStep(
        override val id: String,
        override val configId: String,
    ) : InlineLatexRunStep {

        override fun runInline(context: LatexRunStepContext): Int = 0
    }
}
