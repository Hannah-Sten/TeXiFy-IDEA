package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexRequiredParam
import nl.rubensten.texifyidea.util.*

/**
 * @author Ruben Schellekens
 */
open class FileNameInsertionHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val text = element.`object`
        val file = context.file
        val document = file.document() ?: return
        val offset = context.startOffset
        val normalTextWord = file.findElementAt(offset) ?: return
        val command = normalTextWord.parentOfType(LatexCommands::class) ?: return

        if (command.name != "\\include" && command.name != "\\bibliography" && command.name != "\\addbibresource") return

        // Only replace the first command argument.
        val firstRequiredArgument = command.firstChildOfType(LatexRequiredParam::class) ?: return
        val endOffset = firstRequiredArgument.endOffset()

        val extensionless = text.toString().removeFileExtension()
        document.replaceString(command.textOffset, endOffset, "${command.name}{$extensionless}")
    }
}