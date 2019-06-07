package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.ex.InspectionToolRegistrar
import nl.rubensten.texifyidea.insight.InsightGroup

/**
 * Map where each [InsightGroup] is mapped to all the relevant inspection ids.
 */
val ALL_TEXIFY_INSPECTIONS: Map<InsightGroup, List<String>> by lazy {
    val inspections = InspectionToolRegistrar.getInstance().get()
    val insightGroups = InsightGroup.values()
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