package nl.rubensten.texifyidea.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 * @author Ruben Schellekens
 */
open class BibtexLiveTemplateProvider : DefaultLiveTemplatesProvider {

    override fun getDefaultLiveTemplateFiles() = emptyArray<String>()

    override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/hidden/BibTeX")
}