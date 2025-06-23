package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexMagicComment

/**
 * @author Hannah Schellekens
 */
object LatexStubIndexKeys {

    val COMMANDS =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.commands")

    val COMMANDS_SPECIAL =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.commandsspecial")

    val DEFINITIONS_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.definitions")

    val DEFINITIONS_NEW =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.definitionsnew")


    val ENVIRONMENTS_KEY =
        StubIndexKey.createIndexKey<String, LatexEnvironment>("nl.hannahsten.texifyidea.environments")
    val MAGIC_COMMENTS_KEY =
        StubIndexKey.createIndexKey<String, LatexMagicComment>("nl.hannahsten.texifyidea.magiccomment")
    val LABELED_ENVIRONMENTS_KEY =
        StubIndexKey.createIndexKey<String, LatexEnvironment>("nl.hannahsten.texifyidea.parameterlabeledenvironments")
    val LABELED_COMMANDS_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.parameterlabeledcommands")
    val GLOSSARY_ENTRIES_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.glossaryentries")
}