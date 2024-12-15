package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.file.*
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewElement
import nl.hannahsten.texifyidea.structure.latex.SectionNumbering.DocumentClass
import nl.hannahsten.texifyidea.util.getIncludeCommands
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.allCommands
import nl.hannahsten.texifyidea.util.parser.getIncludedFiles
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewElement(private val element: PsiElement) : StructureViewTreeElement, SortableTreeElement {

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

        // Get document class.
        val scope = GlobalSearchScope.fileScope(element as PsiFile)
        val docClass = LatexCommandsIndex.Util.getItems(element.getProject(), scope).asSequence()
            .filter { cmd -> cmd.name == LatexGenericRegularCommand.DOCUMENTCLASS.commandWithSlash && cmd.getRequiredParameters().isNotEmpty() }
            .mapNotNull { cmd -> cmd.getRequiredParameters().firstOrNull() }
            .firstOrNull() ?: "article"

        // Fetch all commands in the active file.
        val numbering = SectionNumbering(DocumentClass.getClassByName(docClass))
        val commands = element.allCommands()
        val treeElements = ArrayList<LatexStructureViewCommandElement>()

        // Add includes.
        addIncludes(treeElements, commands)

        // Add sectioning.
        val sections = mutableListOf<LatexStructureViewCommandElement>()
        for (currentCmd in commands) {
            val token = currentCmd.name

            // Update counter.
            if (token == LatexGenericRegularCommand.ADDTOCOUNTER.cmd || token == LatexGenericRegularCommand.SETCOUNTER.cmd) {
                updateNumbering(currentCmd, numbering)
                continue
            }

            // Only consider section markers.
            if (!CommandMagic.sectionMarkers.contains(token)) {
                continue
            }

            if (currentCmd.getRequiredParameters().isEmpty()) {
                continue
            }

            val child = LatexStructureViewCommandElement.newCommand(currentCmd) ?: continue

            // First section.
            if (sections.isEmpty()) {
                sections.add(child)
                treeElements.add(child)
                setLevelHint(child, numbering)
                continue
            }

            val currentIndex = order(sections.lastOrNull() ?: continue)
            val nextIndex = order(currentCmd)

            when {
                currentIndex == nextIndex -> registerSameLevel(sections, child, currentCmd, treeElements, numbering)
                nextIndex > currentIndex -> registerDeeper(sections, child, numbering)
                else -> registerHigher(sections, child, currentCmd, treeElements, numbering)
            }
        }

        // Add command definitions.
        CommandMagic.commandDefinitionsAndRedefinitions.forEach {
            addFromCommand(treeElements, commands, it)
        }

        // Add label definitions.
        addFromLabelingCommands(treeElements, commands)

        // Add bibitem definitions.
        addFromCommand(treeElements, commands, LatexGenericRegularCommand.BIBITEM.cmd)

        return treeElements.sortedBy { it.value.textOffset }.toTypedArray()
    }

    private fun addIncludes(treeElements: MutableList<LatexStructureViewCommandElement>, commands: List<LatexCommands>) {
        for (command in commands) {
            if (command.name !in getIncludeCommands()) {
                continue
            }

            val elt = LatexStructureViewCommandElement.newCommand(command) ?: continue
            for (psiFile in command.getIncludedFiles(includeInstalledPackages = TexifySettings.getInstance().showPackagesInStructureView)) {
                if (BibtexFileType == psiFile.fileType) {
                    elt.addChild(BibtexStructureViewElement(psiFile))
                }
                else if (LatexFileType == psiFile.fileType || StyleFileType == psiFile.fileType) {
                    elt.addChild(LatexStructureViewElement(psiFile))
                }
            }
            treeElements.add(elt)
        }
    }

    private fun addFromCommand(
        treeElements: MutableList<LatexStructureViewCommandElement>, commands: List<LatexCommands>,
        commandName: String
    ) {
        for (cmd in commands) {
            if (cmd.commandToken.text != commandName) continue
            val element = LatexStructureViewCommandElement.newCommand(cmd) ?: continue
            treeElements.add(element)
        }
    }

    /**
     * Add commands which define new labels
     */
    private fun addFromLabelingCommands(treeElements: MutableList<LatexStructureViewCommandElement>, commands: List<LatexCommands>) {
        val labelingCommands = getLabelDefinitionCommands()
        commands.filter { labelingCommands.contains(it.name) }
            .mapNotNull { LatexStructureViewCommandElement.newCommand(it) }
            .forEach {
                treeElements.add(it)
            }
    }

    private fun registerHigher(
        sections: MutableList<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        currentCmd: LatexCommands,
        treeElements: MutableList<LatexStructureViewCommandElement>,
        numbering: SectionNumbering
    ) {
        val indexInsert = order(currentCmd)
        while (sections.isNotEmpty()) {
            sections.removeLastOrNull()
            val index = sections.lastOrNull()?.let { order(it) }

            if (index != null && indexInsert > index) {
                registerDeeper(sections, child, numbering)
                break
            }
            // Avoid that an element is not added at all by adding it one level up anyway.
            // If index is null, that means that the tree currently only has elements with a higher order.
            else if (index == null || indexInsert == index) {
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
        sections.removeLastOrNull()
        val parent = sections.lastOrNull()
        parent?.addChild(child)
        sections.addFirst(child)

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
        val required = cmd.getRequiredParameters()
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

        if (token == "\\setcounter") {
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

    private fun order(commands: LatexCommands) = order(commands.commandToken.text)

    private fun order(commandName: String) = CommandMagic.sectionMarkers.indexOf(commandName)
}
