package nl.rubensten.texifyidea.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

open class MathLiveTemplateProvider : DefaultLiveTemplatesProvider {

    // TODO: fix to be consistent with LatexLiveTemplateProvider
    override fun getDefaultLiveTemplateFiles() = arrayOf("liveTemplates/hidden/BibTeX")

    override fun getHiddenLiveTemplateFiles() = arrayOf("liveTemplates/hidden/BibTeX")
}