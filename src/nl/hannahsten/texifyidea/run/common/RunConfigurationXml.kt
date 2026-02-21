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
