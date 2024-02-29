package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.ex.InspectionToolRegistrar

/**
 * Map where each [InsightGroup] is mapped to all the relevant inspection ids.
 */
val ALL_TEXIFY_INSPECTIONS: Map<InsightGroup, List<String>> by lazy {
    val inspections = InspectionToolRegistrar.getInstance().createTools()
    val insightGroups = InsightGroup.entries.toTypedArray()
    HashMap<InsightGroup, List<String>>().apply {
        for (group in insightGroups) {
            val groupInspections = ArrayList<String>()
            for (inspection in inspections) {
                if (inspection.shortName.startsWith(group.prefix)) {
                    groupInspections.add(inspection.shortName.substring(group.prefix.length))
                }
            }
            put(group, groupInspections)
        }
    }
}