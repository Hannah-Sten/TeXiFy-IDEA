package nl.hannahsten.texifyidea.run.latex

import org.jdom.Element

internal object LatexRunConfigurationSerializer {

    fun readRunConfigIds(parent: Element, listTag: String, legacyTag: String? = null): MutableSet<String> {
        val listElement = parent.getChild(listTag)
        if (listElement != null) {
            return listElement.getChildren("id")
                .mapNotNull { it.textTrim.takeIf(String::isNotBlank) }
                .toMutableSet()
        }

        val legacy = legacyTag?.let { parent.getChildText(it) } ?: return mutableSetOf()
        return parseLegacySet(legacy)
    }

    fun writeRunConfigIds(parent: Element, listTag: String, ids: Set<String>) {
        val listElement = Element(listTag)
        ids.forEach { listElement.addContent(Element("id").setText(it)) }
        parent.addContent(listElement)
    }

    private fun parseLegacySet(value: String?): MutableSet<String> {
        if (value.isNullOrBlank()) return mutableSetOf()
        val trimmed = value.trim()
        if (trimmed.length < 2 || trimmed.first() != '[' || trimmed.last() != ']') return mutableSetOf()
        return trimmed
            .drop(1)
            .dropLast(1)
            .split(", ")
            .filter { it.isNotBlank() }
            .toMutableSet()
    }
}
