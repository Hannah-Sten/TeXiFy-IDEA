package nl.hannahsten.texifyidea.util

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.ui.treeStructure.Tree

object TexifyDataKeys {

    val LIBRARY_TREE = DataKey.create<Tree>("tree")

    val LIBRARY_IDENTIFIER = DataKey.create<String>("library.identifier")

    val LIBRARY_NAME = DataKey.create<String>("library.name")
}