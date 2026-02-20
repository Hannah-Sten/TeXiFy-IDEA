package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.index.StringStubIndexWrapper
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class GotoDefinitionSymbolContributor : AbsIndexBasedChooseByNameContributor<LatexCommands>() {

    override val index: StringStubIndexWrapper<LatexCommands> = NewDefinitionIndex

    override fun createNavigationItem(item: LatexCommands, name: String): NavigationItem? {
        val defCommand = item.name ?: return null
        if (defCommand in CommandMagic.commandDefinitionsAndRedefinitions) {
            return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_COMMAND)
        }
        if (defCommand in CommandMagic.environmentDefinitions || defCommand in CommandMagic.environmentRedefinitions) {
            return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_ENVIRONMENT)
        }
        return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_COMMAND)
    }
}

class GotoLabelSymbolContributor : AbsIndexBasedChooseByNameContributor<LatexComposite>() {
    override val index: StringStubIndexWrapper<LatexComposite> = NewLabelsIndex

    override fun createNavigationItem(item: LatexComposite, name: String): NavigationItem = GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_LABEL)
}

class GotoBibtexLabelSymbolContributor : AbsIndexBasedChooseByNameContributor<BibtexEntry>() {
    override val index: StringStubIndexWrapper<BibtexEntry> = NewBibtexEntryIndex

    override fun createNavigationItem(item: BibtexEntry, name: String): NavigationItem = GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_BIB)
}