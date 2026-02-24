package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.TagButton
import com.intellij.execution.ui.FragmentedSettings
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.ide.dnd.DnDAction
import com.intellij.ide.dnd.DnDDragStartBean
import com.intellij.ide.dnd.DnDEvent
import com.intellij.ide.dnd.DnDManager
import com.intellij.ide.dnd.DnDSource
import com.intellij.ide.dnd.DnDTarget
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
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.defaultStepFor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JLayeredPane
import javax.swing.JPanel

internal class LatexCompileSequenceComponent(parentDisposable: Disposable) :
    JPanel(),
    DnDTarget,
    Disposable {

    private val dropFirst = JLabel(AllIcons.General.DropPlace).apply {
        border = JBUI.Borders.empty()
        isVisible = false
    }

    private val addButton: InplaceButton = InplaceButton("Add step", AllIcons.General.Add) {
        showTypeSelectionPopup(addButton, ::addStep)
    }

    private val addPanel = JPanel().apply {
        border = JBUI.Borders.emptyRight(5)
        add(addButton)
    }

    private val addLabel = LinkLabel<Any>("Add step", null) { source, _ ->
        showTypeSelectionPopup(source, ::addStep)
    }.apply {
        border = JBUI.Borders.emptyRight(5)
    }

    private val stepButtons = mutableListOf<StepButton>()

    private lateinit var runConfiguration: LatexRunConfiguration

    var changeListener: () -> Unit = {}
    var onSelectionChanged: (index: Int, stepId: String?, type: String?) -> Unit = { _, _, _ -> }
    var onStepsChanged: (steps: List<LatexStepRunConfigurationOptions>) -> Unit = {}

    private var selectedIndex: Int = -1

    init {
        Disposer.register(parentDisposable, this)
        layout = WrapLayout(FlowLayout.LEFT, 0, 0)
        border = JBUI.Borders.empty(4, 0)
        add(
            JPanel(FlowLayout(FlowLayout.CENTER, 0, 0)).apply {
                add(dropFirst)
                preferredSize = dropFirst.preferredSize
            }
        )
        DnDManager.getInstance().registerTarget(this, this, this)
    }

    private fun buildPanel() {
        remove(addPanel)
        remove(addLabel)
        stepButtons.forEach { add(it) }
        add(addPanel)
        add(addLabel)
        addLabel.isVisible = stepButtons.none { it.isVisible }
        revalidate()
        repaint()
        notifyStepsChanged()
    }

    private fun addStep(type: String) {
        val newStep = defaultStepFor(type) ?: return
        stepButtons.add(StepButton(newStep))
        buildPanel()
        selectStep(stepButtons.lastIndex)
        changeListener()
    }

    private fun showTypeSelectionPopup(anchor: JComponent, onSelected: (String) -> Unit) {
        val group = DefaultActionGroup()

        for (type in LatexStepUiSupport.availableStepTypes()) {
            group.add(AddStepAction(type, onSelected))
        }

        JBPopupFactory.getInstance().createActionGroupPopup(
            "Add New Step",
            group,
            DataManager.getInstance().getDataContext(anchor),
            false,
            false,
            false,
            null,
            -1,
            Conditions.alwaysTrue()
        ).showUnderneathOf(anchor)
    }

    fun resetEditorFrom(configuration: LatexRunConfiguration) {
        this.runConfiguration = configuration
        stepButtons.forEach { remove(it) }
        stepButtons.clear()
        selectedIndex = -1

        configuration.configOptions.ensureDefaultSteps()
        configuration.configOptions.steps.forEach { step -> stepButtons.add(StepButton(step.deepCopy())) }

        buildPanel()
        normalizeSelection()
    }

    fun applyEditorTo(configuration: LatexRunConfiguration) {
        stepButtons.removeAll { !it.isVisible }
        configuration.configOptions.steps = stepButtons
            .map { it.stepConfig.deepCopy() }
            .toMutableList()
        configuration.configOptions.ensureDefaultSteps()
    }

    override fun update(event: DnDEvent): Boolean {
        val buttonToReplace = findButtonToReplace(event)
        if (buttonToReplace == null) {
            stepButtons.forEach { it.showDropPlace(false) }
            dropFirst.isVisible = false
            event.isDropPossible = false
            return true
        }

        val dropButton = findDropButton(buttonToReplace, event)
        stepButtons.forEach { it.showDropPlace(it == dropButton) }
        dropFirst.isVisible = dropButton == null
        event.isDropPossible = true

        return false
    }

    override fun drop(event: DnDEvent) {
        val (index, _) = findButtonToReplace(event) ?: return
        val droppedButton = event.attachedObject as? StepButton ?: return

        stepButtons.remove(droppedButton)
        stepButtons.add(index, droppedButton)
        buildPanel()
        selectStep(stepButtons.indexOf(droppedButton))
        changeListener()
        IdeFocusManager.getInstance(runConfiguration.project).requestFocus(droppedButton, false)
    }

    override fun cleanUpOnLeave() {
        stepButtons.forEach { it.showDropPlace(false) }
        dropFirst.isVisible = false
    }

    private fun findButtonToReplace(event: DnDEvent): IndexedValue<StepButton>? {
        if (event.attachedObject !is StepButton) {
            return null
        }

        val area = Rectangle(event.point.x - 5, event.point.y - 5, 10, 10)
        val indexedSteps = stepButtons.withIndex()
        val intersected = indexedSteps.find { (_, button) ->
            button.isVisible && button.bounds.intersects(area)
        } ?: return null

        val (index, intersectedButton) = intersected
        if (intersectedButton == event.attachedObject) {
            return null
        }

        val leftHalf = intersectedButton.bounds.centerX > event.point.x
        val replaceTarget = if (index < stepButtons.indexOf(event.attachedObject)) {
            if (!leftHalf) {
                indexedSteps.find { (i, button) -> button.isVisible && i > index }
            }
            else {
                intersected
            }
        }
        else if (leftHalf) {
            indexedSteps.findLast { (i, button) -> button.isVisible && i < index }
        }
        else {
            intersected
        }

        return if (replaceTarget?.value == event.attachedObject) null else replaceTarget
    }

    private fun findDropButton(replaceButton: IndexedValue<StepButton>, event: DnDEvent): StepButton? = if (replaceButton.index > stepButtons.indexOf(event.attachedObject)) {
        replaceButton.value
    }
    else {
        stepButtons.withIndex()
            .findLast { (i, button) -> button.isVisible && i < replaceButton.index }
            ?.value
    }

    override fun dispose() {
    }

    internal fun selectedStepIndex(): Int = selectedIndex

    internal fun selectedStepId(): String? = stepButtons.getOrNull(selectedIndex)
        ?.takeIf { it.isVisible }
        ?.stepConfig
        ?.id

    internal fun selectedStepType(): String? = stepButtons.getOrNull(selectedIndex)
        ?.takeIf { it.isVisible }
        ?.stepConfig
        ?.type

    internal fun selectStep(index: Int) {
        if (index !in stepButtons.indices || !stepButtons[index].isVisible) {
            normalizeSelection()
            return
        }

        selectedIndex = index
        refreshSelectionUi()
        val selected = stepButtons[selectedIndex].stepConfig
        onSelectionChanged(selectedIndex, selected.id, selected.type)
    }

    internal fun removeStepAt(index: Int) {
        if (index !in stepButtons.indices) {
            return
        }
        val removed = stepButtons.removeAt(index)
        remove(removed)
        buildPanel()
        normalizeSelection()
        changeListener()
    }

    internal fun moveStep(from: Int, to: Int) {
        if (from !in stepButtons.indices || to !in stepButtons.indices || from == to) {
            return
        }
        val step = stepButtons.removeAt(from)
        stepButtons.add(to, step)
        buildPanel()
        selectStep(stepButtons.indexOf(step))
        changeListener()
    }

    private fun normalizeSelection() {
        if (stepButtons.isEmpty()) {
            selectedIndex = -1
            refreshSelectionUi()
            onSelectionChanged(-1, null, null)
            return
        }

        val currentlyVisible = stepButtons.getOrNull(selectedIndex)?.isVisible == true
        if (currentlyVisible) {
            refreshSelectionUi()
            val selected = stepButtons[selectedIndex].stepConfig
            onSelectionChanged(selectedIndex, selected.id, selected.type)
            return
        }

        selectedIndex = -1
        refreshSelectionUi()
        onSelectionChanged(-1, null, null)
    }

    private fun refreshSelectionUi() {
        stepButtons.forEachIndexed { index, button ->
            button.setSelectedState(index == selectedIndex && button.isVisible)
        }
    }

    private fun notifyStepsChanged() {
        onStepsChanged(stepButtons.filter { it.isVisible }.map { it.stepConfig.deepCopy() })
    }

    private inner class StepButton(stepConfig: LatexStepRunConfigurationOptions) : TagButton("", { changeListener() }), DnDSource {

        var stepConfig: LatexStepRunConfigurationOptions = stepConfig
            private set

        private var dropPlace: JLabel? = null

        init {
            Disposer.register(this@LatexCompileSequenceComponent, this)
            dropPlace = JLabel(AllIcons.General.DropPlace)
            add(dropPlace)
            setLayer(dropPlace, JLayeredPane.DRAG_LAYER)
            dropPlace?.isVisible = false
            border = JBUI.Borders.empty(1)

            updateFromStepType()

            myButton.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    selectStep(stepButtons.indexOf(this@StepButton))
                }

                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        showTypeSelectionPopup(myButton) { selectedType ->
                            val replacement = defaultStepFor(selectedType) ?: return@showTypeSelectionPopup
                            this@StepButton.stepConfig = replacement.copyWithIdentity(
                                stepId = stepConfig.id,
                                enabled = stepConfig.enabled,
                                selectedOptions = stepConfig.selectedOptions,
                            )
                            updateFromStepType()
                            if (selectedIndex == stepButtons.indexOf(this@StepButton)) {
                                onSelectionChanged(selectedIndex, stepConfig.id, stepConfig.type)
                            }
                            notifyStepsChanged()
                            changeListener()
                        }
                    }
                }
            })

            addPropertyChangeListener("visible") {
                if (!isVisible && this@StepButton in stepButtons) {
                    val removedSelected = stepButtons.indexOf(this@StepButton) == selectedIndex
                    stepButtons.remove(this@StepButton)
                    buildPanel()
                    if (removedSelected) {
                        normalizeSelection()
                    }
                    else {
                        refreshSelectionUi()
                    }
                    changeListener()
                }
            }

            DnDManager.getInstance().registerSource(this, myButton, this)
            layoutButtons()
        }

        fun setSelectedState(selected: Boolean) {
            if (selected) {
                myButton.border = JBUI.Borders.customLine(com.intellij.ui.JBColor.namedColor("Component.focusColor", com.intellij.ui.JBColor.BLUE), 2)
            }
            else {
                myButton.border = JBUI.Borders.empty(1)
            }
        }

        private fun updateFromStepType() {
            updateButton(LatexStepUiSupport.description(stepConfig.type), LatexStepUiSupport.icon(stepConfig.type))
            myButton.toolTipText = "Double-click to change step type. Drag and drop to reorder."
        }

        override fun layoutButtons() {
            super.layoutButtons()

            val indicator = dropPlace ?: return
            val bounds = myButton.bounds
            val size = indicator.preferredSize
            val gap = JBUI.scale(2)
            preferredSize = Dimension(bounds.width + size.width + 2 * gap, bounds.height)
            indicator.setBounds(
                (bounds.maxX + gap).toInt(),
                bounds.y + (bounds.height - size.height) / 2,
                size.width,
                size.height
            )
        }

        override fun canStartDragging(action: DnDAction?, dragOrigin: Point): Boolean = true

        override fun startDragging(action: DnDAction?, dragOrigin: Point): DnDDragStartBean = DnDDragStartBean(this)

        fun showDropPlace(show: Boolean) {
            dropPlace?.isVisible = show
        }

        fun getButton(): JButton = myButton
    }

    private fun LatexStepRunConfigurationOptions.copyWithIdentity(
        stepId: String,
        enabled: Boolean,
        selectedOptions: MutableList<FragmentedSettings.Option>,
    ): LatexStepRunConfigurationOptions = deepCopy().also {
        it.id = stepId
        it.enabled = enabled
        it.selectedOptions = selectedOptions
            .map { option -> FragmentedSettings.Option(option.name ?: "", option.visible) }
            .toMutableList()
    }

    private class AddStepAction(
        private val type: String,
        private val onSelected: (String) -> Unit,
    ) : AnAction(LatexStepUiSupport.description(type), null, LatexStepUiSupport.icon(type)) {

        override fun actionPerformed(e: AnActionEvent) {
            onSelected(type)
        }
    }
}
