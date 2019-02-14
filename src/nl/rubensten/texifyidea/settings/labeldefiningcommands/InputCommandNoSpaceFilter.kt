package nl.rubensten.texifyidea.settings.labeldefiningcommands

import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class InputCommandNoSpaceFilter: DocumentFilter() {
    override fun insertString(fb: FilterBypass?, offset: Int, string: String?, attr: AttributeSet?) {
        val stringWithOutSpaces = string?.replace(" ", "")
        super.insertString(fb, offset, stringWithOutSpaces, attr)
    }

    override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
        val stringWithOutSpaces = text?.replace(" ", "")
        super.replace(fb, offset, length, stringWithOutSpaces, attrs)
    }
}
