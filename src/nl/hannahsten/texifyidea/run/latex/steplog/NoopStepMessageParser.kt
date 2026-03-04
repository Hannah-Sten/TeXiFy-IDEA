package nl.hannahsten.texifyidea.run.latex.steplog

internal object NoopStepMessageParser : StepMessageParserSession {

    override val supportsStructuredMessages: Boolean = false

    override fun onText(text: String): List<ParsedStepMessage> = emptyList()
}
