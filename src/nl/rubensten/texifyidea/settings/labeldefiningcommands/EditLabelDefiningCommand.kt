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
class EditLabelDefiningCommand(cmdName: String, position: Int, labelAnyPrevCommandStatus: Boolean) :
        DialogWrapper(null, false) {
    private val commandName: JTextField = JTextField(cmdName, 25)
    private val commandPosition: JTextField = JTextField("$position", 5)
    private val labelAnyPrevCommand: JCheckBox = JCheckBox("Labels a previous command like \\section",
            labelAnyPrevCommandStatus)

    init {
        title = "Define command with label"
        (commandName.document as PlainDocument).documentFilter = InputCommandNoSpaceFilter()
        (commandPosition.document as PlainDocument).documentFilter = InputPossiblePositionFilter()
        super.init()
    }

    override fun createCenterPanel(): JComponent? = null

    /**
     * create the layout of the window
     */
    override fun createNorthPanel(): JComponent? {
        val remoteComponent = JPanel(GridBagLayout())
        val gridBag = GridBagConstraints()
        gridBag.fill = GridBagConstraints.HORIZONTAL
        gridBag.gridx = 0
        gridBag.gridy = 0
        remoteComponent.add(JBLabel("Name: ", SwingConstants.RIGHT), gridBag)
        gridBag.gridx = 1
        remoteComponent.add(commandName, gridBag)
        gridBag.gridx = 0
        gridBag.gridy = 1
        remoteComponent.add(JBLabel("Position: ", SwingConstants.RIGHT), gridBag)
        gridBag.gridx = 1
        remoteComponent.add(commandPosition, gridBag)
        gridBag.gridx = 1
        gridBag.gridy = 2
        val label = JLabel("Only required parameters count for the position", SwingConstants.LEFT)
        label.foreground = Color.GRAY
        remoteComponent.add(label, gridBag)

        gridBag.gridx = 1
        gridBag.gridy = 3
        gridBag.gridwidth = 2
        remoteComponent.add(labelAnyPrevCommand, gridBag)

        peer.window.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                commandName.requestFocus()
            }
        })

        return remoteComponent
    }

    /**
     * validate the input so it is not empty and the Int value is in possible range
     */
    override fun doValidateAll(): MutableList<ValidationInfo> {
        val list = mutableListOf<ValidationInfo>()
        if (commandName.text.trim().isEmpty()) {
            list.add(ValidationInfo("Name can not be empty"))
        }
        if (!commandPosition.text.trim().possiblePosition()) {
            list.add(ValidationInfo("Position must be convertible to Int"))
        }
        return list
    }

    /**
     * return the name of the command
     */
    fun getCommandName(): String {
        return commandName.text
    }

    /**
     * return the position of the label
     */
    fun getCommandPosition(): Int {
        return commandPosition.text.toInt()
    }

    /**
     * return whether the command labels any previous command or not
     */
    fun getLabelAnyPrevCommand(): Boolean {
        return labelAnyPrevCommand.isSelected ?: false
    }
}
