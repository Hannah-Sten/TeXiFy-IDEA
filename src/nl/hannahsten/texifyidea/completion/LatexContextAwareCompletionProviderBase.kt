package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandArgumentInsertHandler
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.util.optionalPowerSet
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

abstract class LatexContextAwareCompletionProviderBase : CompletionProvider<CompletionParameters>() {

    fun getLineNumberOfPsiElementStart(pointer: SmartPsiElementPointer<*>): Int? {
        val psiElement = pointer.element ?: return null // 如果指针无效，返回 null
        val project = pointer.project

        val psiFile = psiElement.containingFile ?: return null
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(psiFile) ?: return null

        // 获取 PSI 元素的 TextRange 和起始偏移量
        val textRange: TextRange = psiElement.textRange
        val startOffset = textRange.startOffset

        // 将偏移量转换为行号（行号从 0 开始）
        return document.getLineNumber(startOffset)
    }

    protected fun createDefinitionSourceText(def : SourcedDefinition) : String {
        val pointer = def.definitionCommandPointer ?: return ""
        val file = pointer.containingFile ?: return ""
        pointer.psiRange?.startOffset ?: return ""
        val element = pointer.element ?: return ""

        val arguments = getArgumentsFromDefinition(cmd)
        var typeText = getTypeText(cmd)
        val line = 1 + StringUtil.offsetToLineNumber(cmd.containingFile.text, cmd.textOffset)
        typeText = typeText + " " + cmd.containingFile.name + ":" + line
        result.addAllElements(
            arguments.toSet().optionalPowerSet().mapIndexed { index, args ->
                LookupElementBuilder.create(cmd, cmdName.substring(1) + List(index) { " " }.joinToString(""))
                    .withPresentableText(cmdName)
                    .bold()
                    .withTailText(args.joinToString(""), true)
                    .withTypeText(typeText, true)
                    .withInsertHandler(LatexCommandArgumentInsertHandler(args.toList()))
                    .withIcon(TexifyIcons.DOT_COMMAND)
            }
        )
    }

    protected abstract fun addContextAwareCompletions(
        parameters: CompletionParameters,
        contexts: LContextSet,
        defBundle: DefinitionBundle,
        processingContext: ProcessingContext, result: CompletionResultSet
    )

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return

        countOfBuilds.incrementAndGet()
        val startTime = System.currentTimeMillis()

        val file = parameters.originalFile
        val defBundle = LatexDefinitionService.getInstance(project).getDefBundlesMerged(file)
        val contexts = LatexPsiUtil.resolveContextUpward(parameters.position, defBundle)
        addContextAwareCompletions(parameters, contexts, defBundle, context, result)
        // Add a message to the user that this is an experimental feature.
        result.addLookupAdvertisement("Experimental feature: context-aware completion. ")

        totalTimeCost.addAndGet(System.currentTimeMillis() - startTime)
    }

    protected fun packageName(entity: LSemanticEntity): String {
        val name = entity.dependency
        return if (name.isEmpty()) "" else " ($name)"
    }

    companion object : SimplePerformanceTracker {
        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)
    }
}