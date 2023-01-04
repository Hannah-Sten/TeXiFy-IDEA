package nl.hannahsten.texifyidea.formatting

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

/**
 * Based on KotlinSpacingBuilder in the Kotlin plugin.
 *
 * @author Sten Wessel
 */
class TexSpacingBuilder(private val commonSettings: CommonCodeStyleSettings) {

    private val builders = ArrayList<Builder>()

    /**
     * Generic spacing builder.
     */
    private interface Builder {

        fun getSpacing(parent: ASTBlock, left: ASTBlock, right: ASTBlock): Spacing?
    }

    /**
     * Basic spacing builder that is based on the implementation of [SpacingBuilder].
     */
    inner class BasicSpacingBuilder : SpacingBuilder(commonSettings), Builder {

        override fun getSpacing(parent: ASTBlock, left: ASTBlock, right: ASTBlock): Spacing? {
            return super.getSpacing(parent, left, right)
        }
    }

    /**
     * Represents a rule condition.
     *
     * The rule is only matched when the condition holds.
     */
    private data class Condition(
        val parent: IElementType? = null,
        val left: IElementType? = null,
        val right: IElementType? = null,
        val parentSet: TokenSet? = null,
        val leftSet: TokenSet? = null,
        val rightSet: TokenSet? = null
    ) : (ASTBlock, ASTBlock, ASTBlock) -> Boolean {

        override fun invoke(p: ASTBlock, l: ASTBlock, r: ASTBlock): Boolean =
            (parent == null || p.node!!.elementType == parent) &&
                (left == null || l.node!!.elementType == left) &&
                (right == null || r.node!!.elementType == right) &&
                (parentSet == null || p.node!!.elementType in parentSet) &&
                (leftSet == null || l.node!!.elementType in leftSet) &&
                (rightSet == null || r.node!!.elementType in rightSet)
    }

    /**
     * Rule that upon matching the conditions returns the spacing action.
     */
    private data class Rule(
        val conditions: List<Condition>,
        val action: (ASTBlock, ASTBlock, ASTBlock) -> Spacing?
    ) : (ASTBlock, ASTBlock, ASTBlock) -> Spacing? {

        override fun invoke(p: ASTBlock, l: ASTBlock, r: ASTBlock): Spacing? =
            if (conditions.all { it(p, l, r) }) action(p, l, r) else null
    }

    /**
     * Build more advanced rules with [Rule] and [Condition] above.
     */
    inner class CustomSpacingBuilder : Builder {

        private val rules = ArrayList<Rule>()
        private var conditions = ArrayList<Condition>()

        override fun getSpacing(parent: ASTBlock, left: ASTBlock, right: ASTBlock): Spacing? {
            for (rule in rules) {
                return rule(parent, left, right) ?: continue
            }

            return null
        }

        fun inPosition(
            parent: IElementType? = null,
            left: IElementType? = null,
            right: IElementType? = null,
            parentSet: TokenSet? = null,
            leftSet: TokenSet? = null,
            rightSet: TokenSet? = null
        ): CustomSpacingBuilder {
            conditions.add(Condition(parent, left, right, parentSet, leftSet, rightSet))
            return this
        }

        fun spacing(spacing: Spacing) {
            newRule { _, _, _ -> spacing }
        }

        fun customRule(block: (parent: ASTBlock, left: ASTBlock, right: ASTBlock) -> Spacing?) {
            newRule(block)
        }

        private fun newRule(rule: (ASTBlock, ASTBlock, ASTBlock) -> Spacing?) {
            val savedConditions = ArrayList(conditions)
            rules.add(Rule(savedConditions, rule))
            conditions.clear()
        }
    }

    /**
     * Get the spacing from the composite builders.
     */
    fun getSpacing(parent: Block, child1: Block?, child2: Block): Spacing? {
        if (parent !is ASTBlock || child1 !is ASTBlock || child2 !is ASTBlock) {
            return null
        }

        for (builder in builders) {
            return builder.getSpacing(parent, child1, child2) ?: continue
        }

        return null
    }

    /**
     * Rules for the basic spacing builder implementation.
     */
    fun simple(init: BasicSpacingBuilder.() -> Unit) {
        val builder = BasicSpacingBuilder()
        builder.init()
        builders.add(builder)
    }

    fun custom(init: CustomSpacingBuilder.() -> Unit) {
        val builder = CustomSpacingBuilder()
        builder.init()
        builders.add(builder)
    }
}

/**
 * Build a [TexSpacingBuilder] with a set of rules.
 */
fun rules(latexSettings: CommonCodeStyleSettings, init: TexSpacingBuilder.() -> Unit): TexSpacingBuilder {
    val builder = TexSpacingBuilder(latexSettings)
    builder.init()
    return builder
}