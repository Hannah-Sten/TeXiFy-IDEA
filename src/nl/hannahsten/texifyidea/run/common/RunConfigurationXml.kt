package nl.hannahsten.texifyidea.run.common

import org.jdom.Element

internal fun getOrCreateAndClearParent(element: Element, parentTag: String): Element {
    val parent = element.getChild(parentTag) ?: Element(parentTag).also { element.addContent(it) }
    parent.removeContent()
    return parent
}

internal fun Element.addTextChild(tag: String, value: String) {
    addContent(Element(tag).also { it.text = value })
}

internal fun writeCommonCompilationFields(
    parent: Element,
    compilerPath: String?,
    pdfViewerName: String?,
    requireFocus: Boolean,
    viewerCommand: String?,
    writeEnvironmentVariables: (Element) -> Unit,
    expandMacrosEnvVariables: Boolean,
    beforeRunCommand: String?,
    mainFilePath: String,
    workingDirectory: String,
    latexDistribution: String,
    hasBeenRun: Boolean,
) {
    parent.addTextChild("compiler-path", compilerPath ?: "")
    parent.addTextChild("pdf-viewer", pdfViewerName ?: "")
    parent.addTextChild("require-focus", requireFocus.toString())
    parent.addTextChild("viewer-command", viewerCommand ?: "")
    writeEnvironmentVariables(parent)
    parent.addTextChild("expand-macros-in-environment-variables", expandMacrosEnvVariables.toString())
    parent.addTextChild("before-run-command", beforeRunCommand ?: "")
    parent.addTextChild("main-file", mainFilePath)
    parent.addTextChild("working-directory", workingDirectory)
    parent.addTextChild("latex-distribution", latexDistribution)
    parent.addTextChild("has-been-run", hasBeenRun.toString())
}
