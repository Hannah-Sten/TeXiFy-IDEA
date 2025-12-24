package nl.hannahsten.texifyidea.modules

import com.intellij.openapi.module.ModuleType
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * Note: ModuleTypes are deprecated, see [ModuleType].
 * Therefore, we do not register it in plugin.xml, so it won't show to the user.
 * We still use it for new project creation though.
 *
 * @author Sten Wessel
 */
class LatexModuleType : ModuleType<LatexModuleBuilder>(ID) {

    companion object {
        const val ID = "LATEX_MODULE_TYPE"
    }

    override fun createModuleBuilder() = LatexModuleBuilder()

    override fun getName() = "LaTeX"

    override fun getDescription() = "LaTeX"

    override fun getNodeIcon(isOpened: Boolean) = TexifyIcons.LATEX_MODULE
}