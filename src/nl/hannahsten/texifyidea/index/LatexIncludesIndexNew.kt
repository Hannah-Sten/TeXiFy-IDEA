package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

object LatexCommandsIndexNew : StringStubIndexExtension<LatexCommands>() {

    private val myKey = StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.commands")
    override fun getKey(): StubIndexKey<String?, LatexCommands?> {
        return myKey
    }

    override fun getVersion(): Int {
        return LatexParserDefinition.Cache.FILE.stubVersion
    }


    fun getIncludeCommands(
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.allScope(project)
    ): Map<String, List<LatexCommands>> {
        return IndexCommandsUtilBase.getCommandsByName(project, scope, KEY)
    }
}