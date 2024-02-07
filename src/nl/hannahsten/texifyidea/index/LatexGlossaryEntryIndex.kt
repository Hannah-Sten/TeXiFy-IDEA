package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * Index all (supported) glossary entry commands.
 */
class LatexGlossaryEntryIndex : StringStubIndexExtension<LatexCommands>() {

    object Util : IndexUtilBase<LatexCommands>(LatexCommands::class.java, IndexKeys.GLOSSARY_ENTRIES_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}