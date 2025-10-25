package nl.hannahsten.texifyidea.editor.folding

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Settings for [LatexCodeFoldingOptionsProvider], inspired by RsCodeFoldingSettings from the Rust plugin.
 * Other languages appear to mostly use editor.xml, so we use it as well.
 * Note: in the debug instance, this file is at build/idea-sandbox/config/options/editor.xml.
 */
@State(name = "LatexCodeFoldingSettings", storages = [Storage("editor.xml")])
class LatexCodeFoldingSettings : PersistentStateComponent<LatexCodeFoldingSettings> {

    var collapseImports = true
    var foldEnvironments = false
    var foldEscapedSymbols = true
    var foldFootnotes = true
    var foldMathSymbols = true
    var foldSections = false
    var foldSymbols = true
    var foldLeftRightExpression = true
    var foldLeftRightCommands = true

    override fun getState() = this

    override fun loadState(state: LatexCodeFoldingSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {

        fun getInstance(): LatexCodeFoldingSettings = service()
    }
}