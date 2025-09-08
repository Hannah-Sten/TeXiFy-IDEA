package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewElement
import nl.hannahsten.texifyidea.structure.latex.SectionNumbering.DocumentClass
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommandsNoUpdate
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewElement(private val element: PsiElement) : StructureViewTreeElement, SortableTreeElement {

    object Cache {
        val includedFiles = mutableMapOf<SmartPsiElementPointer<LatexCommands>, List<SmartPsiElementPointer<PsiFile>>>()
    }

    // Get document class, this can take over one second but does not change frequently, and is only used for the correct sectioning levels, so cache it
    val docClass: String
        get() {
            return NewCommandsIndex.getByName(LatexGenericRegularCommand.DOCUMENTCLASS.commandWithSlash, element.containingFile).firstNotNullOfOrNull {
                it.requiredParameterText(0)
            } ?: "article"
        }

    override fun getValue() = element

    override fun canNavigate() = element is NavigationItem && (element as NavigationItem).canNavigate()

    override fun canNavigateToSource() = element is NavigationItem && (element as NavigationItem).canNavigateToSource()

    override fun navigate(requestFocus: Boolean) {
        if (element is NavigationItem) {
            (element as NavigationItem).navigate(requestFocus)
        }
    }

    override fun getAlphaSortKey(): String {
        return (element as? LatexCommands)?.commandToken?.text?.lowercase(Locale.getDefault())
            ?: if (element is PsiNameIdentifierOwner) {
                element.name!!.lowercase(Locale.getDefault())
            }
            else {
                element.text.lowercase(Locale.getDefault())
            }
    }

    override fun getPresentation(): ItemPresentation {
        if (element is LatexCommands) {
            return LatexPresentationFactory.getPresentation(element)
        }
        else if (element is PsiFile) {
            return LatexFilePresentation(element)
        }

        throw AssertionError("Should not happen! Element !is LatexCommands or PsiFile.")
    }

    override fun getChildren(): Array<TreeElement> {
        if (element !is LatexFile && element !is StyleFile && element !is ClassFile) {
            return emptyArray()
        }

        // Fetch all commands in the active file.
        val numbering = SectionNumbering(DocumentClass.getClassByName(docClass))
        val commands = element.traverseCommands()
        val treeElements = ArrayList<LatexStructureViewCommandElement>()

        val labelingCommands = getLabelDefinitionCommandsNoUpdate()

        // Add sectioning.
        val sections = mutableListOf<LatexStructureViewCommandElement>()
        for (command in commands) {
            // Update counter.
            if (command.name == LatexGenericRegularCommand.ADDTOCOUNTER.cmd || command.name == LatexGenericRegularCommand.SETCOUNTER.cmd) {
                updateNumbering(command, numbering)
                continue
            }

            val newElement = LatexStructureViewCommandElement.newCommand(command) ?: continue

            when (command.name) {
                in CommandMagic.sectionNameToLevel -> {
                    addSections(command, sections, treeElements, numbering)
                }

                in labelingCommands + CommandMagic.commandDefinitionsAndRedefinitions + setOf(LatexGenericRegularCommand.BIBITEM.cmd) -> {
                    addAtCurrentSectionLevel(sections, treeElements, newElement)
                }

                else -> {
                    var includedFiles = InputFileReference.getIncludedFiles(command)
                    if (!TexifySettings.getState().showPackagesInStructureView) {
                        includedFiles = includedFiles.filter {
                            it.virtualFile?.fileType == LatexFileType || it.virtualFile?.fileType == BibtexFileType
                        }
                    }
                    if (includedFiles.isNotEmpty()) {
                        for (psiFile in includedFiles) {
                            when (psiFile.virtualFile?.fileType) {
                                LatexFileType ->
                                    newElement.addChild(LatexStructureViewElement(psiFile))
                                BibtexFileType ->
                                    newElement.addChild(BibtexStructureViewElement(psiFile))

                                StyleFileType, ClassFileType -> {
                                    val inProject = runCatching { ProjectFileIndex.getInstance(element.project).isInProject(psiFile.virtualFile) }
                                        .getOrDefault(false)
                                    if (inProject) // let us do not show the style/class files that are not in the project, or the view will be cluttered
                                        newElement.addChild(LatexStructureViewElement(psiFile))
                                }
                            }
                        }
                        newElement.isFileInclude = true
                        addAtCurrentSectionLevel(sections, treeElements, newElement)
                    }
                }
            }
        }

        return treeElements.sortedBy { it.value.textOffset }.toTypedArray()
    }

    /**
     * Add to top level or at the current sectioning level, so that all entries in the structure view are in the same order as they are in the source
     */
    private fun addAtCurrentSectionLevel(
        sections: MutableList<LatexStructureViewCommandElement>,
        treeElements: ArrayList<LatexStructureViewCommandElement>,
        newElement: LatexStructureViewCommandElement
    ) {
        if (sections.isNotEmpty()) {
            sections.last().addChild(newElement)
        }
        else {
            treeElements.add(newElement)
        }
    }

    private fun LatexStructureViewElement.addSections(
        command: LatexCommands,
        sections: MutableList<LatexStructureViewCommandElement>,
        treeElements: ArrayList<LatexStructureViewCommandElement>,
        numbering: SectionNumbering
    ) {
        if (command.requiredParametersText().isEmpty()) {
            return
        }

        val child = LatexStructureViewCommandElement.newCommand(command) ?: return

        // First section.
        if (sections.isEmpty()) {
            sections.add(child)
            treeElements.add(child)
            setLevelHint(child, numbering)
            return
        }

        // Order of the most recently added element, which is kept at the end of the list for administrative purposes
        val currentIndex = order(sections.lastOrNull() ?: return)
        val nextIndex = order(command)

        when {
            currentIndex == nextIndex -> registerSameLevel(sections, child, command, treeElements, numbering)
            nextIndex > currentIndex -> registerDeeper(sections, child, numbering)
            else -> registerHigher(sections, child, command, treeElements, numbering)
        }
    }

    private fun registerHigher(
        sections: MutableList<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        currentCmd: LatexCommands,
        treeElements: MutableList<LatexStructureViewCommandElement>,
        numbering: SectionNumbering
    ) {
        val currentOrder = order(currentCmd)
        while (sections.isNotEmpty()) {
            // The last entry is the most recently added element (as a child somewhere), remove it first
            sections.removeLastOrNull()
            val highestLevelOrder = sections.lastOrNull()?.let { order(it) }

            if (highestLevelOrder != null && currentOrder > highestLevelOrder) {
                registerDeeper(sections, child, numbering)
                break
            }
            // Avoid that an element is not added at all by adding it one level up anyway.
            // If index is null, that means that the tree currently only has elements with a higher order.
            else if (highestLevelOrder == null || currentOrder == highestLevelOrder) {
                registerSameLevel(sections, child, currentCmd, treeElements, numbering)
                break
            }
        }
    }

    private fun registerDeeper(
        sections: MutableList<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        numbering: SectionNumbering
    ) {
        sections.lastOrNull()?.addChild(child) ?: return
        sections.add(child)

        setLevelHint(child, numbering)
    }

    private fun registerSameLevel(
        sections: MutableList<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        currentCmd: LatexCommands,
        treeElements: MutableList<LatexStructureViewCommandElement>,
        numbering: SectionNumbering
    ) {
        // The last entry is the most recently added element (as a child somewhere), remove it first
        sections.removeLastOrNull()
        val parent = sections.lastOrNull()
        parent?.addChild(child)
        sections.add(child)

        setLevelHint(child, numbering)

        if (currentCmd.name == sections.minBy { order(it) }.commandName) {
            treeElements.add(child)
        }
    }

    /**
     * Sometimes, we show the actual number a section would be in the structure view after the section (as 'hint').
     */
    private fun setLevelHint(child: LatexStructureViewCommandElement, numbering: SectionNumbering) {
        if (hasStar(child.value)) {
            return
        }

        val level = order(child)
        numbering.increase(level)
        child.setHint(numbering.getTitle(level))
    }

    private fun updateNumbering(cmd: LatexCommands, numbering: SectionNumbering) {
        val token = cmd.commandToken.text
        val required = cmd.requiredParametersText()
        if (required.size < 2) {
            return
        }

        // Get the level to modify.
        val name = required[0]
        val level = order("\\" + name)
        if (level == -1) {
            return
        }

        // Get the amount to modify with.
        val amount = required[1].toIntOrNull() ?: return

        if (token == LatexGenericRegularCommand.SETCOUNTER.cmd) {
            numbering.setCounter(level, amount)
        }
        else {
            numbering.addCounter(level, amount)
        }
    }

    private fun hasStar(commands: LatexCommands): Boolean {
        val leafs = PsiTreeUtil.getChildrenOfType(commands, LeafPsiElement::class.java)
        return Arrays.stream(leafs!!)
            .anyMatch { l -> l.elementType == LatexTypes.STAR }
    }

    private fun order(element: LatexStructureViewCommandElement) = order(element.commandName)

    private fun order(commands: LatexCommands) = order(commands.name)

    private fun order(commandName: String?) = CommandMagic.sectionNameToLevel[commandName] ?: -1
}
