package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

abstract class CollectAllStubIndexBase<Psi : PsiElement>(
    val clazz: Class<Psi>,
) : StringStubIndexExtension<Psi>() {


    abstract val keyForAll: String

    fun sinkIndex(sink: IndexSink, indexKey: StubIndexKey<String, Psi>, commandToken: String) {
        sink.occurrence(indexKey, commandToken)
        sink.occurrence(indexKey, keyForAll)
    }

    fun getAll(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)): Collection<Psi> {
        return StubIndex.getElements(
            key, keyForAll, project, LatexFileFilterScope(scope), clazz
        )
    }

    fun traverseAll(
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key,keyForAll, project, LatexFileFilterScope(scope), action)
    }


    fun traverseByName(
        name: String,
        project: Project,
        scope: GlobalSearchScope = GlobalSearchScope.projectScope(project),
        action: (Psi) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, LatexFileFilterScope(scope), action)
    }

}

abstract class NewLatexCommandsStubIndex : CollectAllStubIndexBase<LatexCommands>(LatexCommands::class.java) {

    final override val keyForAll: String
        get() = KEY_ALL_COMMANDS

    companion object {
        /**
         * A special key to retrieve all commands in the index.
         */
        const val KEY_ALL_COMMANDS: String = "_ALL_"
    }


    override fun getVersion(): Int {
        return LatexParserDefinition.Cache.FILE.stubVersion
    }


}