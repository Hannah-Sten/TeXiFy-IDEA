package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.lang.Package.Companion.DEFAULT
import nl.rubensten.texifyidea.util.PackageUtils

/**
 * @author Ruben Schellekens
 */
class LatexCommandPackageIncludeHandler : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, item: LookupElement) {
        val command = item.`object` as LatexCommand
        handleInsert(insertionContext.document, insertionContext.file, command)
    }

    fun handleInsert(document: Document, file: PsiFile, command: LatexCommand) {
        val pack = command.dependency
        if (pack == DEFAULT) {
            return
        }

        val includedPackages = PackageUtils.getIncludedPackages(file)
        if (!includedPackages.contains(pack.name)) {
            PackageUtils.insertUsepackage(
                    document,
                    file,
                    pack.name,
                    StringUtil.join(pack.parameters, ",")
            )
        }
    }
}
