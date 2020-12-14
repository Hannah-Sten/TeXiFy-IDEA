package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.index.LatexIncludesIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.getIncludeCommands
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Hannah Schellekens
 */
class LatexCommandsStubElementType(debugName: String) :
    IStubElementType<LatexCommandsStub, LatexCommands>(debugName, LatexLanguage.INSTANCE) {
    override fun createPsi(latexCommandsStub: LatexCommandsStub): LatexCommands {
        return object : LatexCommandsImpl(latexCommandsStub, this) {
            init {
                this.name = latexCommandsStub.name!!
            }
        }
    }

    override fun createStub(latexCommands: LatexCommands, parent: StubElement<*>?): LatexCommandsStub {
        val commandToken = latexCommands.commandToken.text
        val requiredParameters = latexCommands.requiredParameters
        val optionalParameters: List<String> =
            LinkedList(latexCommands.optionalParameters.keys)
        return LatexCommandsStubImpl(
            parent!!, this,
            commandToken,
            requiredParameters,
            optionalParameters
        )
    }

    override fun getExternalId(): String {
        return "texify.latex.commands"
    }

    @Throws(IOException::class)
    override fun serialize(
        latexCommandsStub: LatexCommandsStub,
        stubOutputStream: StubOutputStream
    ) {
        stubOutputStream.writeName(latexCommandsStub.name)
        stubOutputStream.writeName(serialiseRequired(latexCommandsStub))
        stubOutputStream.writeName(serialiseOptional(latexCommandsStub))
    }

    @Throws(IOException::class)
    override fun deserialize(stubInputStream: StubInputStream, parent: StubElement<*>): LatexCommandsStub {
        val name = stubInputStream.readName().toString()
        val required = deserialiseList(stubInputStream.readName().toString())
        val optional = deserialiseList(stubInputStream.readName().toString())
        return LatexCommandsStubImpl(
            parent, this,
            name,
            required,
            optional
        )
    }

    override fun indexStub(latexCommandsStub: LatexCommandsStub, indexSink: IndexSink) {
        indexSink.occurrence(
            LatexCommandsIndex.key(),
            latexCommandsStub.commandToken
        )
        val token = latexCommandsStub.commandToken
        if (getIncludeCommands().contains(token)) {
            indexSink.occurrence(LatexIncludesIndex.key(), token)
        }
        if (Magic.Command.definitions.contains(token) || Magic.Command.redefinitions.contains(token)) {
            indexSink.occurrence(LatexDefinitionIndex.key(), token)
        }
    }

    private fun deserialiseList(string: String): List<String> {
        return SEPERATOR.splitAsStream(string)
            .collect(Collectors.toList())
    }

    private fun serialiseRequired(stub: LatexCommandsStub): String {
        return java.lang.String.join(SEPERATOR.pattern(), stub.requiredParams)
    }

    private fun serialiseOptional(stub: LatexCommandsStub): String {
        return java.lang.String.join(SEPERATOR.pattern(), stub.optionalParams)
    }

    companion object {
        private val SEPERATOR =
            Pattern.compile("\u1923\u9123\u2d20 hello\u0012")
    }
}