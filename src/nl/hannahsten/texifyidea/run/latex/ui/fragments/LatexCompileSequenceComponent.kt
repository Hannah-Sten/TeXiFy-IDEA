package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.TagButton
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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.InplaceButton
import com.intellij.ui.JBColor
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.WrapLayout
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.defaultStepFor
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import kotlin.math.max
import kotlin.math.min
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class LatexCompileSequenceComponent(
    private val editor: LatexSettingsEditor,
    private val project: Project,
) :
    JPanel(),
    DnDTarget,
    Disposable {

    private val dropFirst = JLabel(AllIcons.General.DropPlace).apply {
        border = JBUI.Borders.empty()
        isVisible = false
    }

    private val addButton: InplaceButton = InplaceButton(TexifyBundle.message("run.step.ui.compile.sequence.add.step"), AllIcons.General.Add) {
        showTypeSelectionPopup(addButton, ::addStep)
    }

    private val autoConfigureLabel = LinkLabel<Any>(TexifyBundle.message("run.step.ui.compile.sequence.auto.configure"), null) { _, _ ->
        autoConfigureSteps()
    }.apply {
        border = JBUI.Borders.emptyRight(5)
        toolTipText = TexifyBundle.message("run.step.ui.compile.sequence.auto.configure.tooltip")
    }

    private val addPanel = JPanel().apply {
        border = JBUI.Borders.emptyRight(5)
        add(addButton)
    }

    private val addLabel = LinkLabel<Any>(TexifyBundle.message("run.step.ui.compile.sequence.add.step"), null) { source, _ ->
        showTypeSelectionPopup(source, ::addStep)
    }.apply {
        border = JBUI.Borders.emptyRight(5)
    }

    private val stepButtons = mutableListOf<StepButton>()
    private val shadowSteps: MutableList<LatexStepRunConfigurationOptions>
        get() = editor.shadowSteps

    private val selectedStepIds = linkedSetOf<String>()
    private var primaryStepId: String? = null
    private var anchorStepId: String? = null

    var changeListener: () -> Unit = {}

    init {
        Disposer.register(editor, this)
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

    internal fun headerActionComponent(): JComponent = autoConfigureLabel

    internal fun selectionState(): LatexStepSelectionState {
        val orderedSelection = orderedSelectedButtons().map { it.stepConfig.id }
        val resolvedPrimary = primaryStepId
            ?.takeIf { it in orderedSelection }
            ?: selectedStepIds.firstOrNull()
        return LatexStepSelectionState(
            selectedStepIds = orderedSelection,
            primaryStepId = resolvedPrimary,
        )
    }

    private fun buildPanel() {
        remove(addPanel)
        remove(addLabel)
        stepButtons.forEach { add(it) }
        add(addPanel)
        add(addLabel)
        addLabel.isVisible = stepButtons.none { it.isVisible }
        refreshSelectionUi()
        revalidate()
        repaint()
    }

    private fun addStep(type: String) {
        editor.beforeSequenceStructureChange()
        val newStep = defaultStepFor(type) ?: return
        shadowSteps.add(newStep)
        stepButtons.add(StepButton(newStep))
        buildPanel()
        setSingleSelection(stepButtons.last(), notify = true)
        notifyStepsChanged()
        changeListener()
    }

    private fun showTypeSelectionPopup(anchor: JComponent, onSelected: (String) -> Unit) {
        val group = DefaultActionGroup()

        for (type in LatexStepUiSupport.availableStepTypes()) {
            group.add(AddStepAction(type, onSelected))
        }

        JBPopupFactory.getInstance().createActionGroupPopup(
            TexifyBundle.message("run.step.ui.compile.sequence.add.new.step"),
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

    fun resetEditorFrom() {
        stepButtons.forEach { remove(it) }
        stepButtons.clear()
        clearSelection(notify = false)

        shadowSteps.forEach { step -> stepButtons.add(StepButton(step)) }

        buildPanel()
        normalizeSelection(notify = true)
    }

    fun applyEditorTo() {
        stepButtons.removeAll { !it.isVisible }
        synchronizeShadowStepsFromButtons()
    }

    override fun update(event: DnDEvent): Boolean {
        val droppedButton = event.attachedObject as? StepButton ?: return true
        val movingButtons = movingButtonsForDrag(droppedButton)
        val insertionIndex = findInsertionIndex(event, movingButtons) ?: run {
            stepButtons.forEach { it.showDropPlace(false) }
            dropFirst.isVisible = false
            event.isDropPossible = false
            return true
        }

        val dropButton = findDropButton(insertionIndex, movingButtons)
        stepButtons.forEach { it.showDropPlace(it == dropButton) }
        dropFirst.isVisible = dropButton == null
        event.isDropPossible = true

        return false
    }

    override fun drop(event: DnDEvent) {
        val droppedButton = event.attachedObject as? StepButton ?: return
        if (droppedButton !in stepButtons) {
            return
        }

        if (droppedButton.stepConfig.id !in selectedStepIds) {
            setSingleSelection(droppedButton, notify = true)
        }

        val movingButtons = movingButtonsForDrag(droppedButton)
        val insertionIndex = findInsertionIndex(event, movingButtons) ?: return

        editor.beforeSequenceStructureChange()
        moveButtonsBeforeIndex(movingButtons, insertionIndex)
        synchronizeShadowStepsFromButtons()
        buildPanel()
        normalizeSelection(notify = true)
        notifyStepsChanged()
        changeListener()
        IdeFocusManager.getInstance(project).requestFocus(droppedButton, false)
    }

    override fun cleanUpOnLeave() {
        stepButtons.forEach { it.showDropPlace(false) }
        dropFirst.isVisible = false
    }

    override fun dispose() {
    }

    internal fun selectedStepIndex(): Int = stepButtons.indexOfFirst {
        it.isVisible && it.stepConfig.id == primaryStepId
    }

    internal fun selectedStepType(): String? = stepButtons
        .firstOrNull { it.isVisible && it.stepConfig.id == primaryStepId }
        ?.stepConfig
        ?.type

    internal fun selectStep(index: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible }
        if (button == null) {
            normalizeSelection(notify = true)
            return
        }
        setSingleSelection(button, notify = true)
    }

    internal fun toggleStepSelection(index: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible } ?: return
        toggleSelection(button, notify = true)
    }

    internal fun selectStepRange(index: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible } ?: return
        selectRange(button, notify = true)
    }

    internal fun moveStep(from: Int, to: Int) {
        val button = stepButtons.getOrNull(from)?.takeIf { it.isVisible } ?: return
        if (button.stepConfig.id !in selectedStepIds) {
            setSingleSelection(button, notify = true)
        }
        moveSelectedStepsTo(to)
    }

    internal fun moveSelectedStepsTo(targetIndex: Int) {
        val movingButtons = orderedSelectedButtons()
        if (movingButtons.isEmpty()) {
            return
        }

        editor.beforeSequenceStructureChange()
        moveButtonsBeforeIndex(movingButtons, targetIndex.coerceIn(0, stepButtons.size))
        synchronizeShadowStepsFromButtons()
        buildPanel()
        normalizeSelection(notify = true)
        notifyStepsChanged()
        changeListener()
    }

    internal fun removeStepForTest(index: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible } ?: return
        editor.beforeSequenceStructureChange()
        removeStepButton(button)
        changeListener()
    }

    internal fun triggerAutoConfigureForTest() {
        autoConfigureSteps()
    }

    internal fun addStepForTest(type: String) {
        addStep(type)
    }

    internal fun currentStepTypesForTest(): List<String> = stepButtons.map { it.stepConfig.type }

    internal fun currentStepTitlesForTest(): List<String> = stepButtons
        .filter { it.isVisible }
        .map { it.labelTextForTest() }

    internal fun selectedStepIdsForTest(): List<String> = selectionState().selectedStepIds

    internal fun primaryStepIdForTest(): String? = selectionState().primaryStepId

    internal fun pressStepForDragStartForTest(index: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible } ?: return
        handleSelection(
            button,
            MouseEvent(
                this,
                MouseEvent.MOUSE_PRESSED,
                0,
                InputEvent.BUTTON1_DOWN_MASK,
                0,
                0,
                1,
                false,
                MouseEvent.BUTTON1,
            )
        )
    }

    internal fun clickStepForTest(index: Int) {
        clickStepForTest(index, modifiers = 0)
    }

    internal fun ctrlClickStepForTest(index: Int) {
        clickStepForTest(index, modifiers = InputEvent.CTRL_DOWN_MASK)
    }

    fun refreshStepTitles() {
        stepButtons.forEach { it.updateFromStepConfig() }
        revalidate()
        repaint()
    }

    private fun handleSelection(button: StepButton, event: MouseEvent) {
        when {
            event.isShiftDown -> selectRange(button, notify = true)
            event.isMetaDown || event.isControlDown -> toggleSelection(button, notify = true)
            button.stepConfig.id in selectedStepIds && selectedStepIds.size > 1 -> {
                // Keep the existing batch selected while drag-and-drop is deciding whether this press becomes a drag.
            }
            else -> setSingleSelection(button, notify = true)
        }
    }

    private fun finalizeSingleClickSelection(button: StepButton, event: MouseEvent) {
        if (
            event.isShiftDown ||
            event.isMetaDown ||
            event.isControlDown ||
            button.stepConfig.id !in selectedStepIds ||
            selectedStepIds.size <= 1
        ) {
            return
        }

        setSingleSelection(button, notify = true)
    }

    private fun setSingleSelection(button: StepButton?, notify: Boolean) {
        if (button == null || !button.isVisible) {
            clearSelection(notify)
            return
        }

        selectedStepIds.clear()
        selectedStepIds.add(button.stepConfig.id)
        primaryStepId = button.stepConfig.id
        anchorStepId = button.stepConfig.id
        refreshSelectionUi()
        if (notify) {
            notifySelectionChanged()
        }
    }

    private fun toggleSelection(button: StepButton, notify: Boolean) {
        val stepId = button.stepConfig.id
        if (!selectedStepIds.add(stepId)) {
            selectedStepIds.remove(stepId)
            if (selectedStepIds.isEmpty()) {
                primaryStepId = null
                anchorStepId = null
            }
            else {
                if (primaryStepId == stepId) {
                    primaryStepId = selectedStepIds.firstOrNull()
                    anchorStepId = primaryStepId
                }
                else if (anchorStepId == stepId) {
                    anchorStepId = primaryStepId
                }
            }
        }
        else {
            if (primaryStepId == null) {
                primaryStepId = stepId
                anchorStepId = stepId
            }
        }

        refreshSelectionUi()
        if (notify) {
            notifySelectionChanged()
        }
    }

    private fun selectRange(button: StepButton, notify: Boolean) {
        val anchorIndex = visibleStepIds().indexOf(anchorStepId)
        val targetIndex = visibleStepIds().indexOf(button.stepConfig.id)
        if (anchorIndex < 0 || targetIndex < 0) {
            setSingleSelection(button, notify)
            return
        }

        selectedStepIds.clear()
        val rangeStart = min(anchorIndex, targetIndex)
        val rangeEnd = max(anchorIndex, targetIndex)
        visibleStepIds()
            .subList(rangeStart, rangeEnd + 1)
            .forEach(selectedStepIds::add)
        primaryStepId = primaryStepId
            ?.takeIf { it in selectedStepIds }
            ?: anchorStepId?.takeIf { it in selectedStepIds }
            ?: selectedStepIds.firstOrNull()
        refreshSelectionUi()
        if (notify) {
            notifySelectionChanged()
        }
    }

    private fun clearSelection(notify: Boolean) {
        selectedStepIds.clear()
        primaryStepId = null
        anchorStepId = null
        refreshSelectionUi()
        if (notify) {
            notifySelectionChanged()
        }
    }

    private fun normalizeSelection(notify: Boolean) {
        val visibleIds = visibleStepIds()
        selectedStepIds.retainAll(visibleIds.toSet())

        if (selectedStepIds.isEmpty()) {
            primaryStepId = null
            anchorStepId = null
            refreshSelectionUi()
            if (notify) {
                notifySelectionChanged()
            }
            return
        }

        if (primaryStepId !in selectedStepIds) {
            primaryStepId = selectedStepIds.firstOrNull()
            anchorStepId = primaryStepId
        }
        else if (anchorStepId !in selectedStepIds) {
            anchorStepId = primaryStepId
        }

        refreshSelectionUi()
        if (notify) {
            notifySelectionChanged()
        }
    }

    private fun refreshSelectionUi() {
        stepButtons.forEach { button ->
            val stepId = button.stepConfig.id
            button.setSelectedState(
                primary = stepId == primaryStepId && button.isVisible,
                selected = stepId in selectedStepIds && button.isVisible,
            )
        }
    }

    private fun notifyStepsChanged() {
        editor.onCompileSequenceStepsChanged()
    }

    private fun notifySelectionChanged() {
        editor.onCompileSequenceSelectionChanged(selectionState())
    }

    private fun autoConfigureSteps() {
        editor.beforeSequenceStructureChange()
        val previousSelection = selectionState()
        val autoConfiguredSteps = editor.autoConfigureCurrentSteps()

        if (autoConfiguredSteps.isEmpty()) {
            return
        }

        stepButtons.forEach { remove(it) }
        stepButtons.clear()
        autoConfiguredSteps.forEach { configuredStep ->
            stepButtons.add(StepButton(configuredStep))
        }
        buildPanel()

        val availableIds = autoConfiguredSteps.map { it.id }.toSet()
        selectedStepIds.clear()
        previousSelection.selectedStepIds
            .filter { it in availableIds }
            .forEach(selectedStepIds::add)
        primaryStepId = previousSelection.primaryStepId
            ?.takeIf { it in selectedStepIds }
            ?: selectedStepIds.firstOrNull()
        anchorStepId = primaryStepId

        normalizeSelection(notify = true)
        notifyStepsChanged()
        changeListener()
    }

    private fun synchronizeShadowStepsFromButtons() {
        editor.shadowSteps.clear()
        editor.shadowSteps.addAll(stepButtons.filter { it.isVisible }.map { it.stepConfig })
    }

    private fun orderedSelectedButtons(): List<StepButton> = stepButtons.filter {
        it.isVisible && it.stepConfig.id in selectedStepIds
    }

    private fun clickStepForTest(index: Int, modifiers: Int) {
        val button = stepButtons.getOrNull(index)?.takeIf { it.isVisible } ?: return
        handleSelection(
            button,
            MouseEvent(
                this,
                MouseEvent.MOUSE_PRESSED,
                0,
                modifiers,
                0,
                0,
                1,
                false,
                MouseEvent.BUTTON1,
            )
        )
        finalizeSingleClickSelection(
            button,
            MouseEvent(
                this,
                MouseEvent.MOUSE_CLICKED,
                0,
                modifiers,
                0,
                0,
                1,
                false,
                MouseEvent.BUTTON1,
            )
        )
    }

    private fun visibleStepIds(): List<String> = stepButtons
        .filter { it.isVisible }
        .map { it.stepConfig.id }

    private fun movingButtonsForDrag(droppedButton: StepButton): List<StepButton> = if (droppedButton.stepConfig.id in selectedStepIds) {
        orderedSelectedButtons()
    }
    else {
        listOf(droppedButton)
    }

    private fun findInsertionIndex(event: DnDEvent, movingButtons: Collection<StepButton>): Int? {
        if (event.attachedObject !is StepButton) {
            return null
        }

        val area = Rectangle(event.point.x - 5, event.point.y - 5, 10, 10)
        val movingSet = movingButtons.toSet()
        val visibleTargets = stepButtons.withIndex().filter { (_, button) ->
            button.isVisible && button !in movingSet
        }
        val intersected = visibleTargets.find { (_, button) ->
            button.bounds.intersects(area)
        } ?: return null

        val (index, intersectedButton) = intersected
        val insertBeforeTarget = intersectedButton.bounds.centerX > event.point.x
        if (insertBeforeTarget) {
            return index
        }

        return visibleTargets.firstOrNull { (targetIndex, _) -> targetIndex > index }?.index ?: stepButtons.size
    }

    private fun findDropButton(
        insertionIndex: Int,
        movingButtons: Collection<StepButton>,
    ): StepButton? {
        val movingSet = movingButtons.toSet()
        return stepButtons.withIndex()
            .findLast { (index, button) ->
                button.isVisible &&
                    button !in movingSet &&
                    index < insertionIndex
            }
            ?.value
    }

    private fun moveButtonsBeforeIndex(movingButtons: List<StepButton>, rawIndex: Int) {
        if (movingButtons.isEmpty()) {
            return
        }

        val movingSet = movingButtons.toSet()
        val insertionIndex = (
            rawIndex - stepButtons.withIndex().count { (index, button) ->
                index < rawIndex && button in movingSet
            }
            ).coerceIn(0, stepButtons.size - movingButtons.size)

        stepButtons.removeAll(movingSet)
        stepButtons.addAll(insertionIndex, movingButtons)
    }

    private fun removeStepButton(button: StepButton) {
        selectedStepIds.remove(button.stepConfig.id)
        stepButtons.remove(button)
        shadowSteps.remove(button.stepConfig)
        remove(button)
        if (primaryStepId == button.stepConfig.id) {
            primaryStepId = selectedStepIds.firstOrNull()
            anchorStepId = primaryStepId
        }
        else if (anchorStepId == button.stepConfig.id) {
            anchorStepId = primaryStepId
        }

        buildPanel()
        normalizeSelection(notify = true)
        notifyStepsChanged()
    }

    private inner class StepButton(stepConfig: LatexStepRunConfigurationOptions) : TagButton("", { changeListener() }), DnDSource {

        var stepConfig: LatexStepRunConfigurationOptions = stepConfig
            private set

        private var dropPlace: JLabel? = null

        init {
            Disposer.register(this@LatexCompileSequenceComponent, this)
            dropPlace = JLabel(AllIcons.General.DropPlace)
            add(dropPlace, DRAG_LAYER)
            dropPlace?.isVisible = false
            border = JBUI.Borders.empty(1)

            updateFromStepConfig()

            myButton.addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    handleSelection(this@StepButton, e)
                }

                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 1) {
                        finalizeSingleClickSelection(this@StepButton, e)
                    }
                    if (e.clickCount == 2) {
                        showTypeSelectionPopup(myButton) { selectedType ->
                            editor.beforeSequenceStructureChange()
                            val replacement = defaultStepFor(selectedType) ?: return@showTypeSelectionPopup
                            val buttonIndex = stepButtons.indexOf(this@StepButton)
                            this@StepButton.stepConfig = replacement.copyWithIdentity(
                                stepId = stepConfig.id,
                                selectedOptions = stepConfig.selectedOptions,
                            )
                            if (buttonIndex in shadowSteps.indices) {
                                shadowSteps[buttonIndex] = this@StepButton.stepConfig
                            }
                            updateFromStepConfig()
                            refreshSelectionUi()
                            notifySelectionChanged()
                            notifyStepsChanged()
                            changeListener()
                        }
                    }
                }
            })

            addPropertyChangeListener("visible") {
                if (!isVisible && this@StepButton in stepButtons) {
                    editor.beforeSequenceStructureChange()
                    removeStepButton(this@StepButton)
                    changeListener()
                }
            }

            DnDManager.getInstance().registerSource(this, myButton, this)
            layoutButtons()
        }

        fun setSelectedState(primary: Boolean, selected: Boolean) {
            myButton.border = when {
                primary -> JBUI.Borders.customLine(
                    JBColor.namedColor("Component.focusColor", JBColor.BLUE),
                    2
                )

                selected -> JBUI.Borders.customLine(
                    JBColor.namedColor("Component.focusColor", JBColor.BLUE),
                    1
                )

                else -> JBUI.Borders.empty(1)
            }
        }

        fun updateFromStepConfig() {
            updateButton(stepConfig.displayName(), LatexStepUiSupport.icon(stepConfig.type))
            myButton.toolTipText = TexifyBundle.message("run.step.ui.compile.sequence.step.tooltip")
        }

        fun labelTextForTest(): String = myButton.text ?: ""

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
