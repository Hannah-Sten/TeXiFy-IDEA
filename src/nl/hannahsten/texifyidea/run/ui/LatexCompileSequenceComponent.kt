package nl.hannahsten.texifyidea.run.ui

import com.intellij.execution.ui.FragmentedSettingsBuilder
import com.intellij.execution.ui.TagButton
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.ide.dnd.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.InplaceButton
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.step.Step
import nl.hannahsten.texifyidea.run.step.StepProvider
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * Tag editor for defining a LaTeX compile sequence.
 *
 * Heavily inspired by [com.intellij.execution.ui.BeforeRunComponent].
 *
 * @author Sten Wessel
 */
class LatexCompileSequenceComponent(parentDisposable: Disposable) :
    JPanel(WrapLayout(FlowLayout.LEADING, 0, JBUI.scale(FragmentedSettingsBuilder.TAG_VGAP))),
    DnDTarget,
    Disposable {

    private val dropFirst = JLabel(AllIcons.General.DropPlace).apply {
        border = JBUI.Borders.empty()
    }

    private val addButton = InplaceButton("Add step", AllIcons.General.Add) { showPopup() }

    private val addPanel = JPanel().apply {
        border = JBUI.Borders.emptyRight(5)
        add(addButton)
    }

    private val addLabel = LinkLabel<Any>("Add step", null) { _, _ -> showPopup() }.apply {
        border = JBUI.Borders.emptyRight(5)
    }

    private val steps = mutableListOf<StepButton>()

    var changeListener: () -> Unit = { }

    private lateinit var configuration: LatexRunConfiguration

    init {
        Disposer.register(parentDisposable, this)
        add(Box.createVerticalStrut(30))

        initDropPanel()

        DnDManager.getInstance().registerTarget(this, this, this)
    }

    private fun initDropPanel() {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0)).apply {
            add(dropFirst)
            preferredSize = dropFirst.preferredSize
        }
        add(panel)
        dropFirst.isVisible = false
    }

    private fun buildPanel() {
        remove(addPanel)
        remove(addLabel)
        steps.forEach { add(it) }
        add(addPanel)
        add(addLabel)
        addLabel.isVisible = steps.isEmpty()
    }

    private fun showPopup() {
        val group = DefaultActionGroup()

        for (provider in CompilerMagic.compileStepProviders.values) {
            group.add(TagAction(provider))
        }

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            "Add New Step",
            group,
            DataManager.getInstance().getDataContext(addButton),
            false, false, false, null, -1, Conditions.alwaysTrue()
        )

        popup.showUnderneathOf(addButton)
    }

    private fun createStep(provider: StepProvider, e: AnActionEvent) {
        val step = provider.createStep(configuration)
        val tag = StepButton(step)
        steps.add(tag)
        buildPanel()
        changeListener()
        step.configure(e.dataContext, tag)
        step.onConfigured(tag)
    }

    fun resetEditorFrom(c: LatexRunConfiguration) {
        configuration = c
        steps.forEach { remove(it) }
        steps.clear()

        configuration.compileSteps.forEach {
            val tag = StepButton(it)
            steps.add(tag)
        }

        buildPanel()
    }

    fun applyEditorTo(c: LatexRunConfiguration) {
        // Actually delete deleted steps (we cannot hook into the private remove() method of TagButton)
        steps.removeAll { !it.isVisible }

        c.compileSteps.apply {
            clear()
            addAll(steps.map { it.step })
        }
    }

    override fun update(event: DnDEvent): Boolean {
        val buttonToReplace = findButtonToReplace(event)
        if (buttonToReplace == null) {
            steps.forEach { it.showDropPlace(false) }
            event.isDropPossible = false
            return true
        }

        val dropButton = findDropButton(buttonToReplace, event)
        steps.forEach { it.showDropPlace(it == dropButton) }
        dropFirst.isVisible = (dropButton == null)
        event.isDropPossible = true

        return false
    }

    override fun drop(event: DnDEvent) {
        val (index, _) = findButtonToReplace(event) ?: return
        val droppedButton = event.attachedObject as? StepButton ?: return

        steps.remove(droppedButton)
        steps.add(index, droppedButton)
        buildPanel()
        changeListener()
        IdeFocusManager.getInstance(configuration.project).requestFocus(droppedButton, false)
    }

    private fun findButtonToReplace(event: DnDEvent): IndexedValue<StepButton>? {
        if (event.attachedObject !is StepButton) return null

        val area = Rectangle(event.point.x - 5, event.point.y - 5, 10, 10)
        val indexedSteps = steps.withIndex()

        val intersectedButtonWithIndex = indexedSteps.find { (_, it) -> it.isVisible && it.bounds.intersects(area) } ?: return null
        val (index, intersectedButton) = intersectedButtonWithIndex
        if (intersectedButton == event.attachedObject) {
            return null
        }

        val left = intersectedButton.bounds.centerX > event.point.x
        val buttonToReplace = if (index < steps.indexOf(event.attachedObject)) {
            if (!left) {
                indexedSteps.find { (i, it) -> it.isVisible && i > index }
            }
            else intersectedButtonWithIndex
        }
        else if (left) {
            indexedSteps.findLast { (i, it) -> it.isVisible && i < index }
        }
        else intersectedButtonWithIndex

        return if (buttonToReplace == event.attachedObject) null else buttonToReplace
    }

    private fun findDropButton(replaceButton: IndexedValue<StepButton>, event: DnDEvent): StepButton? = if (replaceButton.index > steps.indexOf(event.attachedObject)) {
        replaceButton.value
    }
    else {
        steps.withIndex().findLast { (i, it) -> it.isVisible && i < replaceButton.index }?.value
    }

    override fun dispose() {
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun cleanUpOnLeave() {
        steps.forEach { it.showDropPlace(false) }
        dropFirst.isVisible = false
    }

    inner class StepButton(val step: Step) : TagButton(step.getDescription(), { changeListener() }), DnDSource {

        private val dropPlace = JLabel(AllIcons.General.DropPlace)

        init {
            Disposer.register(this@LatexCompileSequenceComponent, this)
            add(dropPlace, JLayeredPane.DRAG_LAYER)
            dropPlace.isVisible = false

            myButton.icon = step.provider.icon

            myButton.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        step.configure(DataManager.getInstance().getDataContext(myButton), this@StepButton)
                        step.onConfigured(this@StepButton)
                    }
                }
            })

            DnDManager.getInstance().registerSource(this, myButton, this)
            myButton.toolTipText = "Double click to edit settings.<br>Drag'n'drop to reorder."
            layoutButtons()
        }

        override fun layoutButtons() {
            super.layoutButtons()
            @Suppress("SENSELESS_COMPARISON")
            if (dropPlace == null) return

            val bounds = myButton.bounds
            val size = dropPlace.preferredSize
            val gap = JBUI.scale(2)
            preferredSize = Dimension(bounds.width + size.width + 2 * gap, bounds.height)
            dropPlace.setBounds((bounds.maxX + gap).toInt(), bounds.y + (bounds.height - size.height) / 2, size.width, size.height)
        }

        override fun canStartDragging(action: DnDAction?, dragOrigin: Point) = true

        override fun startDragging(action: DnDAction?, dragOrigin: Point) = DnDDragStartBean(this)

        fun showDropPlace(show: Boolean) {
            dropPlace.isVisible = show
        }

        fun getButton(): JButton = myButton

        fun updateButton() {
            updateButton(step.getDescription(), step.getIcon())
        }
    }

    private inner class TagAction(private val provider: StepProvider) : AnAction(provider.name, null, provider.icon) {

        override fun actionPerformed(e: AnActionEvent) {
            createStep(provider, e)
        }
    }
}
