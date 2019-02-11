package nl.rubensten.texifyidea.settings

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.UIUtil
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.text.PlainDocument

class TexifyDefineLabelingCommand(cmdName: String, position: Int) : DialogWrapper(null, false) {
    private val myCommandName : JTextField = JTextField(cmdName, 30)
    private val myCommandPosition : JTextField = JTextField("$position", 5)

    init {
        title = "Define command with label"
        (myCommandPosition.document as PlainDocument).documentFilter = MyIntFilter()
        super.init()
    }

    override fun createCenterPanel(): JComponent? = null

    override fun createNorthPanel(): JComponent? {
        val remoteComponent = JPanel(GridBagLayout())
        val gridBag = GridBag().setDefaultAnchor(GridBagConstraints.LINE_START)
                .setDefaultInsets(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP, 0, 0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
        remoteComponent.add(JBLabel("Name: ", SwingConstants.RIGHT), gridBag.nextLine().next().weightx(0.0))
        remoteComponent.add(myCommandName, gridBag.next().weightx(1.0))
        remoteComponent.add(JBLabel("Position: ", SwingConstants.RIGHT), gridBag.nextLine().next().weightx(0.0))
        remoteComponent.add(myCommandPosition, gridBag.next().weightx(1.0))
        return remoteComponent
    }

    override fun doValidateAll(): MutableList<ValidationInfo> {
        val list = mutableListOf<ValidationInfo>()
        if (myCommandName.text.trim().isEmpty()) {
            list.add(ValidationInfo("Name can not be empty"))
        }
        if (!myCommandPosition.text.trim().isConvertibleToInt()) {
            list.add(ValidationInfo("Position must be convertible to Int"))
        }
        return list
    }

    fun getMyCommandName() : String {
        return myCommandName.text
    }

    fun getMyCommandPosition() : Int {
        return myCommandPosition.text.toInt()
    }
}
