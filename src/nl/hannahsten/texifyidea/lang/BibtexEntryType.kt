package nl.hannahsten.texifyidea.lang

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression

/**
 * @author Hannah Schellekens
 */
interface BibtexEntryType : Described, Dependend {

    /**
     * The token used to identify the BibTeX token _without_ `@` prefix.
     */
    val token: String

    /**
     * All required fields/keys. Empty array when no fields are required.
     */
    val required: Array<BibtexEntryField>

    /**
     * All optional fields/keys. Empty array when no fields are optional.
     */
    val optional: Array<BibtexEntryField>

    /**
     * Get all [required] and all [optional] fields. Empty collection when there are no required and no optional fields.
     */
    fun allFields(): Collection<BibtexEntryField> {
        val list: MutableList<BibtexEntryField> = ArrayList()
        list.addAll(required)
        list.addAll(optional)
        return list
    }

    /**
     * Create a template to insert when inserting this BibTeX entry.
     */
    fun template(): TemplateImpl {
        val keyValueString = required.mapIndexed { i: Int, field: BibtexEntryField ->
            $$"$${field.fieldName} = {$__Variable$${i + 1}$}"
        }.joinToString(",\n")

        val templateString = $$"{$__Variable0$,\n$$keyValueString,$END$\n}"

        val template = object : TemplateImpl("", templateString, "") {
            override fun isToReformat(): Boolean = true
        }
        template.addVariable(TextExpression("identifier"), true)
        required.forEach { template.addVariable(TextExpression(it.fieldName), true) }
        return template
    }
}