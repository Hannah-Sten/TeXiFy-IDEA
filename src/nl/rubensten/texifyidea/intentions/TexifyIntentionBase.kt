package nl.rubensten.texifyidea.intentions

import com.intellij.codeInsight.intention.IntentionAction
import nl.rubensten.texifyidea.insight.InsightGroup

/**
 * @author Ruben Schellekens
 */
abstract class TexifyIntentionBase(

        /**
         * The name of the intention that shows up in the intention overview.
         */
        val name: String,

        /**
         * The group to which the intention belongs (duh).
         */
        val insightGroup: InsightGroup = InsightGroup.LATEX

) : IntentionAction {

    override fun startInWriteAction() = false

    override fun getText() = name

    override fun getFamilyName() = name
}