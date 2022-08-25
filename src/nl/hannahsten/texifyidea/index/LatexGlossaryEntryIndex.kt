package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * Index all (supported) glossary entry commands.
 */
class LatexGlossaryEntryIndex : StringStubIndexExtension<LatexCommands>() {

    companion object : IndexUtilBase<LatexCommands>(LatexCommands::class.java, IndexKeys.GLOSSARY_ENTRIES_KEY)

    @Suppress("RedundantCompanionReference") // Avoid type checking issues
    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}