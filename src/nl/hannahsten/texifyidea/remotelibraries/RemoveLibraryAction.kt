package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RemoveLibraryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        println("remove library")
    }
}