package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexStubIndexKeys
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.util.parser.forEachDirectChildTyped
import nl.hannahsten.texifyidea.util.parser.toStringMap
import java.io.IOException
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Hannah Schellekens
 */
class LatexCommandsStubElementType(debugName: String) :
    IStubElementType<LatexCommandsStub, LatexCommands>(debugName, LatexLanguage) {

    override fun createPsi(latexCommandsStub: LatexCommandsStub): LatexCommands = LatexCommandsImpl(latexCommandsStub, this)

    override fun createStub(latexCommands: LatexCommands, parent: StubElement<*>): LatexCommandsStub {
        val commandToken = latexCommands.commandToken.text
        val parameterContents = mutableListOf<LatexParameterStub>()
        latexCommands.forEachDirectChildTyped<LatexParameter> {
            val text = it.contentText()
            val type = if (it.requiredParam != null) LatexParameterStub.REQUIRED
            else if (it.optionalParam != null) LatexParameterStub.OPTIONAL else return@forEachDirectChildTyped
            parameterContents.add(LatexParameterStub(type, text))
        }
        val optionalParametersMap: Map<String, String> =
            latexCommands.getOptionalParameterMap().toStringMap()
        return LatexCommandsStubImpl(
            parent, this,
            commandToken, parameterContents, optionalParametersMap
        )
    }

    // Should equal externalIdPrefix from registration in index.xml plus field name in LatexStubElementTypes
    override fun getExternalId() = "texify.latex." + super.toString()

    private fun writeParameters(
        parameters: List<LatexParameterStub>,
        output: StubOutputStream
    ) {
        output.writeVarInt(parameters.size)
        parameters.forEach { parameter ->
            output.writeByte(parameter.type)
            output.writeUTFFast(parameter.content)
        }
    }

    private fun readParameters(input: StubInputStream): List<LatexParameterStub> {
        val size = input.readVarInt()
        return (0 until size).map {
            val type = input.readByte().toInt()
            val content = input.readUTFFast()
            LatexParameterStub(type, content)
        }
    }

    @Throws(IOException::class)
    override fun serialize(
        latexCommandsStub: LatexCommandsStub,
        stubOutputStream: StubOutputStream
    ) {
        stubOutputStream.writeName(latexCommandsStub.name)
        writeParameters(latexCommandsStub.parameters, stubOutputStream)
        stubOutputStream.writeName(serialiseOptionalMap(latexCommandsStub))
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, parent: StubElement<*>): LatexCommandsStub {
        val name = stubInputStream.readName().toString()
        val parameters = readParameters(stubInputStream)
        val optional = deserializeMap(stubInputStream.readName().toString())
        return LatexCommandsStubImpl(
            parent, this, name, parameters, optional
        )
    }

    private fun deserializeMap(fromString: String): Map<String, String> {
        if(fromString.isEmpty()) return emptyMap()
        val keyValuePairs = deserialiseList(fromString)
        return keyValuePairs.filter { it.isNotEmpty() }.associate {
            val parts = it.split(KEY_VALUE_SEPARATOR)
            parts[0] to parts[1]
        }
    }

    override fun indexStub(stub: LatexCommandsStub, sink: IndexSink) {
        val token = stub.commandToken
        sink.occurrence(LatexStubIndexKeys.COMMANDS, token)

        NewSpecialCommandsIndex.sinkIndex(sink, token) // all commands classified
        NewDefinitionIndex.sinkIndex(stub, sink) // record definitions
        NewLabelsIndex.sinkIndexCommand(stub, sink) // labels
    }

    private fun deserialiseList(string: String): List<String> = LIST_ELEMENT_SEPARATOR.splitAsStream(string)
        .collect(Collectors.toList())

    private fun serialiseOptionalMap(stub: LatexCommandsStub): String {
        val keyValuePairs = stub.optionalParamsMap.map { "${it.key}=${it.value}" }
        return java.lang.String.join(LIST_ELEMENT_SEPARATOR.pattern(), keyValuePairs)
    }

    companion object {

        private val LIST_ELEMENT_SEPARATOR =
            Pattern.compile("\u1923\u9123\u2d20 hello\u0012")
        private val KEY_VALUE_SEPARATOR = "=".toRegex()
    }
}