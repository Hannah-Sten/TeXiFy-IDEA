package nl.hannahsten.texifyidea.action.wizard.graphic

import nl.hannahsten.texifyidea.lang.graphic.CaptionLocation
import nl.hannahsten.texifyidea.lang.graphic.FigureLocation

/**
 * @author Hannah Schellekens
 */
data class InsertGraphicData(
        val filePath: String,
        val relativePath: Boolean,
        val options: String,
        val center: Boolean,
        val placeInFigure: Boolean,
        /** `null` when [placeInFigure] is `false`, not `null` otherwise */
        val captionLocation: CaptionLocation? = null,
        /** `null` when [placeInFigure] is `false`, not `null` otherwise */
        val caption: String? = null,
        /** `null` when [placeInFigure] is `false`, not `null` otherwise */
        val shortCaption: String? = null,
        /** `null` when [placeInFigure] is `false`, not `null` otherwise */
        val label: String? = null,
        /** `null` when [placeInFigure] is `false`, not `null` otherwise */
        val positions: List<FigureLocation>? = null
)