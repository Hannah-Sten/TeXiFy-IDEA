package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.removeFileExtension
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.psi.parentOfType

/**
 * @author Hannah Schellekens
 */
open class FileNameInsertionHandler : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, element: LookupElement) {
        val text = element.`object`
        val file = context.file
        val document = file.document() ?: return
        val offset = context.startOffset
        val normalTextWord = file.findElementAt(offset) ?: return
        val command = normalTextWord.parentOfType(LatexCommands::class) ?: return

        if (command.name !in CommandMagic.illegalExtensions.keys) return

        val extensionless = text.toString().removeFileExtension()
        document.replaceString(offset, context.tailOffset, extensionless)
    }
}