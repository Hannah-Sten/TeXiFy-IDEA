package nl.hannahsten.texifyidea.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Hannah Schellekens
 */
open class BibtexLiveTemplateProvider : DefaultLiveTemplatesProvider {

    override fun getDefaultLiveTemplateFiles() = emptyArray<String>()

    override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/hidden/BibTeX")
}