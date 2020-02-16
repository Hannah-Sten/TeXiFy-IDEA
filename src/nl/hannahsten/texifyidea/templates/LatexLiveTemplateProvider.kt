package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Sten Wessel
 */
class LatexLiveTemplateProvider : DefaultLiveTemplatesProvider {

    override fun getDefaultLiveTemplateFiles() = emptyArray<String>()

    override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/hidden/Math")
}