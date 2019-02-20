package nl.rubensten.texifyidea.settings.labeldefiningcommands

import java.lang.StringBuilder
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class InputCommandFilter: DocumentFilter() {
    override fun insertString(fb: FilterBypass?, offset: Int, string: String?, attr: AttributeSet?) {
        val stringWithOutSpaces = string?.replace(" ", "")
        super.insertString(fb, offset, stringWithOutSpaces, attr)
    }

    override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
        val stringWithOutSpaces = text?.replace(" ", "") ?: return

        val offsetWithoutSlash = if (offset != 0) offset else 1
        val lengthWithoutSlash = if (offset != 0) length else length - 1
        
        super.replace(fb, offsetWithoutSlash, lengthWithoutSlash, stringWithOutSpaces, attrs)
    }

    override fun remove(fb: FilterBypass?, offset: Int, length: Int) {
        val offsetWithoutSlash = if (offset != 0) offset else 1
        val lengthWithoutSlash = if (offset != 0) length else length - 1
        super.remove(fb, offsetWithoutSlash, lengthWithoutSlash)
    }
}
