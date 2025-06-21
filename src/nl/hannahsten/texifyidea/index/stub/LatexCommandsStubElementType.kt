package nl.hannahsten.texifyidea.index.stub

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.testFramework.LightVirtualFile
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.index.*
import nl.hannahsten.texifyidea.index.file.LatexIndexableSetContributor
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.util.defaultIncludeCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.toStringMap
import java.io.IOException
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Hannah Schellekens
 */
class LatexCommandsStubElementType(debugName: String) :
    IStubElementType<LatexCommandsStub, LatexCommands>(debugName, LatexLanguage) {

    override fun createPsi(latexCommandsStub: LatexCommandsStub): LatexCommands {
        return object : LatexCommandsImpl(latexCommandsStub, this) {

            override fun getElementType(): IStubElementType<*, *> {
                return this@LatexCommandsStubElementType
            }

            override fun getStub(): LatexCommandsStub {
                return latexCommandsStub
            }

            override fun setName(newName: String): PsiElement {
                this.name = newName
                return this
            }

            init {
                this.name = latexCommandsStub.name!!
            }
        }
    }

    override fun createStub(latexCommands: LatexCommands, parent: StubElement<*>?): LatexCommandsStub {
        val commandToken = latexCommands.commandToken.text
        val requiredParameters = latexCommands.getRequiredParameters()
        val optionalParameters: Map<String, String> =
            latexCommands.getOptionalParameterMap().toStringMap()
        return LatexCommandsStubImpl(
            parent!!, this,
            commandToken,
            requiredParameters,
            optionalParameters
        )
    }

    // Should equal externalIdPrefix from registration in index.xml plus field name in LatexStubElementTypes
    override fun getExternalId() = "texify.latex." + super.toString()

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
        val optional = deserializeMap(stubInputStream.readName().toString())
        return LatexCommandsStubImpl(
            parent, this,
            name,
            required,
            optional
        )
    }

    private fun deserializeMap(fromString: String): Map<String, String> {
        val keyValuePairs = deserialiseList(fromString)
        return keyValuePairs.filter { it.isNotEmpty() }.associate {
            val parts = it.split(KEY_VALUE_SEPARATOR)
            parts[0] to parts[1]
        }
    }

    override fun indexStub(latexCommandsStub: LatexCommandsStub, sink: IndexSink) {
        // We do not want all the commands from all package source files in this index, because
        // then we end up with 200k keys for texlive full, but we need to iterate over all keys
        // every time we need to get e.g. all commands in a file, so that would be too slow.
        // Therefore, we check if the indexing of this file was caused by being in an extra project root or not
        // It seems we cannot make a distinction that we do want to index with LatexExternalCommandIndex but not this index
        // Unfortunately, this seems to make indexing five times slower
        val pathOfCurrentlyIndexedFile = (latexCommandsStub.psi?.containingFile?.viewProvider?.virtualFile as? LightVirtualFile)?.originalFile?.path

        // If any of the sdk source roots is part of the currently indexed path, don't index the file
        if (getAdditionalProjectRoots(latexCommandsStub.psi?.project).any { pathOfCurrentlyIndexedFile?.contains(it) == true }) {
            return
        }

        val token = latexCommandsStub.commandToken
        sink.occurrence(LatexStubIndexKeys.COMMANDS, token)
        if (token in defaultIncludeCommands) {
            sink.occurrence(LatexStubIndexKeys.COMMANDS, LatexCommandsIndexNew.KEY_INCLUDE_COMMAND)
        }
        if (token in CommandMagic.definitions) {
            sink.occurrence(LatexStubIndexKeys.COMMANDS, LatexCommandsIndexNew.KEY_DEFINITION)
        }
        if (token in CommandMagic.labelAsParameter) {
            latexCommandsStub.optionalParams["label"]?.let { label ->
                sink.occurrence(LatexStubIndexKeys.LABELED_COMMANDS_KEY, label)
            }
        }
        if (token in CommandMagic.glossaryEntry && latexCommandsStub.requiredParams.isNotEmpty()) {
            sink.occurrence(LatexStubIndexKeys.GLOSSARY_ENTRIES_KEY, latexCommandsStub.requiredParams[0])
        }
    }

    private fun deserialiseList(string: String): List<String> {
        return LIST_ELEMENT_SEPARATOR.splitAsStream(string)
            .collect(Collectors.toList())
    }

    private fun serialiseRequired(stub: LatexCommandsStub): String {
        return java.lang.String.join(LIST_ELEMENT_SEPARATOR.pattern(), stub.requiredParams)
    }

    private fun serialiseOptional(stub: LatexCommandsStub): String {
        val keyValuePairs = stub.optionalParams.map { "${it.key}=${it.value}" }
        return java.lang.String.join(LIST_ELEMENT_SEPARATOR.pattern(), keyValuePairs)
    }

    companion object {

        private val LIST_ELEMENT_SEPARATOR =
            Pattern.compile("\u1923\u9123\u2d20 hello\u0012")
        private val KEY_VALUE_SEPARATOR = "=".toRegex()

        private var projectRootsCache: List<String>? = null

        fun getAdditionalProjectRoots(project: Project?): List<String> {
            if (projectRootsCache == null && project != null) {
                projectRootsCache = LatexIndexableSetContributor().getAdditionalProjectRootsToIndex(project).map { it.path }
            }
            return projectRootsCache ?: emptyList()
        }
    }
}