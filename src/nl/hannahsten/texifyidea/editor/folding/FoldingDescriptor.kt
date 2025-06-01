package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * A helper function to create a [FoldingDescriptor] with the given parameters.
 */
fun foldingDescriptor(
    element: PsiElement,
    range: TextRange,
    placeholderText: String,
    isCollapsedByDefault: Boolean,
    group: FoldingGroup? = null,
    neverExpand: Boolean = false,
    dependencies: Set<Any> = emptySet(),
): FoldingDescriptor {
    return FoldingDescriptor(
        element.node, range, group, dependencies, neverExpand, placeholderText,
        isCollapsedByDefault
    )
}