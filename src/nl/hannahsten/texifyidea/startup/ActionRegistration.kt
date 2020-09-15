package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import nl.hannahsten.texifyidea.editor.MoveElementLeftAction

class ActionRegistration : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        ActionManagerImpl.getInstance().replaceAction(IdeActions.MOVE_ELEMENT_LEFT, MoveElementLeftAction())
    }
}