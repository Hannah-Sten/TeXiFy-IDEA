package nl.rubensten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.removeFileExtension

/**
 * @author Ruben Schellekens
 */
open class FileNameInsertionHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext?, element: LookupElement?) {
        val text = element?.`object` ?: return
        val file = context?.file ?: return
        val document = file.document() ?: return
        val offset = context.startOffset
        val normalTextWord = file.findElementAt(offset) ?: return
        val command = normalTextWord.parentOfType(LatexCommands::class) ?: return

        if (command.name != "\\include") {
            return
        }

        val extensionless = text.toString().removeFileExtension()
        document.replaceString(command.textOffset, command.endOffset(), "\\include{$extensionless}")
    }
}