package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.intention.IntentionAction

/**
 * @author Hannah Schellekens
 */
abstract class TexifyIntentionBase(

    /**
     * The name of the intention that shows up in the intention overview.
     */
    val name: String

) : IntentionAction {

    override fun startInWriteAction() = false

    override fun getText() = name

    override fun getFamilyName() = name
}