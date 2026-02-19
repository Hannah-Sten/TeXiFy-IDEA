package nl.hannahsten.texifyidea.lang.predefined

import com.intellij.openapi.application.ApplicationManager
import nl.hannahsten.texifyidea.lang.CachedLatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.util.Log
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

object AllPredefined : CachedLatexSemanticsLookup() {

    val allEntities = listOf(
        PredefinedPrimitives,
        PredefinedCmdGeneric,
        PredefinedCmdDefinitions,
        PredefinedCmdFiles,
        PredefinedCmdMath,
        PredefinedCmdPairedDelimiters,
        PredefinedCmdMathSymbols,
        PredefinedCmdUnicodeMathSymbols,
        PredefinedCmdTextSymbols,
        PredefinedEnvBasic,
        MorePackages
    ).flatMap {
        it.allEntities
    }

    override fun allEntitiesSeq(): Sequence<LSemanticEntity> = allEntities.asSequence()

    val allCommands: List<LSemanticCommand> = allEntities.filterIsInstance<LSemanticCommand>()

    val allEnvironments: List<LSemanticEnv> = allEntities.filterIsInstance<LSemanticEnv>()

    private val packageToEntities: Map<LatexLib, List<LSemanticEntity>> =
        allEntities.groupBy { it.dependency }.mapValues { it.value }

    fun findByLib(packageName: LatexLib): List<LSemanticEntity> = packageToEntities[packageName] ?: emptyList()

    private val simpleNameLookup = allEntities.associateBy { it.name }

    override fun lookup(name: String): LSemanticEntity? = simpleNameLookup[name]

    val nameToEntities = allEntities.groupBy { it.name }

    fun findAll(name: String): List<LSemanticEntity> = nameToEntities[name] ?: emptyList()

    private val displayToCommand: Map<String, List<LSemanticCommand>> by lazy {
        buildMap<String, MutableList<LSemanticCommand>> {
            for (entity in allCommands) {
                val display = entity.display ?: continue
                this.getOrPut(display) { mutableListOf() }.add(entity)
            }
        }
    }

    fun findCommandByDisplay(display: String): List<LSemanticCommand> = displayToCommand[display] ?: emptyList()

    private val commandContextInverseSearch: Map<LatexContext, List<LSemanticCommand>> by lazy {
        buildMap<LatexContext, MutableList<LSemanticCommand>> {
            for(entity in allCommands) {
                for (arg in entity.arguments) {
                    val contexts = when (val intro = arg.contextSignature) {
                        is LatexContextIntro.Assign -> intro.contexts
                        is LatexContextIntro.Modify -> intro.toAdd
                        LatexContextIntro.Clear, LatexContextIntro.Inherit -> continue
                    }
                    for (context in contexts) {
                        this.getOrPut(context) { mutableListOf() }.add(entity)
                    }
                }
            }
        }
    }

    fun findCommandsByContext(context: LatexContext): List<LSemanticCommand> = commandContextInverseSearch[context] ?: emptyList()

    init {
        val app = ApplicationManager.getApplication()
        if (app == null || app.isInternal) {
            val names = allEntities.groupBy { it }
            for ((item, commands) in names) {
                if (commands.size > 1) {
                    Log.warn("Duplicate predefined items: ${item.name}(${item.dependency}): ${commands.joinToString()}")
                }
            }
        }
    }
}
