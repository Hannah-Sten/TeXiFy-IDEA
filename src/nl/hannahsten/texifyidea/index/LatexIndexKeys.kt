package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexMagicComment

/**
 * @author Hannah Schellekens
 */
object LatexStubIndexKeys {

    val COMMANDS =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.commands")

    val COMMANDS_SPECIAL =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.commands_special")

    val DEFINITIONS =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.definitions")

    val ENVIRONMENTS =
        StubIndexKey.createIndexKey<String, LatexEnvironment>("nl.hannahsten.texifyidea.environments")

    val LABELED_ELEMENT =
        StubIndexKey.createIndexKey<String, LatexComposite>("nl.hannahsten.texifyidea.labeledelement")

    val MAGIC_COMMENTS_KEY =
        StubIndexKey.createIndexKey<String, LatexMagicComment>("nl.hannahsten.texifyidea.magiccomment")
    val LABELED_COMMANDS_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.parameterlabeledcommands")
    val GLOSSARY_ENTRIES_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.glossaryentries")
}

object LatexFileBasedIndexKeys {
    val EXTERNAL_COMMANDS = ID.create<String, String>("nl.hannahsten.texifyidea.external.commands")

//    val EXTERNAL_ENVIRONMENTS =
//        ID.create<String, String>("nl.hannahsten.texifyidea.external.environments")
//
//    val EXTERNAL_LABELS =
//        ID.create<String, String>("nl.hannahsten.texifyidea.external.labels")
//
//    val EXTERNAL_REFERENCES =
//        ID.create<String, String>("nl.hannahsten.texifyidea.external.references")
}