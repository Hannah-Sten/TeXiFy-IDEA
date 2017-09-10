package nl.rubensten.texifyidea.intentions

import com.intellij.codeInsight.intention.IntentionAction

/**
 * @author Ruben Schellekens
 */
abstract class TexifyIntentionBase(

        /**
         * The name of the intention that shows up in the intention overview.
         */
        val name: String

) : IntentionAction {

    companion object {

        private val FAMILY_NAME = "TeXiFy"
    }

    override fun getText() = name

    override fun startInWriteAction() = false

    override fun getFamilyName() = FAMILY_NAME
}