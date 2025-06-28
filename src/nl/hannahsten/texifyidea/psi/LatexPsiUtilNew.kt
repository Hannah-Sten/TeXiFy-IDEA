package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.forEachDirectChild
import nl.hannahsten.texifyidea.util.parser.getOptionalParameterMapFromParameters
import nl.hannahsten.texifyidea.util.parser.toStringMap

/*
This file contains utility functions for the LaTex-related PSI elements.
 */

/**
 * Gets the name of the environment, for example `itemize` for `\begin{itemize} ... \end{itemize}`.
 */
fun LatexEnvironment.getEnvironmentName(): String {
    val stub = this.stub
    if (stub != null) return stub.environmentName
    return this.beginCommand.envIdentifier?.name ?: ""
}

/**
 * Get the environment name of the end command.
 */
fun LatexEndCommand.environmentName(): String? {
    return envIdentifier?.name
}

/**
 * Get the environment name of the begin command.
 */
fun LatexBeginCommand.environmentName(): String? {
    return envIdentifier?.name
}

/**
 * Find the label of the environment. The method finds labels inside the environment content as well as labels
 * specified via an optional parameter
 * Similar to LabelExtraction#extractLabelElement, but we cannot use the index here
 *
 * @return the label name if any, null otherwise
 */
fun LatexEnvironment.getLabel(): String? {
    val stub = this.stub
    if (stub != null) return stub.label
    return if (EnvironmentMagic.labelAsParameter.contains(this.getEnvironmentName())) {
        // See if we can find a label option
        val optionalParameters = getOptionalParameterMapFromParameters(this.beginCommand.parameterList).toStringMap()
        optionalParameters.getOrDefault("label", null)
    }
    else {
        // Not very clean. We don't really need the conventions here, but determine which environments *can* have a
        // label. However, if we didn't use the conventions, we would have to duplicate the information in
        // EnvironmentMagic
        val conventionSettings = TexifyConventionsSettingsManager.getInstance(this.project).getSettings()
        if (conventionSettings.getLabelConvention(
                this.getEnvironmentName(),
                LabelConventionType.ENVIRONMENT
            ) == null
        ) return null

        val content = this.environmentContent ?: return null

        // See if we can find a label command inside the environment
        val children = PsiTreeUtil.findChildrenOfType(content, LatexCommands::class.java)
        if (!children.isEmpty()) {
            // We cannot include user defined labeling commands, because to get them we need the index,
            // but this code is used to create the index (for environments)
            val labelCommands = CommandMagic.labelDefinitionsWithoutCustomCommands
            val labelCommand =
                children.firstOrNull { c: LatexCommands -> labelCommands.contains(c.name) } ?: return null
            val requiredParameters = labelCommand.getRequiredParameters()
            if (requiredParameters.isEmpty()) return null
            val info = CommandManager.labelAliasesInfo.getOrDefault(labelCommand.name, null) ?: return null
            if (!info.labelsPreviousCommand) return null
            val parameterPosition = info.positions.firstOrNull() ?: 0
            return if (parameterPosition > requiredParameters.size - 1 || parameterPosition < 0) null else requiredParameters[parameterPosition]
        }
        null
    }
}

/**
 * Gets the contextual surrounder of the current [com.intellij.psi.PsiElement].
 *
 * See the example below for the structure of the PSI tree, the contextual surrounder is the `no_math_content` elements or `PsiWhiteSpace`
 * ```
 * - root
 *   - no_math_content
 *     - element
 *   - PsiWhiteSpace
 *   - no_math_content
 *     - element
 * ```
 */
fun PsiElement.contextualSurrounder(): PsiElement? {
    // Find the contextual element of the current element in the PSI tree.
    // The current element is surrounded by `no_math_content`, so we should go an extra level up
    return when (this) {
        is PsiWhiteSpace -> this
        else -> parent as? LatexNoMathContent
    }
}

/**
 * This is a template function for traversing the contextual siblings of the current [PsiElement].
 * Use [traverseContextualSiblingsNext] or [traverseContextualSiblingsPrev] instead.
 */
inline fun PsiElement.traverseContextualSiblingsTemplate(action: (PsiElement) -> Unit, siblingFunc: (PsiElement) -> PsiElement?) {
    val parent = contextualSurrounder() ?: return

    var siblingContent = siblingFunc(parent)
    while (siblingContent != null) {
        if (siblingContent is LatexNoMathContent) {
            val sibling = siblingContent.firstChild ?: continue
            action(sibling)
        }
        siblingContent = siblingFunc(siblingContent)
    }
}

/**
 * Gets the contextual siblings of the current [LatexComposite] element.
 *
 * See the example below for the structure of the PSI tree:
 *
 *     some root
 *       - no_math_content
 *         - previous sibling
 *       - no_math_content
 *         - the current element
 *       - no_math_content
 *         - next sibling
 *
 *
 *
 *  @see [traverseContextualSiblingsNext]
 *  @see [traverseContextualSiblingsPrev]
 */
fun <T : Any> PsiElement.contextualSiblings(): List<LatexComposite> {
    // Find the contextual siblings of the current element in the PSI tree.
    // The current element is surrounded by `no_math_content`, so we should go an extra level up
    val parent = contextualSurrounder() ?: return emptyList()
    val grandParent = parent.parent ?: return emptyList()
    val result = mutableListOf<LatexComposite>()
    grandParent.forEachDirectChild { siblingParent ->
        if (siblingParent is LatexNoMathContent) {
            siblingParent.firstChild?.let { child ->
                if (child is LatexComposite && child != this) {
                    result.add(child)
                }
            }
        }
        true // continue iterating
    }
    return result
}

/**
 * Traverse the contextual siblings of the current [LatexComposite] element that are after it in the PSI tree in order.
 *
 * You can return non-locally from the [action] to stop the traversal.
 *
 * For the structure of the PSI tree, see the example in [contextualSiblings].
 *
 * @see [contextualSiblings]
 */
inline fun PsiElement.traverseContextualSiblingsNext(action: (PsiElement) -> Unit) {
    return traverseContextualSiblingsTemplate(action) { it.nextSibling }
}

/**
 * Traverse the contextual siblings of the current [LatexComposite] element that are before it in the PSI tree in order.
 *
 * You can return non-locally in from the [action] to stop the traversal.
 *
 * For the structure of the PSI tree, see the example in [contextualSiblings].
 *
 * @see [contextualSiblings]
 *
 */
inline fun PsiElement.traverseContextualSiblingsPrev(action: (PsiElement) -> Unit) {
    return traverseContextualSiblingsTemplate(action) { it.prevSibling }
}

inline fun PsiElement.prevContextualSibling(predicate: (PsiElement) -> Boolean): PsiElement? {
    traverseContextualSiblingsPrev { sibling ->
        if (predicate(sibling)) {
            return sibling
        }
    }
    return null
}

fun PsiElement.prevContextualSiblingIgnoreWhitespace(): PsiElement? =
    prevContextualSibling { it !is PsiWhiteSpace }

/**
 * Gets the name of the command from the [PsiElement] if it is a command or is a content containing a command.
 */
fun PsiElement.asCommandName(): String? {
    return when (this) {
        is LatexCommandWithParams -> this.getName()
        is LatexNoMathContent -> this.commands?.name
        else -> null
    }
}