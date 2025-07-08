package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.application.smartReadAction
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewElement
import nl.hannahsten.texifyidea.structure.latex.SectionNumbering.DocumentClass
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommandsNoUpdate
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.getIncludedFiles
import nl.hannahsten.texifyidea.util.runInBackgroundWithoutProgress
import nl.hannahsten.texifyidea.util.updateAndGetIncludeCommands
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
                it.requiredParametersText().firstOrNull()
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

        val includeCommands = updateAndGetIncludeCommands(element.project)
        val labelingCommands = getLabelDefinitionCommandsNoUpdate()

        val cachedFileInclusions = Cache.includedFiles.mapKeys { it.key.element }

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

                in includeCommands -> {
                    cachedFileInclusions.getOrDefault(command, null)?.let { includedFiles ->
                        for (filePointer in includedFiles) {
                            val psiFile = filePointer.element ?: continue
                            if (BibtexFileType == psiFile.fileType) {
                                newElement.addChild(BibtexStructureViewElement(psiFile))
                            }
                            else if (LatexFileType == psiFile.fileType || StyleFileType == psiFile.fileType) {
                                newElement.addChild(LatexStructureViewElement(psiFile))
                            }
                        }
                        addAtCurrentSectionLevel(sections, treeElements, newElement)
                    }
                }
            }
        }

        // This can take a long time (a minute for a large file), but it is not crucial for the structure view, so we get the info in the background.
        // This function may be called for every editor action, so cache this as well to reduce cpu usage
        val includeCommandsElements = commands.filter { it.name in includeCommands }
        if (includeCommandsElements.count() != Cache.includedFiles.size) {
            runInBackgroundWithoutProgress {
                smartReadAction(element.project) {
                    val newIncludes = includeCommandsElements.associate {
                        Pair(it.createSmartPointer(), it.getIncludedFiles(includeInstalledPackages = TexifySettings.getInstance().showPackagesInStructureView).map { it.createSmartPointer() })
                    }
                    // Clear cache to avoid it becoming outdated too much
                    Cache.includedFiles.clear()
                    Cache.includedFiles.putAll(newIncludes)
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
