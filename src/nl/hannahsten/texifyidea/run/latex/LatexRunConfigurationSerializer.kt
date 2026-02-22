package nl.hannahsten.texifyidea.run.latex

import org.jdom.Element

internal object LatexRunConfigurationSerializer {

    private const val COMPILE_STEPS = "compile-steps"
    private const val COMPILE_STEP = "compile-step"
    private const val TYPE = "type"
    private const val STEP_NAME = "step-name"

    fun probeStepSchema(parent: Element): StepSchemaReadStatus {
        val stepsParent = parent.getChild(COMPILE_STEPS) ?: return StepSchemaReadStatus.MISSING
        val steps = stepsParent.getChildren(COMPILE_STEP)
        if (steps.isEmpty()) {
            return StepSchemaReadStatus.INVALID
        }

        val allStepsHaveIdentifier = steps.all { step ->
            !step.getAttributeValue(TYPE).isNullOrBlank() ||
                !step.getAttributeValue(STEP_NAME).isNullOrBlank()
        }
        return if (allStepsHaveIdentifier) {
            StepSchemaReadStatus.PARSED
        }
        else {
            StepSchemaReadStatus.INVALID
        }
    }

    fun readStepTypes(parent: Element): List<String> {
        val stepsParent = parent.getChild(COMPILE_STEPS) ?: return emptyList()
        return stepsParent.getChildren(COMPILE_STEP)
            .mapNotNull { step ->
                step.getAttributeValue(TYPE)?.trim()?.takeIf(String::isNotBlank)
                    ?: step.getAttributeValue(STEP_NAME)?.trim()?.takeIf(String::isNotBlank)
            }
    }

    fun writeStepTypes(parent: Element, stepTypes: List<String>) {
        if (stepTypes.isEmpty()) return
        val stepsParent = Element(COMPILE_STEPS)
        stepTypes.forEach { type ->
            if (type.isBlank()) return@forEach
            stepsParent.addContent(Element(COMPILE_STEP).setAttribute(TYPE, type))
        }
        if (stepsParent.getChildren(COMPILE_STEP).isNotEmpty()) {
            parent.addContent(stepsParent)
        }
    }

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
