package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.indexing.ID
import nl.hannahsten.texifyidea.index.file.LatexSimpleDefinition
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

    val LABELED_ELEMENT =
        StubIndexKey.createIndexKey<String, LatexComposite>("nl.hannahsten.texifyidea.labeledelement")

    val MAGIC_COMMENTS_KEY =
        StubIndexKey.createIndexKey<String, LatexMagicComment>("nl.hannahsten.texifyidea.magiccomment")

    // The following keys are not used

    val ENVIRONMENTS =
        StubIndexKey.createIndexKey<String, LatexEnvironment>("nl.hannahsten.texifyidea.environments")

    val LABELED_COMMANDS_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.parameterlabeledcommands")
    val GLOSSARY_ENTRIES_KEY =
        StubIndexKey.createIndexKey<String, LatexCommands>("nl.hannahsten.texifyidea.glossaryentries")
}

object LatexFileBasedIndexKeys {
    val EXTERNAL_COMMANDS: ID<String, String> = ID.create("nl.hannahsten.texifyidea.external.commands")

    val DTX_DEFINITIONS: ID<String, List<LatexSimpleDefinition>> = ID.create("nl.hannahsten.texifyidea.dtx.definitions")

    val REGEX_PACKAGE_INCLUSIONS =
        ID.create<Int, List<String>>("nl.hannahsten.texifyidea.regex.package.inclusions")

    val REGEX_COMMAND_DEFINITIONS =
        ID.create<Int, List<String>>("nl.hannahsten.texifyidea.regex.def.commands")
    val REGEX_ENVIRONMENT_DEFINITIONS =
        ID.create<Int, List<String>>("nl.hannahsten.texifyidea.regex.def.environments")
}