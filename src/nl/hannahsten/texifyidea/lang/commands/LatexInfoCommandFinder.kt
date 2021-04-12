package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage

object LatexInfoCommandFinder {
    val commands: Set<LatexCommand> by lazy {
        setOf(
            object : LatexCommand {
                override val command: String = "nonsense"
                override val display: String? = null
                override val description: String = "I am a nonsense command to test this functionality"
                override val arguments: Array<out Argument> =
                    arrayOf(RequiredArgument("req"), OptionalArgument("option"), RequiredArgument("key"))
                override val isMathMode: Boolean = false
                override val dependency: LatexPackage = LatexPackage.DEFAULT
            },
            object : LatexCommand {
                override val command: String = "cite"
                override val display: String? = null
                override val description: String = "Use me to cite stuff."
                override val arguments: Array<out Argument> = arrayOf(OptionalArgument("extra"), "keys".asRequired())
                override val isMathMode: Boolean = false
                override val dependency: LatexPackage = LatexPackage.DEFAULT
            }
        )
    }
}