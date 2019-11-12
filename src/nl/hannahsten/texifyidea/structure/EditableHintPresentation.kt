package nl.hannahsten.texifyidea.structure

import com.intellij.navigation.ItemPresentation

/**
 * @author Hannah Schellekens
 */
interface EditableHintPresentation : ItemPresentation {

    fun setHint(hint: String)
}