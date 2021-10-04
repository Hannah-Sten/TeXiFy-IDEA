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
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.structure.bibtex.BibtexStructureViewElement
import nl.hannahsten.texifyidea.structure.latex.SectionNumbering.DocumentClass
import nl.hannahsten.texifyidea.util.allCommands
import nl.hannahsten.texifyidea.util.getIncludeCommands
import nl.hannahsten.texifyidea.util.getIncludedFiles
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.*
import java.util.*
import kotlin.collections.ArrayList

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
        val docClass = LatexCommandsIndex.getItems(element.getProject(), scope).asSequence()
            .filter { cmd -> cmd.commandToken.text == "\\documentclass" && cmd.requiredParameters.isNotEmpty() }
            .map { cmd -> cmd.requiredParameters[0] }
            .firstOrNull() ?: "article"

        // Fetch all commands in the active file.
        val numbering = SectionNumbering(DocumentClass.getClassByName(docClass))
        val commands = element.allCommands()
        val treeElements = ArrayList<TreeElement>()

        // Add includes.
        addIncludes(treeElements, commands)

        // Add sectioning.
        val sections = ArrayDeque<LatexStructureViewCommandElement>()
        for (currentCmd in commands) {
            val token = currentCmd.commandToken.text

            // Update counter.
            if (token == "\\addtocounter" || token == "\\setcounter") {
                updateNumbering(currentCmd, numbering)
                continue
            }

            // Only consider section markers.
            if (!CommandMagic.sectionMarkers.contains(token)) {
                continue
            }

            if (currentCmd.requiredParameters.isEmpty()) {
                continue
            }

            val child = LatexStructureViewCommandElement.newCommand(currentCmd) ?: continue

            // First section.
            if (sections.isEmpty()) {
                sections.addFirst(child)
                treeElements.add(child)
                setLevelHint(child, numbering)
                continue
            }

            val currentIndex = order(current(sections) ?: continue)
            val nextIndex = order(currentCmd)

            // Same level.
            when {
                currentIndex == nextIndex -> registerSameLevel(sections, child, currentCmd, treeElements, numbering)
                nextIndex > currentIndex -> registerDeeper(sections, child, numbering)
                else -> registerHigher(sections, child, currentCmd, treeElements, numbering)
            }
        }

        // Add command definitions.
        CommandMagic.commandDefinitions.forEach {
            addFromCommand(treeElements, commands, it)
        }

        // Add label definitions.
        addFromLabelingCommands(treeElements, commands)

        // Add bibitem definitions.
        addFromCommand(treeElements, commands, "\\bibitem")

        return treeElements.toTypedArray()
    }

    private fun addIncludes(treeElements: MutableList<TreeElement>, commands: List<LatexCommands>) {
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
        treeElements: MutableList<TreeElement>, commands: List<LatexCommands>,
        commandName: String
    ) {
        for (cmd in commands) {
            if (cmd.commandToken.text != commandName) continue
            val element = LatexStructureViewCommandElement.newCommand(cmd) ?: continue
            treeElements.add(element)
        }
    }

    private fun addFromLabelingCommands(treeElements: MutableList<TreeElement>, commands: List<LatexCommands>) {
        val labelingCommands = getLabelDefinitionCommands()
        commands.filter { labelingCommands.contains(it.commandToken.text) }
            .mapNotNull { LatexStructureViewCommandElement.newCommand(it) }
            .forEach {
                treeElements.add(it)
            }
    }

    private fun registerHigher(
        sections: Deque<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        currentCmd: LatexCommands,
        treeElements: MutableList<TreeElement>,
        numbering: SectionNumbering
    ) {
        val indexInsert = order(currentCmd)
        while (!sections.isEmpty()) {
            pop(sections)
            val index = order(current(sections) ?: continue)

            if (index == indexInsert) {
                registerSameLevel(sections, child, currentCmd, treeElements, numbering)
                break
            }

            if (indexInsert > index) {
                registerDeeper(sections, child, numbering)
                break
            }
        }
    }

    private fun registerDeeper(
        sections: Deque<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        numbering: SectionNumbering
    ) {
        current(sections)?.addChild(child) ?: return
        queue(child, sections)

        setLevelHint(child, numbering)
    }

    private fun registerSameLevel(
        sections: Deque<LatexStructureViewCommandElement>,
        child: LatexStructureViewCommandElement,
        currentCmd: LatexCommands,
        treeElements: MutableList<TreeElement>,
        numbering: SectionNumbering
    ) {
        sections.removeFirst()
        val parent = sections.peekFirst()
        parent?.addChild(child)
        sections.addFirst(child)

        setLevelHint(child, numbering)

        if (currentCmd.commandToken.text == highestLevel(sections)) {
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
        val required = cmd.requiredParameters

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

    private fun highestLevel(sections: Deque<LatexStructureViewCommandElement>): String {
        return sections.stream()
            .map { this.order(it) }
            .min { obj, anotherInteger -> obj.compareTo(anotherInteger) }
            .map { CommandMagic.sectionMarkers[it] }
            .orElse("\\section")
    }

    private fun pop(sections: Deque<LatexStructureViewCommandElement>) {
        sections.removeFirst()
    }

    private fun queue(
        child: LatexStructureViewCommandElement,
        sections: Deque<LatexStructureViewCommandElement>
    ) {
        sections.addFirst(child)
    }

    private fun current(sections: Deque<LatexStructureViewCommandElement>) = sections.peekFirst() ?: null

    private fun order(element: LatexStructureViewCommandElement) = order(element.commandName)

    private fun order(commands: LatexCommands) = order(commands.commandToken.text)

    private fun order(commandName: String) = CommandMagic.sectionMarkers.indexOf(commandName)
}
