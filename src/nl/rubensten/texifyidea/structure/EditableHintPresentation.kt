package nl.rubensten.texifyidea.structure

import com.intellij.navigation.ItemPresentation

/**
 * @author Ruben Schellekens
 */
interface EditableHintPresentation : ItemPresentation {

    fun setHint(hint: String)
}