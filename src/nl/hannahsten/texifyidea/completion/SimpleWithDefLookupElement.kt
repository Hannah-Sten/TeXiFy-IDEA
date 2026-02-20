package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.index.SourcedDefinition
import javax.swing.Icon

/**
 * This customized [LookupElement] is used to represent a definition with a specific lookup string.
 */
data class SimpleWithDefLookupElement(
    val def: SourcedDefinition, private val lookupString: String,
    private val caseSensitive: Boolean = true,
    private val insertHandler: InsertHandler<in SimpleWithDefLookupElement>? = null,
    private val renderer: LookupElementRenderer<LookupElement>? = null,
    private val expensiveRenderer: LookupElementRenderer<LookupElement>? = null,
    private val hardcodedPresentation: LookupElementPresentation? = null,
    private val allLookupStrings: Set<String> = setOf(lookupString)
) : LookupElement() {

    init {
        StringUtil.assertValidSeparators(lookupString)
    }

    companion object {
        fun create(
            def: SourcedDefinition, lookupString: String,
            insertHandler: InsertHandler<in SimpleWithDefLookupElement>? = null,
            presentableText: String? = null,
            caseSensitive: Boolean = true,
            typeText: String? = null,
            tailText: String? = null, tailTextGrayed: Boolean = false,
            bold: Boolean = false, italic: Boolean = false,
            icon: Icon? = null
        ): SimpleWithDefLookupElement {
            val presentation = LookupElementPresentation().apply {
                this.itemText = presentableText ?: lookupString
                this.typeText = typeText
                this.setTailText(tailText, tailTextGrayed)
                this.icon = icon
                this.isItemTextBold = bold
                this.isItemTextItalic = italic
            }
            return SimpleWithDefLookupElement(
                def,
                lookupString = lookupString,
                insertHandler = insertHandler,
                caseSensitive = caseSensitive,
                hardcodedPresentation = presentation
            )
        }
    }

    override fun getLookupString(): String = lookupString

    override fun getAllLookupStrings(): Set<String> = allLookupStrings

    override fun getObject(): Any = def

    override fun getPsiElement(): PsiElement? = def.definitionCommandPointer?.element

    override fun isCaseSensitive(): Boolean = caseSensitive

    override fun handleInsert(context: InsertionContext) {
        insertHandler?.handleInsert(context, this)
    }

    override fun renderElement(presentation: LookupElementPresentation) {
        when {
            renderer != null -> renderer.renderElement(this, presentation)
            hardcodedPresentation != null -> presentation.copyFrom(hardcodedPresentation)
            else -> presentation.itemText = lookupString
        }
    }

    override fun getExpensiveRenderer(): LookupElementRenderer<out LookupElement>? = expensiveRenderer

    override fun toString(): String = "SimpleLookupElement(string=$lookupString; handler=$insertHandler)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleWithDefLookupElement

        if (def != other.def) return false
        if (lookupString != other.lookupString) return false
        if (insertHandler != other.insertHandler) return false

        return true
    }

    override fun hashCode(): Int {
        var result = def.hashCode()
        result = 31 * result + lookupString.hashCode()
        result = 31 * result + (insertHandler?.hashCode() ?: 0)
        return result
    }
}