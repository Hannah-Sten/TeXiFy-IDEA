package nl.hannahsten.texifyidea.action.group

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import nl.hannahsten.texifyidea.run.okular.isOkularAvailable

class OkularActionGroup : DefaultActionGroup() {

    override fun canBePerformed(context: DataContext) = isOkularAvailable()

    override fun hideIfNoVisibleChildren(): Boolean = true
}