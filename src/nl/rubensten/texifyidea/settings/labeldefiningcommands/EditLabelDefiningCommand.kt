package nl.rubensten.texifyidea.settings.labeldefiningcommands

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.PlainDocument

/**
 * window to enter the information about a label defining command
 */
class EditLabelDefiningCommand(cmdName: String, position: Int) : DialogWrapper(null, false) {
    private val myCommandName : JTextField = JTextField(cmdName, 25)
    private val myCommandPosition : JTextField = JTextField("$position", 5)

    init {
        title = "Define command with label"
        (myCommandName.document as PlainDocument).documentFilter = InputCommandNoSpaceFilter()
        (myCommandPosition.document as PlainDocument).documentFilter = InputPossiblePositionFilter()
        super.init()
    }

    override fun createCenterPanel(): JComponent? = null

    /**
     * create the layout of the window
     */
    override fun createNorthPanel(): JComponent? {
        val remoteComponent = JPanel(GridBagLayout())
        val gridBag = GridBag().setDefaultAnchor(GridBagConstraints.LINE_START)
                .setDefaultInsets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, 0, 0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
        remoteComponent.add(JBLabel("Name: ", SwingConstants.RIGHT), gridBag.nextLine().next().weightx(0.0))
        remoteComponent.add(myCommandName, gridBag.next().weightx(1.0))
        remoteComponent.add(JBLabel("Position: ", SwingConstants.RIGHT), gridBag.nextLine().next().weightx(0.0))
        remoteComponent.add(myCommandPosition, gridBag.next().weightx(1.0))

        remoteComponent.add(JLabel("", SwingConstants.LEFT), gridBag.nextLine().next().weightx(0.0))
        val label = JLabel("Only required parameters count for the position", SwingConstants.LEFT)
        label.foreground = Color.GRAY
        remoteComponent.add(label, gridBag.next().weightx(1.0))

        peer.window.addWindowListener(object: WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                myCommandName.requestFocus()
            }
        })

        return remoteComponent
    }

    /**
     * validate the input so it is not empty and the Int value is in possible range
     */
    override fun doValidateAll(): MutableList<ValidationInfo> {
        val list = mutableListOf<ValidationInfo>()
        if (myCommandName.text.trim().isEmpty()) {
            list.add(ValidationInfo("Name can not be empty"))
        }
        if (!myCommandPosition.text.trim().possiblePosition()) {
            list.add(ValidationInfo("Position must be convertible to Int"))
        }
        return list
    }

    /**
     * return the name of the command
     */
    fun getMyCommandName() : String {
        return myCommandName.text
    }

    /**
     * return the position of the label
     */
    fun getMyCommandPosition() : Int {
        return myCommandPosition.text.toInt()
    }
}
