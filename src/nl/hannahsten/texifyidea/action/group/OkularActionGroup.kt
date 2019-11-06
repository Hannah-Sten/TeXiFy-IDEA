package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup

class OkularActionGroup : DefaultActionGroup() {

    // TODO should be isOkularAvailable()
    override fun canBePerformed(context: DataContext) = true

    override fun hideIfNoVisibleChildren(): Boolean = true
}