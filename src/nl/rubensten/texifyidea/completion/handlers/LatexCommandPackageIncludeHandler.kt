package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.text.StringUtil
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.util.PackageUtils

/**
 * @author Ruben Schellekens
 */
class LatexCommandPackageIncludeHandler : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, item: LookupElement) {
        val command = item.`object` as LatexCommand
        val file = insertionContext.file

        val pack = command.dependency
        if (Package.DEFAULT == pack) {
            return
        }

        val includedPackages = PackageUtils.getIncludedPackages(file)
        if (!includedPackages.contains(pack.name)) {
            PackageUtils.insertUsepackage(
                    insertionContext.document,
                    file,
                    pack.name,
                    StringUtil.join(pack.parameters, ",")
            )
        }
    }
}
