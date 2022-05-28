package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

class LatexGlossaryEntryIndex : StringStubIndexExtension<LatexCommands>() {
    companion object : IndexUtilBase<LatexCommands>(LatexCommands::class.java, IndexKeys.GLOSSARY_ENTRIES_KEY)

    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}