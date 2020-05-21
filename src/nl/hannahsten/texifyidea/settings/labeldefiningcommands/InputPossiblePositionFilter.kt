package nl.hannahsten.texifyidea.settings.labeldefiningcommands

import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * This class is used to prevent wrong input for the position of the label parameter
 */
class InputPossiblePositionFilter : DocumentFilter() {

    override fun insertString(fb: FilterBypass?, offset: Int, string: String?, attr: AttributeSet?) {
        val doc = fb?.document ?: return
        val content = StringBuilder()
        content.append(doc.getText(0, doc.length))
        content.insert(offset, string)
        if (content.toString().toIntOrNull() == null) {
            super.insertString(fb, offset, string, attr)
        }
    }

    override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String?, attrs: AttributeSet?) {
        val doc = fb?.document ?: return
        val content = StringBuilder()
        content.append(doc.getText(0, doc.length))
        content.replace(offset, offset + length, text)
        if (content.toString().toIntOrNull() != null) {
            super.replace(fb, offset, length, text, attrs)
        }
    }

    override fun remove(fb: FilterBypass?, offset: Int, length: Int) {
        val doc = fb?.document ?: return
        val content = StringBuilder()
        content.append(doc.getText(0, doc.length))
        content.delete(offset, offset + length)
        if (content.toString().isNumericOrEmpty()) {
            super.remove(fb, offset, length)
        }
    }

    private fun String.isNumericOrEmpty(): Boolean {
        if (this.trim() == "") return true
        if (this.toIntOrNull() == null) return false
        return true
    }
}
