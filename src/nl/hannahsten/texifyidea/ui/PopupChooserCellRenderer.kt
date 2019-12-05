package nl.hannahsten.texifyidea.ui

import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder


/**
 * Custom cell renderer to be used for popup choosers.
 * Adds some indentation to the list cell.
 *
 * @author Abby Berkers
 */
class PopupChooserCellRenderer : ListCellRenderer<String> {
    override fun getListCellRendererComponent(list: JList<out String>?, value: String?, position: Int, isSelected: Boolean, hasFocus: Boolean): Component {
        val renderer = DefaultListCellRenderer().getListCellRendererComponent(list, value, position,
                isSelected, hasFocus) as JLabel
        renderer.border = EmptyBorder(1, 8, 1, 1)
        return renderer
    }
}