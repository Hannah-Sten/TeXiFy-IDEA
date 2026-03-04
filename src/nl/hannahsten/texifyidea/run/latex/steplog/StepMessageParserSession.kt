package nl.hannahsten.texifyidea.run.latex.steplog

internal interface StepMessageParserSession {

    val supportsStructuredMessages: Boolean

    fun onText(text: String): List<ParsedStepMessage>
}
