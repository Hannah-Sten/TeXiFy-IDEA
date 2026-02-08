package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import nl.hannahsten.texifyidea.completion.SimpleWithDefLookupElement
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.PackageUtils

object LatexAddImportInsertHandler : InsertHandler<SimpleWithDefLookupElement> {

    override fun handleInsert(context: InsertionContext, item: SimpleWithDefLookupElement) {
        if (!TexifySettings.getState().automaticDependencyCheck) return
        val def = item.def
        val entity = def.entity
        val defBundle = LatexDefinitionService.getInstance(context.project).getDefBundlesMerged(context.file)
        if (defBundle.containsLibrary(entity.dependency)) return
        PackageUtils.insertUsepackage(context.file, entity.dependency)
    }
}