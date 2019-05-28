package nl.rubensten.texifyidea.settings.labeldefiningcommands

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.PlainDocument

/**
 * Window to enter the information about a label defining command.
 */
class EditLabelDefiningCommand(
        cmdName: String,
        position: Int,
        labelAnyPrevCommandStatus: Boolean
) : DialogWrapper(null, false) {
    private val commandName = JTextField(cmdName, 25)
    private val commandPosition = JTextField("$position", 5)
    private val labelAnyPrevCommand = JCheckBox(
            "Labels a previous command like \\section",
            labelAnyPrevCommandStatus
    )

    init {
        title = "Define command with label"
        (commandName.document as PlainDocument).documentFilter = InputCommandFilter()
        (commandPosition.document as PlainDocument).documentFilter = InputPossiblePositionFilter()
        super.init()
    }

    override fun createCenterPanel(): JComponent? = null

    /**
     * Create the layout of the window.
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
     * Validate the input so it is not empty and the Int value is in possible range.
     */
    override fun doValidateAll(): MutableList<ValidationInfo> {
        val list = mutableListOf<ValidationInfo>()
        if (commandName.text.trim().isEmpty()) {
            list.add(ValidationInfo("Name can not be empty"))
        }
        if (commandPosition.text.trim().toIntOrNull() == null) {
            list.add(ValidationInfo("Position must be convertible to Int"))
        }
        return list
    }

    fun getCommandName(): String {
        return commandName.text
    }

    fun getCommandPosition(): Int {
        return commandPosition.text.toInt()
    }

    fun getLabelAnyPrevCommand(): Boolean {
        return labelAnyPrevCommand.isSelected
    }
}
