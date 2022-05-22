package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        println("add library")
    }
}